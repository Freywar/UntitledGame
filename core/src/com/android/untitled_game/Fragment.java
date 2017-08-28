package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Fragment extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.DESTROYABLE_DYNAMIC;
    }

    protected static float m_damageVelocity = 1.4f;
    protected static float m_breakVelocity = 7f;

    protected boolean m_isLive = true;

    public Fragment(World world, float health, Body body, float damageVelocity, float breakVelocity) {
        super(world, health, body);
        m_damageVelocity = damageVelocity;
        m_breakVelocity = breakVelocity;
    }

    @Override
    public void postSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, ContactImpulse impulse) {
        float velocity = impulse.getNormalImpulses()[0] / m_mainBody.getMass();
        if (velocity > m_breakVelocity * m_health / m_totalHealth) {
            m_isLive = false;
        } else if (velocity > m_damageVelocity) {
            m_health -= 1f;
        }
    }

    public GameObject[] update(float delta) {
        if (!m_isLive && m_mainBody.getMass() / (m_totalHealth / 100) > 0.25) {
            return create(m_world, m_mainBody, m_health, m_damageVelocity, m_breakVelocity);
        }
        return null;
    }

    public static Fragment[] create(World world, final Body originalBody, float health, float damageVelocity, float breakVelocity) {
        final Fixture originalFixture = originalBody.getFixtureList().get(0);
        final PolygonShape originalShape = (PolygonShape) originalFixture.getShape();
        Vector2[] points = new Vector2[originalShape.getVertexCount()];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector2();
            originalShape.getVertex(i, points[i]);
        }

        int pointsCountOne = points.length / 2;
        final Vector2[] pointsOne = new Vector2[pointsCountOne + 2];
        int pointsCountTwo = points.length - pointsCountOne;
        final Vector2[] pointsTwo = new Vector2[pointsCountTwo + 2];

        for (int i = 0; i < pointsCountOne; i++) {
            pointsOne[i] = points[i];
        }
        for (int i = 0; i < pointsCountTwo; i++) {
            pointsTwo[i] = points[i + pointsCountOne];
        }

        double splitOne = 0.333 + Math.random() / 3;
        pointsOne[pointsCountOne] = pointsTwo[pointsTwo.length - 1] = points[pointsCountOne - 1].cpy().add(points[pointsCountOne].cpy().sub(points[pointsCountOne - 1]).scl((float) splitOne));
        double splitTwo = 0.333 + Math.random() / 3;
        pointsOne[pointsCountOne + 1] = pointsTwo[pointsTwo.length - 2] = points[points.length - 1].cpy().add(points[0].cpy().sub(points[points.length - 1]).scl((float) splitTwo));

        Body bodyOne = world.createBody(new BodyDef() {{
            type = BodyType.DynamicBody;
            position.x = originalBody.getPosition().x;
            position.y = originalBody.getPosition().y;
            angle = originalBody.getAngle();
            linearVelocity.x = originalBody.getLinearVelocity().x * 0.5f;
            linearVelocity.y = originalBody.getLinearVelocity().y * 0.5f;
            angularVelocity = originalBody.getAngularVelocity() * 0.5f;
        }});

        final PolygonShape shapeOne = new PolygonShape() {{
            set(pointsOne);
        }};

        bodyOne.createFixture(new FixtureDef() {{
            shape = shapeOne;
            density = originalFixture.getDensity();
            restitution = originalFixture.getRestitution();
            friction = originalFixture.getFriction();
        }});

        shapeOne.dispose();

        Body bodyTwo = originalBody.getWorld().createBody(new BodyDef() {{
            type = BodyType.DynamicBody;
            position.x = originalBody.getPosition().x;
            position.y = originalBody.getPosition().y;
            angle = originalBody.getAngle();
            linearVelocity.x = originalBody.getLinearVelocity().x * 0.5f;
            linearVelocity.y = originalBody.getLinearVelocity().y * 0.5f;
            angularVelocity = originalBody.getAngularVelocity() * 0.5f;
        }});

        final PolygonShape shapeTwo = new PolygonShape() {{
            set(pointsTwo);
        }};

        bodyTwo.createFixture(new FixtureDef() {{
            shape = shapeTwo;
            density = originalFixture.getDensity();
            restitution = originalFixture.getRestitution();
            friction = originalFixture.getFriction();
        }});

        shapeTwo.dispose();

        return new Fragment[]{
                new Fragment(world, health, bodyOne, damageVelocity, breakVelocity),
                new Fragment(world, health, bodyTwo, damageVelocity, breakVelocity)
        };
    }

    @Override
    public void attack(float value) {
        m_health -= value;
        if (m_health < m_totalHealth / 3) {
            m_isLive = false;
        }
    }

    @Override
    public boolean isLive() {
        return m_isLive;
    }

    @Override
    public boolean isExisting() {
        return m_isLive;
    }
}
