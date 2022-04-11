package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.pickups.WeaponsDefinitions;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.character.CharacterAnimation;
import com.gadarts.necronemes.components.character.CharacterAnimations;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.character.CharacterSpriteData;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.player.Item;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.components.player.Weapon;
import com.gadarts.necronemes.map.*;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import com.gadarts.necronemes.systems.character.CharacterCommands;
import com.gadarts.necronemes.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.necronemes.systems.ui.AttackNodesHandler;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystemEventsSubscriber;

import java.util.List;

import static com.gadarts.necronemes.components.ComponentsMapper.*;
import static com.gadarts.necronemes.map.MapGraphConnectionCosts.CLEAN;
import static com.gadarts.necronemes.systems.character.CharacterCommands.*;
import static com.gadarts.necronemes.utils.GeneralUtils.calculatePath;

public class PlayerSystem extends GameSystem<PlayerSystemEventsSubscriber> implements
		UserInterfaceSystemEventsSubscriber,
		CharacterSystemEventsSubscriber {
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final CalculatePathRequest request = new CalculatePathRequest();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final Vector3 auxVector3 = new Vector3();
	private PathPlanHandler playerPathPlanner;

	public PlayerSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
	}

	@Override
	public void onSelectedWeaponChanged(Weapon selectedWeapon) {
		WeaponsDefinitions definition = (WeaponsDefinitions) selectedWeapon.getDefinition();
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		Entity player = systemsCommonData.getPlayer();
		CharacterDecalComponent cdc = characterDecal.get(player);
		CharacterAnimations animations = getAssetsManager().get(Assets.Atlases.findByRelatedWeapon(definition).name());
		cdc.init(animations, cdc.getSpriteType(), cdc.getDirection(), auxVector3.set(cdc.getDecal().getPosition()));
		CharacterAnimation animation = animations.get(cdc.getSpriteType(), cdc.getDirection());
		ComponentsMapper.animation.get(player).init(cdc.getSpriteType().getAnimationDuration(), animation);
		if (selectedWeapon != systemsCommonData.getStorage().getSelectedWeapon()) {
			systemsCommonData.getStorage().setSelectedWeapon(selectedWeapon);
		}
	}

	@Override
	public void onCharacterCommandDone(final Entity character, final CharacterCommand executedCommand) {
		if (player.has(character)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerFinishedTurn();
			}
		}
	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {
		Item item = pickup.get(itemPickedUp).getItem();
		if (getSystemsCommonData().getStorage().addItem(item)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onItemAddedToStorage(item);
			}
		}
		getSoundPlayer().playSound(Assets.Sounds.PICKUP);
	}

	@Override
	public void onUserSelectedNodeToApplyTurn(MapGraphNode node, AttackNodesHandler attackNodesHandler) {
		applyPlayerTurn(node, attackNodesHandler);
	}

	private void applyPlayerTurn(final MapGraphNode cursorNode, AttackNodesHandler attackNodesHandler) {
		MapGraphPath currentPath = playerPathPlanner.getCurrentPath();
		int pathSize = currentPath.getCount();
		if (!currentPath.nodes.isEmpty() && currentPath.get(pathSize - 1).equals(cursorNode)) {
			applyPlayerCommandAccordingToPlan(cursorNode, attackNodesHandler);
		} else {
			planPath(cursorNode, attackNodesHandler);
		}
	}

	private void planPath(final MapGraphNode cursorNode, AttackNodesHandler attackNodesHandler) {
		Entity enemyAtNode = getSystemsCommonData().getMap().getAliveEnemyFromNode(cursorNode);
		if (!calculatePathAccordingToSelection(cursorNode, enemyAtNode)) return;
		MapGraphNode selectedAttackNode = attackNodesHandler.getSelectedAttackNode();
		SystemsCommonData commonData = getSystemsCommonData();
		Entity highLightedPickup = commonData.getCurrentHighLightedPickup();
		if (highLightedPickup != null || isSelectedAttackNodeIsNotInAvailableNodes(cursorNode, selectedAttackNode)) {
			attackNodesHandler.reset();
		}
		pathHasCreated(cursorNode, enemyAtNode, attackNodesHandler);
	}

	private boolean isSelectedAttackNodeIsNotInAvailableNodes(MapGraphNode cursorNode, MapGraphNode selectedAttackNode) {
		MapGraph map = getSystemsCommonData().getMap();
		return selectedAttackNode != null
				&& !isNodeInAvailableNodes(cursorNode, map.getAvailableNodesAroundNode(selectedAttackNode));
	}

	private void enemySelected(final MapGraphNode node, final Entity enemyAtNode, final AttackNodesHandler attackNodesHandler) {
		Weapon selectedWeapon = getSystemsCommonData().getStorage().getSelectedWeapon();
		if (selectedWeapon.isMelee()) {
			List<MapGraphNode> availableNodes = getSystemsCommonData().getMap().getAvailableNodesAroundNode(node);
			attackNodesHandler.setSelectedAttackNode(node);
			activateAttackMode(enemyAtNode, availableNodes);
		} else {
			playerPathPlanner.resetPlan();
			enemySelectedWithRangeWeapon(node);
		}
	}

	private void enemySelectedWithRangeWeapon(final MapGraphNode node) {
		Entity player = getSystemsCommonData().getPlayer();
		CharacterComponent charComp = character.get(player);
		Weapon w = getSystemsCommonData().getStorage().getSelectedWeapon();
		CharacterSpriteData characterSpriteData = charComp.getCharacterSpriteData();
		characterSpriteData.setMeleeHitFrameIndex(((WeaponsDefinitions) w.getDefinition()).getHitFrameIndex());
		Entity targetNode = getSystemsCommonData().getMap().getAliveEnemyFromNode(node);
		charComp.setTarget(targetNode);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerAppliedCommand(auxCommand.init(ATTACK_PRIMARY, null, player, targetNode));
		}
	}

	private void activateAttackMode(final Entity enemyAtNode, final List<MapGraphNode> availableNodes) {
		character.get(getSystemsCommonData().getPlayer()).setTarget(enemyAtNode);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeActivated(availableNodes);
		}
	}

	private void pathHasCreated(MapGraphNode cursorNode, Entity enemyAtNode, AttackNodesHandler attackNodesHandler) {
		if (enemyAtNode != null) {
			enemySelected(cursorNode, enemyAtNode, attackNodesHandler);
		}
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerPathCreated();
		}
		Entity player = getSystemsCommonData().getPlayer();
		playerPathPlanner.displayPathPlan(character.get(player).getSkills().getAgility());
	}

	public boolean calculatePathToCharacter(MapGraphNode sourceNode,
											Entity character,
											boolean avoidCharactersInCalculation,
											MapGraphConnectionCosts maxCostPerNodeConnection) {
		playerPathPlanner.getCurrentPath().clear();
		CharacterDecalComponent characterDecalComponent = characterDecal.get(character);
		Vector2 cellPosition = characterDecalComponent.getNodePosition(auxVector2_1);
		MapGraphNode destNode = getSystemsCommonData().getMap().getNode((int) cellPosition.x, (int) cellPosition.y);
		initializePathPlanRequest(sourceNode, destNode, maxCostPerNodeConnection, avoidCharactersInCalculation);
		return playerPathPlanner.getPathFinder().searchNodePathBeforeCommand(playerPathPlanner.getHeuristic(), request);
	}

	private void initializePathPlanRequest(MapGraphNode sourceNode,
										   MapGraphNode destinationNode,
										   MapGraphConnectionCosts maxCostInclusive,
										   boolean avoidCharactersInCalculations) {
		request.setSourceNode(sourceNode);
		request.setDestNode(destinationNode);
		request.setOutputPath(playerPathPlanner.getCurrentPath());
		request.setAvoidCharactersInCalculations(avoidCharactersInCalculations);
		request.setMaxCostInclusive(maxCostInclusive);
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode, Entity enemyAtNode) {
		CharacterDecalComponent charDecalComp = characterDecal.get(getSystemsCommonData().getPlayer());
		MapGraphPath plannedPath = playerPathPlanner.getCurrentPath();
		initializePathPlanRequest(cursorNode, charDecalComp, plannedPath);
		Vector2 cellPosition = charDecalComp.getNodePosition(auxVector2_1);
		MapGraphNode playerNode = getSystemsCommonData().getMap().getNode(cellPosition);
		return calculatePathToEnemy(enemyAtNode, playerNode)
				|| calculatePath(request, playerPathPlanner.getPathFinder(), playerPathPlanner.getHeuristic());
	}

	private boolean calculatePathToEnemy(Entity enemyAtNode, MapGraphNode playerNode) {
		return enemyAtNode != null
				&& character.get(enemyAtNode).getSkills().getHealthData().getHp() > 0
				&& calculatePathToCharacter(playerNode, enemyAtNode, true, CLEAN);
	}

	private void initializePathPlanRequest(MapGraphNode cursorNode,
										   CharacterDecalComponent charDecalComp,
										   MapGraphPath plannedPath) {
		request.setSourceNode(getSystemsCommonData().getMap().getNode(charDecalComp.getNodePosition(auxVector2_1)));
		request.setDestNode(cursorNode);
		request.setOutputPath(plannedPath);
		request.setAvoidCharactersInCalculations(true);
		request.setMaxCostInclusive(CLEAN);
	}

	private void applyPlayerCommandAccordingToPlan(MapGraphNode cursorNode, AttackNodesHandler attackNodesHandler) {
		playerPathPlanner.hideAllArrows();
		SystemsCommonData commonData = getSystemsCommonData();
		CharacterDecalComponent charDecalComp = characterDecal.get(commonData.getPlayer());
		MapGraphNode playerNode = commonData.getMap().getNode(charDecalComp.getNodePosition(auxVector2_1));
		if (attackNodesHandler.getSelectedAttackNode() == null) {
			applyCommandWhenNoAttackNodeSelected(commonData, playerNode);
		} else {
			applyPlayerMeleeCommand(cursorNode, playerNode, attackNodesHandler);
		}
	}

	private void applyCommandWhenNoAttackNodeSelected(SystemsCommonData commonData, MapGraphNode playerNode) {
		if (commonData.getItemToPickup() != null || isPickupAndPlayerOnSameNode(commonData.getMap(), playerNode)) {
			applyPlayerCommand(GO_TO_PICKUP, commonData.getItemToPickup());
		} else {
			applyGoToCommand(playerPathPlanner.getCurrentPath());
		}
	}

	private boolean isNodeInAvailableNodes(final MapGraphNode node, final List<MapGraphNode> availableNodes) {
		boolean result = false;
		for (MapGraphNode availableNode : availableNodes) {
			if (availableNode.equals(node)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private void applyPlayerMeleeCommand(final MapGraphNode targetNode,
										 final MapGraphNode playerNode,
										 final AttackNodesHandler attackNodesHandler) {
		MapGraphNode attackNode = attackNodesHandler.getSelectedAttackNode();
		boolean result = targetNode.equals(attackNode);
		MapGraph map = getSystemsCommonData().getMap();
		result |= isNodeInAvailableNodes(targetNode, map.getAvailableNodesAroundNode(attackNode));
		result |= targetNode.equals(attackNode) && playerNode.isConnectedNeighbour(attackNode);
		if (result) {
			calculatePathToMelee(targetNode, map);
			applyPlayerCommand(GO_TO_MELEE);
		}
		deactivateAttackMode(attackNodesHandler);
	}

	private void calculatePathToMelee(MapGraphNode targetNode, MapGraph map) {
		if (map.getAliveEnemyFromNode(targetNode) != null) {
			Array<MapGraphNode> nodes = playerPathPlanner.getCurrentPath().nodes;
			nodes.removeIndex(nodes.size - 1);
		}
	}

	private void deactivateAttackMode(AttackNodesHandler attackNodesHandler) {
		attackNodesHandler.setSelectedAttackNode(null);
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onAttackModeDeactivated();
		}
	}

	private boolean isPickupAndPlayerOnSameNode(MapGraph map, MapGraphNode playerNode) {
		if (getSystemsCommonData().getCurrentHighLightedPickup() == null) return false;
		Entity p = getSystemsCommonData().getCurrentHighLightedPickup();
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(p).getModelInstance();
		Vector3 pickupPosition = modelInstance.transform.getTranslation(auxVector3);
		return map.getNode(pickupPosition).equals(playerNode);
	}

	private void applyPlayerCommand(CharacterCommands commandDefinition) {
		applyPlayerCommand(commandDefinition, null);
	}

	private void applyPlayerCommand(CharacterCommands CommandDefinition, Object additionalData) {
		Entity player = getSystemsCommonData().getPlayer();
		auxCommand.init(CommandDefinition, playerPathPlanner.getCurrentPath(), player, additionalData);
		subscribers.forEach(sub -> sub.onPlayerAppliedCommand(auxCommand));
	}

	private void applyGoToCommand(final MapGraphPath path) {
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		MapGraph map = systemsCommonData.getMap();
		Entity player = systemsCommonData.getPlayer();
		MapGraphNode playerNode = map.getNode(characterDecal.get(player).getDecal().getPosition());
		if (path.getCount() > 0 && !playerNode.equals(path.get(path.getCount() - 1))) {
			applyPlayerCommand(GO_TO);
		}
	}

	@Override
	public Class<PlayerSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return PlayerSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {
		playerPathPlanner = new PathPlanHandler(getAssetsManager(), getSystemsCommonData().getMap());
		playerPathPlanner.init((PooledEngine) getEngine());
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		getSystemsCommonData().setPlayer(getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
		Weapon weapon = initializeStartingWeapon();
		getSystemsCommonData().getStorage().setSelectedWeapon(weapon);
	}

	private Weapon initializeStartingWeapon( ) {
		Weapon weapon = Pools.obtain(Weapon.class);
		Texture image = getAssetsManager().getTexture(DefaultGameSettings.STARTING_WEAPON.getImage());
		weapon.init(DefaultGameSettings.STARTING_WEAPON, 0, 0, image);
		return weapon;
	}

	@Override
	public void dispose( ) {
		getSystemsCommonData().getStorage().clear();
	}
}
