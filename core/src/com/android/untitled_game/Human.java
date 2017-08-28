package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class Human extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.DESTROYABLE_DYNAMIC;
    }

    protected static float JOINT_BREAK_FORCE = 1000f;
    protected static float JOINT_BREAK_TORQUE = 1000f;

    protected static int BODY_INDEX = 0;
    protected static int HEAD_INDEX = 1;
    protected static int LEFT_ARM_INDEX = 2;
    protected static int RIGHT_ARM_INDEX = 3;
    protected static int LEFT_LEG_INDEX = 4;
    protected static int RIGHT_LEG_INDEX = 5;
    protected static int PARTS_COUNT = 6;

    protected Body[] m_bodies;
    protected Joint[] m_joints;

    protected Body createPart(final Shape partShape) {
        Body result = m_world.createBody(new BodyDef() {{
            type = BodyType.DynamicBody;
            position.x = 8.0f;
            position.y = 8.0f;
        }});

        result.setUserData(this);

        result.createFixture(new FixtureDef() {{
            shape = partShape;
            density = 1;
            restitution = 0.2f;
            friction = 0.8f;
        }});

        partShape.dispose();

        return result;
    }

    protected Joint createJoint(final Body branch, final Body root, final Vector2 anchor, final float lowerAngleDeg, final float upperAngleDeg) {
        return m_world.createJoint(new RevoluteJointDef() {{
            bodyA = branch;
            bodyB = root;
            collideConnected = false;
            localAnchorA.set(anchor);
            localAnchorB.set(anchor);
            enableLimit = true;
            lowerAngle = lowerAngleDeg * (float) Math.PI / 180f;
            upperAngle = upperAngleDeg * (float) Math.PI / 180f;
        }});
    }

    protected Joint tryBreakJoint(Joint joint, float delta) {
        if (joint != null && (
                joint.getReactionForce(1 / delta).len() > JOINT_BREAK_FORCE * m_health / m_totalHealth ||
                        joint.getReactionTorque(1 / delta) > JOINT_BREAK_TORQUE * m_health / m_totalHealth
        )) {
            m_world.destroyJoint(joint);
            return null;
        }
        return joint;
    }

    public Human(World world) {
        super(world, 100f);

        m_bodies = new Body[PARTS_COUNT];
        m_joints = new Joint[PARTS_COUNT];

        m_mainBody = m_bodies[BODY_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.1f, 0.5f, new Vector2(0f, 0.5f), 0);
        }});

        m_joints[HEAD_INDEX] = createJoint(m_bodies[LEFT_ARM_INDEX] = createPart(new CircleShape() {{
            setPosition(new Vector2(0f, 1.1f));
            setRadius(0.1f);
        }}), m_bodies[BODY_INDEX], new Vector2(0, 0.9f), -30f, 30f);

        m_joints[LEFT_ARM_INDEX] = createJoint(m_bodies[LEFT_ARM_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.05f, 0.5f, new Vector2(0f, 0.5f), 0);
        }}), m_bodies[BODY_INDEX], new Vector2(0, 0.9f), -90f, 170f);

        m_joints[RIGHT_ARM_INDEX] = createJoint(m_bodies[RIGHT_ARM_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.05f, 0.5f, new Vector2(0f, 0.5f), 0);
        }}), m_bodies[BODY_INDEX], new Vector2(0, 0.9f), -90f, 170f);

        m_joints[LEFT_LEG_INDEX] = createJoint(m_bodies[LEFT_LEG_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.05f, 0.5f, new Vector2(0f, -0.5f), 0);
        }}), m_bodies[BODY_INDEX], new Vector2(0, 0.0f), -60f, 60f);

        m_joints[RIGHT_LEG_INDEX] = createJoint(m_bodies[RIGHT_LEG_INDEX] = createPart(new PolygonShape() {{
            setAsBox(0.05f, 0.5f, new Vector2(0f, -0.5f), 0);
        }}), m_bodies[BODY_INDEX], new Vector2(0, 0.0f), -60f, 60f);
    }

    @Override
    public GameObject[] update(float delta) {
        GameObject[] result = null;

        for (int i = 0; i < PARTS_COUNT; i++) {
            if (m_joints[i] != null) {
                m_joints[i] = tryBreakJoint(m_joints[i], delta);
                if (m_joints[i] == null) {
                    if (result == null) {
                        result = new GameObject[PARTS_COUNT];
                    }
                    result[i] = new Limb(m_world, m_health, m_bodies[i]);
                    m_bodies[i] = null;
                }
            }
        }

        m_mainBody = m_bodies[BODY_INDEX];

        return result;
    }

    @Override
    public boolean isLive() {
        return super.isLive() && m_joints[HEAD_INDEX] != null;
    }

    @Override
    public boolean isExisting() {
        return m_health > 0;
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
