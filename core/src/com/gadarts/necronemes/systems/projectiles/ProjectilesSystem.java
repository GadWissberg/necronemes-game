package com.gadarts.necronemes.systems.projectiles;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necromine.model.characters.attributes.Accuracy;
import com.gadarts.necromine.model.map.MapNodesTypes;
import com.gadarts.necronemes.SoundPlayer;
import com.gadarts.necronemes.components.BulletComponent;
import com.gadarts.necronemes.components.ComponentsMapper;
import com.gadarts.necronemes.components.character.CharacterComponent;
import com.gadarts.necronemes.components.collision.CollisionComponent;
import com.gadarts.necronemes.components.enemy.EnemyComponent;
import com.gadarts.necronemes.map.MapGraphNode;
import com.gadarts.necronemes.systems.GameSystem;
import com.gadarts.necronemes.systems.SystemsCommonData;
import com.gadarts.necronemes.systems.character.CharacterSystemEventsSubscriber;
import com.gadarts.necronemes.utils.EntityBuilder;

import static com.badlogic.gdx.math.Vector3.Zero;

public class ProjectilesSystem extends GameSystem<ProjectilesSystemEventsSubscriber> implements CharacterSystemEventsSubscriber {
	private static final Vector3 auxVector3_1 = new Vector3();
	private final static float BULLET_SPEED = 0.2f;
	private static final Vector2 auxVector2_1 = new Vector2();
	private final static float BULLET_MAX_DISTANCE = 14;
	private ImmutableArray<Entity> bullets;
	private ImmutableArray<Entity> collidables;

	public ProjectilesSystem(SystemsCommonData systemsCommonData,
							 SoundPlayer soundPlayer,
							 GameAssetsManager assetsManager) {
		super(systemsCommonData, soundPlayer, assetsManager);
	}

	@Override
	public void onCharacterEngagesPrimaryAttack(final Entity character,
												final Vector3 direction,
												final Vector3 charPos) {

		if (ComponentsMapper.enemy.has(character)) {
			enemyEngagesPrimaryAttack(character, direction, charPos);
		}
	}

	private void enemyEngagesPrimaryAttack(final Entity character, final Vector3 direction, final Vector3 charPos) {
		EnemyComponent enemyComponent = ComponentsMapper.enemy.get(character);
		Accuracy accuracy = enemyComponent.getEnemyDefinition().getAccuracy()[enemyComponent.getSkill() - 1];
		direction.rotate(Vector3.Y, MathUtils.random(-accuracy.getMaxAngle(), accuracy.getMaxAngle()));
		direction.rotate(Vector3.X, MathUtils.random(-accuracy.getMaxAngle(), accuracy.getMaxAngle()));
		EnemyComponent enemyComp = ComponentsMapper.enemy.get(character);
		Animation<TextureAtlas.AtlasRegion> bulletAnim = enemyComp.getBulletAnimation();
		getSoundPlayer().playSound(Assets.Sounds.ATTACK_ENERGY_BALL);
		createEnemyBullet(character, direction, charPos, enemyComp, bulletAnim);
	}

	private void createEnemyBullet(final Entity character,
								   final Vector3 direction,
								   final Vector3 charPos,
								   final EnemyComponent enemyComp,
								   final Animation<TextureAtlas.AtlasRegion> bulletAnim) {
		charPos.y += ComponentsMapper.enemy.get(character).getEnemyDefinition().getHeight() / 2F;
		Integer[] damagePoints = enemyComp.getEnemyDefinition().getPrimaryAttack().getDamagePoints();
		EntityBuilder.beginBuildingEntity((PooledEngine) getEngine())
				.addBulletComponent(charPos, direction, character, damagePoints[enemyComp.getSkill() - 1])
				.addAnimationComponent(enemyComp.getEnemyDefinition().getPrimaryAttack().getFrameDuration(), bulletAnim)
				.addSimpleDecalComponent(charPos, bulletAnim.getKeyFrames()[0], Zero.setZero(), true, true)
				.finishAndAddToEngine();
	}

	@Override
	public void addedToEngine(Engine engine) {
		bullets = engine.getEntitiesFor(Family.all(BulletComponent.class).get());
		collidables = engine.getEntitiesFor(Family.all(CollisionComponent.class).get());
	}

	private boolean handleCollisionsWithWalls(final Entity bullet) {
		Vector3 position = ComponentsMapper.simpleDecal.get(bullet).getDecal().getPosition();
		MapGraphNode node = getSystemsCommonData().getMap().getNode(position);
		MapNodesTypes nodeType = node.getType();
		if (nodeType != MapNodesTypes.PASSABLE_NODE || node.getHeight() >= position.y) {
			onCollisionWithWall(bullet, node);
			return true;
		}
		return false;
	}

	private void onCollisionWithWall(final Entity bullet, final MapGraphNode node) {
		for (ProjectilesSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onBulletCollisionWithWall(bullet, node);
		}
		destroyBullet(bullet);
	}

	private void destroyBullet(final Entity bullet) {
		bullet.remove(BulletComponent.class);
		getEngine().removeEntity(bullet);
	}

	private boolean checkCollisionWithCharacter(final Decal decal, final Entity collidable) {
		Vector3 colPos = ComponentsMapper.characterDecal.get(collidable).getDecal().getPosition();
		boolean alive = ComponentsMapper.character.get(collidable).getSkills().getHealthData().getHp() > 0;
		float distance = auxVector2_1.set(colPos.x, colPos.z).dst(decal.getPosition().x, decal.getPosition().z);
		return alive && distance < CharacterComponent.CHAR_RAD;
	}

	private boolean checkCollision(final Decal decal, final Entity collidable) {
		if (ComponentsMapper.characterDecal.has(collidable)) {
			return checkCollisionWithCharacter(decal, collidable);
		}
		return false;
	}

	private void handleCollisionsWithOtherEntities(final Decal decal, final Entity bullet) {
		for (Entity collidable : collidables) {
			if (ComponentsMapper.bullet.get(bullet).getOwner() != collidable) {
				if (checkCollision(decal, collidable)) {
					onProjectileCollisionWithAnotherEntity(bullet, collidable);
					break;
				}
			}
		}
	}

	private void onProjectileCollisionWithAnotherEntity(final Entity bullet, final Entity collidable) {
		for (ProjectilesSystemEventsSubscriber subscriber : subscribers) {
			subscriber.onProjectileCollisionWithAnotherEntity(bullet, collidable);
		}
		destroyBullet(bullet);
	}

	private void handleCollisions(final Decal decal, final Entity bullet) {
		if (!handleCollisionsWithWalls(bullet)) {
			handleCollisionsWithOtherEntities(decal, bullet);
		}
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		for (Entity bullet : bullets) {
			Decal decal = ComponentsMapper.simpleDecal.get(bullet).getDecal();
			BulletComponent bulletComponent = ComponentsMapper.bullet.get(bullet);
			handleCollisions(decal, bullet);
			handleBulletMovement(decal, bulletComponent);
			handleBulletMaxDistance(bullet, decal, bulletComponent);
		}
	}

	private void handleBulletMaxDistance(final Entity bullet, final Decal decal, final BulletComponent bulletComponent) {
		Vector3 position = decal.getPosition();
		float dst = bulletComponent.getInitialPosition(auxVector2_1).dst(position.x, position.z);
		if (dst >= BULLET_MAX_DISTANCE) {
			destroyBullet(bullet);
		}
	}

	private void handleBulletMovement(final Decal decal, final BulletComponent bulletComponent) {
		Vector3 velocity = bulletComponent.getDirection(auxVector3_1).nor().scl(BULLET_SPEED);
		decal.translate(velocity.x, 0, velocity.z);
	}

	@Override
	public Class<ProjectilesSystemEventsSubscriber> getEventsSubscriberClass( ) {
		return ProjectilesSystemEventsSubscriber.class;
	}

	@Override
	public void initializeData( ) {

	}

	@Override
	public void dispose( ) {

	}

}
