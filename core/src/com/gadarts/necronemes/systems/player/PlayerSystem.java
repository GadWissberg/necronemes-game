package com.gadarts.necronemes.systems.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector2;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.cd.CharacterDecalComponent;
import com.gadarts.necronemes.components.player.PlayerComponent;
import com.gadarts.necronemes.map.MapGraph;
import com.gadarts.necronemes.map.MapGraphConnectionCosts;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.map.MapGraphPath;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.character.CharacterCommand;
import com.gadarts.necronemes.systems.ui.UserInterfaceSystemEventsSubscriber;
import com.gadarts.necronemes.utils.CalculatePathRequest;
import com.gadarts.necronemes.utils.GeneralUtils;

import static com.gadarts.necronemes.systems.character.CharacterCommands.GO_TO;

public class PlayerSystem extends GameSystem<PlayerSystemEventsSubscriber> implements UserInterfaceSystemEventsSubscriber {
	private static final Vector2 auxVector2_1 = new Vector2();
	private static final CalculatePathRequest request = new CalculatePathRequest();
	private static final CharacterCommand auxCommand = new CharacterCommand();
	private final GameAssetsManager assetsManager;
	private PathPlanHandler pathPlanHandler;

	public PlayerSystem(SystemsCommonData systemsCommonData, GameAssetsManager assetsManager, SoundPlayer soundPlayer) {
		super(systemsCommonData, soundPlayer);
		this.assetsManager = assetsManager;
	}

	@Override
	public void onUserSelectedNodeToApplyTurn(MapGraphNode node) {
		applyPlayerTurn(node);
	}

	private void applyPlayerTurn(final MapGraphNode cursorNode) {
		int pathSize = pathPlanHandler.getCurrentPath().getCount();
		if (!pathPlanHandler.getCurrentPath().nodes.isEmpty() && pathPlanHandler.getCurrentPath().get(pathSize - 1).equals(cursorNode)) {
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
		pathPlanHandler.displayPathPlan(ComponentsMapper.character.get(player).getSkills().getAgility());
	}

	private boolean calculatePathAccordingToSelection(final MapGraphNode cursorNode) {
		CharacterDecalComponent charDecalComp = ComponentsMapper.characterDecal.get(getSystemsCommonData().getPlayer());
		MapGraphPath plannedPath = pathPlanHandler.getCurrentPath();
		request.setSourceNode(getSystemsCommonData().getMap().getNode(charDecalComp.getNodePosition(auxVector2_1)));
		request.setDestNode(cursorNode);
		request.setOutputPath(plannedPath);
		request.setAvoidCharactersInCalculations(true);
		request.setMaxCostInclusive(MapGraphConnectionCosts.CLEAN);
		return (GeneralUtils.calculatePath(request, pathPlanHandler.getPathFinder(), pathPlanHandler.getHeuristic()));
	}

	private void applyPlayerCommandAccordingToPlan( ) {
		pathPlanHandler.hideAllArrows();
		applyGoToCommand(pathPlanHandler.getCurrentPath());
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
	public void initializeData( ) {
		pathPlanHandler = new PathPlanHandler(assetsManager, getSystemsCommonData().getMap());
		pathPlanHandler.init((PooledEngine) getEngine());

	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		getSystemsCommonData().setPlayer(getEngine().getEntitiesFor(Family.all(PlayerComponent.class).get()).first());
	}

	@Override
	public void dispose( ) {

	}
}
