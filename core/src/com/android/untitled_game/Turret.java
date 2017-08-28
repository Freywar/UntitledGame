package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
echo "# UntitledGame" >> README.md
        git init
        git add README.md
        git commit -m "first commit"
        git remote add origin https://github.com/Freywar/UntitledGame.git
        git push -u origin master

public abstract class Turret extends GameObject {
    protected class TurretSettings {
        protected float RECHARGE_TIME = 0.1f;
        protected float RELOAD_TIME = 2f;
        protected float RESET_TARGETING_TIME = 2f;
        protected int MAX_BULLETS_COUNT = 100;
        protected float MAX_BARREL_ANGULAR_VELOCITY = 2f;
        protected float ANGLE_TOLERANCE = 2f;
        protected float BULLET_RADIUS = 0.3f;
        protected float BULLET_SPREAD_ANGLE = 10f;

        protected float DAMAGE_VELOCITY = 1.4f;
        protected float BREAK_VELOCITY = 7f;
        protected float[] JOINT_BREAK_FORCES = new float[]{0f, 300f, 100f, 0f};
        protected float[] JOINT_BREAK_TORQUES = new float[]{1000f, 1000f, 1000f, 0f};
    }

    public CollisionType getCollisionType() {
        return CollisionType.DESTROYABLE_DYNAMIC;
    }

    protected enum LoadingState {READY, RECHARGING, RELOADING}

    protected enum TargetingState {IDLE, WAITING, LOCKED}

    protected static int BASE_INDEX = 0;
    protected static int BODY_INDEX = 1;
    protected static int BARREL_INDEX = 2;
    protected static int PARTS_COUNT = 4;

    protected TurretSettings m_settings;

    protected Body[] m_bodies;
    protected Joint[] m_joints;

    protected LoadingState m_loadingState = LoadingState.READY;
    protected float m_timeSinceLastLoadingStateChange = 0;
    protected TargetingState m_targetingState = TargetingState.IDLE;
    protected float m_timeSinceLastTargetingStateChange = 0;
    protected Body[] m_trackedBodies = new Body[100];
    protected float m_targetAngle = 90;
    protected int m_remainingBulletsCount = 0;

    protected Body createPart(final Shape partShape, final BodyDef.BodyType partType) {
        Body result = m_world.createBody(new BodyDef() {{
            type = partType;
            position.x = 12.0f;
            position.y = 2.0f;
        }});

        result.setUserData(this);

        result.createFixture(new FixtureDef() {{
            shape = partShape;
            density = 10;
            restitution = 0.3f;
            friction = 0.8f;
        }});

        partShape.dispose();

        return result;
    }

    protected Joint createJoint(final Body branch, final Body root, final Vector2 anchor, final float lowerAngleDeg, final float upperAngleDeg, final boolean collidable) {
        return m_world.createJoint(new RevoluteJointDef() {{
            bodyA = branch;
            bodyB = root;
            collideConnected = collidable;
            localAnchorA.set(anchor);
            localAnchorB.set(anchor);
            enableLimit = true;
            lowerAngle = lowerAngleDeg * (float) Math.PI / 180f;
            upperAngle = upperAngleDeg * (float) Math.PI / 180f;
        }});
    }

    protected void tryBreakJoint(int index, float delta) {
        if (m_joints[index] != null && (
                m_joints[index].getReactionForce(1 / delta).len() > m_settings.JOINT_BREAK_FORCES[index] * m_health / m_totalHealth ||
                        m_joints[index].getReactionTorque(1 / delta) > m_settings.JOINT_BREAK_TORQUES[index] * m_health / m_totalHealth
        )) {
            m_world.destroyJoint(m_joints[index]);
            m_joints[index] = null;
        }
    }

    public Turret(World world) {
        super(world, 100f);

        m_bodies = new Body[PARTS_COUNT];
        m_joints = new Joint[PARTS_COUNT];

        m_mainBody = m_bodies[BASE_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.5f, 0.01f);
        }}, BodyDef.BodyType.StaticBody);

        m_joints[BODY_INDEX] = createJoint(m_bodies[BODY_INDEX] = createPart(new PolygonShape() {{
            set(new Vector2[]{
                    new Vector2(-0.5f, 0.01f),
                    new Vector2(-0.4f, 0.4f),
                    new Vector2(0.4f, 0.4f),
                    new Vector2(0.5f, 0.01f)
            });
        }}, BodyDef.BodyType.DynamicBody), m_bodies[BASE_INDEX], new Vector2(0, 0.01f), 0, 0, true);


        final CircleShape sensorShape = new CircleShape() {{
            setRadius(4f);
        }};
        m_bodies[BODY_INDEX].createFixture(new FixtureDef() {{
            isSensor = true;
            shape = sensorShape;
        }});
        sensorShape.dispose();

        m_joints[BARREL_INDEX] = createJoint(m_bodies[BARREL_INDEX] = createPart(new PolygonShape() {{
            set(new Vector2[]{
                    new Vector2(-0.01f, 0.4f),
                    new Vector2(-0.01f, 0.8f),
                    new Vector2(0.01f, 0.8f),
                    new Vector2(0.01f, 0.4f)
            });
        }}, BodyDef.BodyType.DynamicBody), m_bodies[BODY_INDEX], new Vector2(0, 0.2f), -90, 90, false);

        if (m_settings != null) {
            applySettings(m_settings);
        }
    }

    protected void applySettings(TurretSettings settings) {
        m_settings = settings;

        m_remainingBulletsCount = m_settings.MAX_BULLETS_COUNT;

        ((RevoluteJoint) m_joints[BARREL_INDEX]).enableMotor(true);
        ((RevoluteJoint) m_joints[BARREL_INDEX]).setMaxMotorTorque(m_settings.JOINT_BREAK_TORQUES[BARREL_INDEX]);
        ((RevoluteJoint) m_joints[BARREL_INDEX]).setMotorSpeed(0f);
    }

    @Override
    public void beginContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
        if (selfFixture.isSensor() && otherObject.getCollisionType() == CollisionType.SWARM) {
            for (int i = 0; i < m_trackedBodies.length; i++) {
                if (m_trackedBodies[i] == null) {
                    m_trackedBodies[i] = otherFixture.getBody();
                    break;
                }
            }
        }
    }

    @Override
    public void postSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, ContactImpulse impulse) {
         /*float velocity = impulse.getNormalImpulses()[0] / m_mainBody.getMass();
        if (velocity > m_settings.DAMAGE_VELOCITY * m_health / m_totalHealth) {
            m_isLive = false;
        } else if (velocity > m_settings.BREAK_VELOCITY) {
            m_health -= 1f;
        }*/
    }

    @Override
    public void endContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
        if (selfFixture.isSensor() && otherObject.getCollisionType() == CollisionType.SWARM) {
            for (int i = 0; i < m_trackedBodies.length; i++) {
                if (m_trackedBodies[i] == otherFixture.getBody()) {
                    m_trackedBodies[i] = null;
                    break;
                }
            }
        }
    }

    @Override
    public GameObject[] update(float delta) {
        GameObject[] result = null;

        for (int i = 0; i < PARTS_COUNT; i++) {
            if (m_joints[i] != null) {
                tryBreakJoint(i, delta);
                if (m_joints[i] == null) {
                    if (result == null) {
                        result = new GameObject[PARTS_COUNT];
                    }
                    result[i] = new Fragment(m_world, m_health, m_bodies[i], m_settings.DAMAGE_VELOCITY, m_settings.BREAK_VELOCITY);
                    m_bodies[i] = null;
                }
            }
        }

        m_mainBody = m_bodies[BASE_INDEX];

        if (!isLive()) {
            return result;
        } else {
            m_timeSinceLastTargetingStateChange += delta;
            m_timeSinceLastLoadingStateChange += delta;

            float currentAngle = 90 - ((RevoluteJoint) m_joints[BARREL_INDEX]).getJointAngle() * 180f / (float) Math.PI;

            Vector2 target = new Vector2();
            int count = 0;
            for (int i = 0; i < m_trackedBodies.length; i++) {
                if (m_trackedBodies[i] != null) {
                    target.add(m_trackedBodies[i].getWorldCenter());
                    count++;
                }
            }

            if (count != 0) {
                m_targetingState = TargetingState.LOCKED;
                m_timeSinceLastTargetingStateChange = 0;
            } else if (m_targetingState == TargetingState.LOCKED) {
                m_targetingState = TargetingState.WAITING;
                m_timeSinceLastTargetingStateChange = 0;
            }

            switch (m_targetingState) {
                case IDLE:
                    if (Math.abs(m_targetAngle - currentAngle) > m_settings.ANGLE_TOLERANCE) {
                        m_timeSinceLastTargetingStateChange = 0;
                    } else if (m_timeSinceLastTargetingStateChange >= m_settings.RESET_TARGETING_TIME) {
                        m_targetAngle = (float) Math.random() * 180f;
                        m_timeSinceLastTargetingStateChange = 0;
                    }
                    break;
                case WAITING:
                    if (m_timeSinceLastTargetingStateChange >= m_settings.RESET_TARGETING_TIME) {
                        m_targetingState = TargetingState.IDLE;
                        m_targetAngle = (float) Math.random() * 180f;
                        m_timeSinceLastTargetingStateChange = 0;
                    }
                    break;
                case LOCKED:
                    m_targetAngle = target.scl(1f / count).sub(m_bodies[BODY_INDEX].getWorldCenter()).angle();
                    break;
            }

            if (currentAngle - m_targetAngle < -m_settings.ANGLE_TOLERANCE) {
                ((RevoluteJoint) m_joints[BARREL_INDEX]).setMotorSpeed(-m_settings.MAX_BARREL_ANGULAR_VELOCITY);
            } else if (currentAngle - m_targetAngle > m_settings.ANGLE_TOLERANCE) {
                ((RevoluteJoint) m_joints[BARREL_INDEX]).setMotorSpeed(m_settings.MAX_BARREL_ANGULAR_VELOCITY);
            } else {
                ((RevoluteJoint) m_joints[BARREL_INDEX]).setMotorSpeed(0);
            }

            boolean hasShot = false;
            switch (m_loadingState) {
                case READY:
                    if (m_targetingState == TargetingState.LOCKED && Math.abs(m_targetAngle - currentAngle) > m_settings.ANGLE_TOLERANCE) {
                        hasShot = true;
                        m_remainingBulletsCount -= 1;
                        if (m_remainingBulletsCount == 0) {
                            m_loadingState = LoadingState.RELOADING;
                            m_timeSinceLastLoadingStateChange = 0;
                        } else {
                            m_loadingState = LoadingState.RECHARGING;
                            m_timeSinceLastLoadingStateChange = 0;
                        }
                    }
                    break;

                case RECHARGING:
                    if (m_timeSinceLastLoadingStateChange >= m_settings.RECHARGE_TIME) {
                        m_loadingState = LoadingState.READY;
                        m_timeSinceLastLoadingStateChange = 0;
                    }
                    break;

                case RELOADING:
                    if (m_timeSinceLastLoadingStateChange >= m_settings.RELOAD_TIME) {
                        m_loadingState = LoadingState.READY;
                        m_remainingBulletsCount = m_settings.MAX_BULLETS_COUNT;
                        m_timeSinceLastLoadingStateChange = 0;
                    }
                    break;
            }

            Vector2 barrelTip = m_bodies[BARREL_INDEX].getWorldVector(new Vector2(0f, 1f)),
                    barrelTipPosition = m_bodies[BARREL_INDEX].getWorldCenter().add(m_bodies[BARREL_INDEX].getWorldVector(new Vector2(0f, 1f))),
                    barrelTipDirection = barrelTip.cpy().nor();

            return hasShot ?
                    new GameObject[]{
                            new Bullet(m_world, barrelTipPosition,
                                    barrelTipDirection.setAngle(barrelTipDirection.angle() + (float) (Math.random() - 0.5) * m_settings.BULLET_SPREAD_ANGLE).scl(20), m_settings.BULLET_RADIUS)
                    } :
                    null;
        }
    }

    @Override
    public boolean isLive() {
        return m_joints[BODY_INDEX] != null && m_joints[BARREL_INDEX] != null && super.isLive();
    }

    @Override
    public void dispose() {
        for (int i = 0; i < PARTS_COUNT; i++) {
            if (m_joints[i] != null) {
                m_world.destroyJoint(m_joints[i]);
            }
            if (m_bodies[i] != null) {
                m_world.destroyBody(m_bodies[i]);
            }
        }
    }
}
