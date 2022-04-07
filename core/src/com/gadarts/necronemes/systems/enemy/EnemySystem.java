package com.gadarts.necronemes.systems.enemy;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.FlowerSkillIconComponent;
import com.gadarts.necronemes.components.enemy.EnemyComponent;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.utils.TimeUtils.*;
import static com.gadarts.necromine.assets.Assets.*;
import static com.gadarts.necronemes.components.ComponentsMapper.*;
import static com.gadarts.necronemes.systems.enemy.EnemyAiStatus.*;

public class EnemySystem extends GameSystem<EnemySystemEventsSubscriber> {
	public static final float SKILL_FLOWER_HEIGHT_RELATIVE = 1F;
	private static final int ICON_DURATION = 2;
	private static final float ICON_SPEED = 0.5F;
	private static final long AMB_SOUND_INTERVAL_MIN = 10L;
	private static final long AMB_SOUND_INTERVAL_MAX = 50L;
	private final List<Entity> iconsToRemove = new ArrayList<>();
	private final List<Sounds> ambSounds = List.of(Sounds.AMB_CHAINS, Sounds.AMB_SIGH, Sounds.AMB_LAUGH);
	private ImmutableArray<Entity> enemies;
	private ImmutableArray<Entity> icons;
	private long nextAmbSoundTime;

	public EnemySystem(SystemsCommonData systemsCommonData, SoundPlayer soundPlayer, GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
	}

	@Override
	public void addedToEngine(Engine engine) {
		super.addedToEngine(engine);
		enemies = engine.getEntitiesFor(Family.all(EnemyComponent.class).get());
		icons = engine.getEntitiesFor(Family.all(FlowerSkillIconComponent.class).get());
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		handleRoamSounds();
		handleFlowerSkills(deltaTime);
		if (millis() > nextAmbSoundTime) {
			getSoundPlayer().playSound(ambSounds.get(MathUtils.random(0, ambSounds.size() - 1)));
			resetNextAmbSound();
		}
	}

	private void handleFlowerSkills(float deltaTime) {
		iconsToRemove.clear();
		for (Entity flowerIcon : icons) {
			if (timeSinceMillis(flowerSkillIcon.get(flowerIcon).getTimeOfCreation()) >= ICON_DURATION * 1000F) {
				iconsToRemove.add(flowerIcon);
			} else {
				simpleDecal.get(flowerIcon).getDecal().getPosition().add(0, deltaTime * ICON_SPEED, 0);
			}
		}
		for (Entity icon : iconsToRemove) {
			getEngine().removeEntity(icon);
		}
	}

	private void handleRoamSounds() {
		for (Entity enemy : enemies) {
			EnemyComponent enemyComp = ComponentsMapper.enemy.get(enemy);
			if (enemyComp.getAiStatus() != IDLE && timeSinceMillis(enemyComp.getNextRoamSound()) >= 0) {
				if (enemyComp.getNextRoamSound() != 0) {
					getSoundPlayer().playSound(enemyComp.getEnemyDefinition().getRoamSound());
				}
				enemyComp.calculateNextRoamSound();
			}
		}
	}

	private void resetNextAmbSound() {
		nextAmbSoundTime = millis() + MathUtils.random(AMB_SOUND_INTERVAL_MIN, AMB_SOUND_INTERVAL_MAX) * 1000L;
	}

	@Override
	public Class<EnemySystemEventsSubscriber> getEventsSubscriberClass() {
		return EnemySystemEventsSubscriber.class;
	}

	@Override
	public void initializeData() {

	}

	@Override
	public void dispose() {

	}
}
