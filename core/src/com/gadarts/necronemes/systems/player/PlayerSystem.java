package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.DefaultGameSettings;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.mi.GameModelInstance;
import com.gadarts.necronemes.components.player.Item;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.components.player.Weapon;
import com.gadarts.necronemes.map.CalculatePathRequest;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphConnectionCosts;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.map.MapGraphPath;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import com.gadarts.necronemes.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystemEventsSubscriber;
import com.gadarts.necronemes.utils.GeneralUtils;

import static com.gadarts.necronemes.systems.character.CharacterCommands.GO_TO;
import static com.gadarts.necronemes.systems.character.CharacterCommands.GO_TO_PICKUP;

public class PlayerSystem extends GameSystem<PlayerSystemEventsSubscriber> implements UserInterfaceSystemEventsSubscriber, CharacterSystemEventsSubscriber {
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final CalculatePathRequest request = new CalculatePathRequest();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private static final Vector3 auxVector3 = new Vector3();
	private PathPlanHandler playerPathPlanner;

	public PlayerSystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
	}

	@Override
	public void onUserAppliedSelectionToSelectedWeapon(Weapon weapon) {
		setSelectedWeapon(weapon);
	}

	private void setSelectedWeapon(final Weapon selectedWeapon) {
		if (selectedWeapon != getSystemsCommonData().getStorage().getSelectedWeapon()) {
			getSystemsCommonData().getStorage().setSelectedWeapon(selectedWeapon);
		}
	}

	@Override
	public void onCharacterCommandDone(final Entity character, final CharacterCommand executedCommand) {
		if (ComponentsMapper.player.has(character)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onPlayerFinishedTurn();
			}
		}
	}

	@Override
	public void onItemPickedUp(final Entity itemPickedUp) {
		Item item = ComponentsMapper.pickup.get(itemPickedUp).getItem();
		if (getSystemsCommonData().getStorage().addItem(item)) {
			for (PlayerSystemEventsSubscriber subscriber : subscribers) {
				subscriber.onItemAddedToStorage(item);
			}
			getSystemsCommonData().getUiStage().onItemAddedToStorage(item);
		}
		getSoundPlayer().playSound(Assets.Sounds.PICKUP);
	}

	@Override
	public void onUserSelectedNodeToApplyTurn(MapGraphNode node) {
		applyPlayerTurn(node);
	}

	private void applyPlayerTurn(final MapGraphNode cursorNode) {
		int pathSize = playerPathPlanner.getCurrentPath().getCount();
		if (!playerPathPlanner.getCurrentPath().nodes.isEmpty() && playerPathPlanner.getCurrentPath().get(pathSize - 1).equals(cursorNode)) {
			applyPlayerCommandAccordingToPlan();
		} else {
			planPath(cursorNode);
		}
	}

	private void planPath(final MapGraphNode cursorNode) {
		if (calculatePathAccordingToSelection(cursorNode)) {
			pathHasCreated();
		}
	}

	private void pathHasCreated( ) {
		for (PlayerSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onPlayerPathCreated();
		}
		Entity player = getSystemsCommonData().getPlayer();
		playerPathPlanner.displayPathPlan(ComponentsMapper.character.get(player).getSkills().getAgility());
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode) {
		CharacterDecalComponent charDecalComp = ComponentsMapper.characterDecal.get(getSystemsCommonData().getPlayer());
		MapGraphPath plannedPath = playerPathPlanner.getCurrentPath();
		initializePathPlanRequest(cursorNode, charDecalComp, plannedPath);
		return GeneralUtils.calculatePath(request, playerPathPlanner.getPathFinder(), playerPathPlanner.getHeuristic());
	}

	private void initializePathPlanRequest(MapGraphNode cursorNode,
										   CharacterDecalComponent charDecalComp,
										   MapGraphPath plannedPath) {
		request.setSourceNode(getSystemsCommonData().getMap().getNode(charDecalComp.getNodePosition(auxVector2_1)));
		request.setDestNode(cursorNode);
		request.setOutputPath(plannedPath);
		request.setAvoidCharactersInCalculations(true);
		request.setMaxCostInclusive(MapGraphConnectionCosts.CLEAN);
	}

	private void applyPlayerCommandAccordingToPlan( ) {
		playerPathPlanner.hideAllArrows();
		SystemsCommonData commonData = getSystemsCommonData();
		CharacterDecalComponent charDecalComp = ComponentsMapper.characterDecal.get(commonData.getPlayer());
		MapGraphNode playerNode = commonData.getMap().getNode(charDecalComp.getNodePosition(auxVector2_1));
		if (commonData.getItemToPickup() != null || isPickupAndPlayerOnSameNode(commonData.getMap(), playerNode)) {
			applyGoToPickupCommand(playerPathPlanner.getCurrentPath(), commonData.getItemToPickup());
		} else {
			applyGoToCommand(playerPathPlanner.getCurrentPath());
		}
	}

	private boolean isPickupAndPlayerOnSameNode(MapGraph map, MapGraphNode playerNode) {
		if (getSystemsCommonData().getCurrentHighLightedPickup() == null) return false;
		Entity p = getSystemsCommonData().getCurrentHighLightedPickup();
		GameModelInstance modelInstance = ComponentsMapper.modelInstance.get(p).getModelInstance();
		Vector3 pickupPosition = modelInstance.transform.getTranslation(auxVector3);
		return map.getNode(pickupPosition).equals(playerNode);
	}

	private void applyGoToPickupCommand(final MapGraphPath path, final Entity itemToPickup) {
		Entity player = getSystemsCommonData().getPlayer();
		auxCommand.init(GO_TO_PICKUP, path, player, itemToPickup);
		subscribers.forEach(sub -> sub.onPlayerAppliedCommand(auxCommand, player));
	}

	private void applyGoToCommand(final MapGraphPath path) {
		SystemsCommonData systemsCommonData = getSystemsCommonData();
		MapGraph map = systemsCommonData.getMap();
		Entity player = systemsCommonData.getPlayer();
		MapGraphNode playerNode = map.getNode(ComponentsMapper.characterDecal.get(player).getDecal().getPosition());
		if (path.getCount() > 0 && !playerNode.equals(path.get(path.getCount() - 1))) {
			CharacterCommand command = auxCommand.init(GO_TO, path, player);
			subscribers.forEach(sub -> sub.onPlayerAppliedCommand(command, player));
		}
	}

	@Override
	public Class<PlayerSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return PlayerSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData() {
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

	private Weapon initializeStartingWeapon() {
		Weapon weapon = Pools.obtain(Weapon.class);
		Texture image = getAssetsManager().getTexture(DefaultGameSettings.STARTING_WEAPON.getImage());
		weapon.init(DefaultGameSettings.STARTING_WEAPON, 0, 0, image);
		return weapon;
	}

	@Override
	public void dispose() {
		getSystemsCommonData().getStorage().clear();
	}
}
