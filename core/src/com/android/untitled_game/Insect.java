package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;

public class Insect extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.SWARM;
    }

    protected Swarm m_parent;
    protected Vector2 m_target;
    protected float m_maxSpeed = 15f;
    protected float m_maxThrust = 20f;
    protected float m_maxTorque = 4000f;
    protected float m_maxForce = 20f;
    protected float m_phase = (float) Math.random() * (float) Math.PI;

    protected boolean m_colliding = false;
    protected boolean m_collided = false;

    public Insect(final Swarm parent, World world) {
        super(world, 1f,
                world.createBody(new BodyDef() {{
                    type = BodyDef.BodyType.DynamicBody;
                    position.x = parent.position.x;
                    position.y = parent.position.y;
                    gravityScale = 0f;
                }})
        );

        final CircleShape mainShape = new CircleShape() {{
            setRadius(0.01f);
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = mainShape;
            density = 1f;
            restitution = 1f;
            friction = 0f;
            filter.groupIndex = -1;
        }});

        mainShape.dispose();

        m_parent = parent;
        m_target = parent.position;

        m_maxSpeed += ((float) Math.random() - 0.5f) * 0.4f * m_maxSpeed;
        m_maxThrust += ((float) Math.random() - 0.5f) * 0.4f * m_maxThrust;
        m_maxTorque += ((float) Math.random() - 0.5f) * 0.4f * m_maxTorque;
        m_maxForce += ((float) Math.random() - 0.5f) * 0.4f * m_maxForce;
    }

    @Override
    public void beginContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
        if (otherObject.getCollisionType() == CollisionType.WALL) {
            m_colliding = true;
        }
    }

    @Override
    public void preSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, Manifold oldManifold) {
        if (otherObject.getCollisionType() != CollisionType.WALL) {
            Vector2 distance = m_parent.position.cpy().sub(m_mainBody.getWorldCenter());
            Vector2 direction = distance.len() < m_parent.size * 2 ? distance.add(0, m_parent.size).nor() : m_mainBody.getLinearVelocity().cpy().nor();
            float relativeVelocity = Math.max(0, m_maxSpeed - otherFixture.getBody().getLinearVelocity().dot(direction));
            otherFixture.getBody().applyForce(direction.cpy().nor().scl(m_maxForce * relativeVelocity / m_maxSpeed), m_mainBody.getWorldCenter(), true);
            if (otherObject.getCollisionType() == CollisionType.BULLET) {
                m_health = 0f;
            } else {
                otherObject.attack(0.1f);
            }

            contact.setEnabled(false);
        }
    }

    @Override
    public void postSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, ContactImpulse impulse) {
        if (otherObject.getCollisionType() == CollisionType.WALL) {
            WorldManifold manifold = contact.getWorldManifold();
            Vector2 fullImpulse = m_mainBody.getLinearVelocity().cpy().scl(m_mainBody.getMass());
            Vector2 correctedImpulse = fullImpulse.cpy().setAngle(manifold.getNormal().angle() + (fullImpulse.angle() - manifold.getNormal().angle()) / 90f * 60f);
            m_mainBody.applyLinearImpulse(fullImpulse.cpy().scl(-1f), m_mainBody.getWorldCenter(), true);
            m_mainBody.applyLinearImpulse(correctedImpulse, m_mainBody.getWorldCenter(), true);
        }
    }

    @Override
    public void endContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
        if (otherObject.getCollisionType() == CollisionType.WALL) {
            m_colliding = false;
            m_collided = true;
        }
    }

    @Override
    public GameObject[] update(float delta) {
        if (m_colliding) {
            return null;
        }

        if (m_collided) {
            m_mainBody.setTransform(m_mainBody.getPosition(), (float) Math.toRadians(m_mainBody.getLinearVelocity().angle() - 90));
            m_collided = false;
        }

        Vector2 position = m_mainBody.getPosition();
        Vector2 target = m_parent.position;

        if (position.cpy().sub(m_target).len() < 0.01 || position.cpy().sub(target).len() > m_parent.size) {
            m_target = target.cpy().add(new Vector2(((float) Math.random() - 0.5f) * m_parent.size, ((float) Math.random() - 0.5f) * m_parent.size));
        }

        Vector2 currentRightNormal = m_mainBody.getWorldVector(new Vector2(1f, 0f));
        Vector2 lateralVelocity = currentRightNormal.cpy().scl(currentRightNormal.dot(m_mainBody.getLinearVelocity()));

        Vector2 currentForwardNormal = m_mainBody.getWorldVector(new Vector2(0f, 1f));
        Vector2 forwardVelocity = currentForwardNormal.cpy().scl(currentForwardNormal.dot(m_mainBody.getLinearVelocity()));


        m_mainBody.applyLinearImpulse(lateralVelocity.cpy().scl(-m_mainBody.getMass()), m_mainBody.getWorldCenter(), true);
        m_mainBody.applyAngularImpulse(m_mainBody.getInertia() * -m_mainBody.getAngularVelocity(), true);

        float currentSpeed = forwardVelocity.cpy().dot(currentForwardNormal);

        float deltaSpeed = Math.max(0f, Math.min((m_maxSpeed - currentSpeed) * (m_maxSpeed - currentSpeed) * (m_maxSpeed - currentSpeed), 1f));

        m_mainBody.applyForce(currentForwardNormal.cpy().setLength(deltaSpeed * m_maxThrust * m_mainBody.getMass()), m_mainBody.getWorldCenter(), true);


        float currentDistance = Math.abs(m_target.cpy().sub(position).len());
        float currentAngle = currentForwardNormal.angle(m_target.cpy().sub(position)) + 20f * (float) Math.sin((double) currentDistance / m_parent.size / 2 + m_phase);
        if (currentDistance < m_parent.size) {
            m_mainBody.applyTorque(
                    Math.signum(currentAngle / 180f) * (float) Math.sqrt((double) Math.abs(currentAngle / 180f)) *
                            (float) Math.pow((double) Math.min(currentDistance / m_parent.size, 1f), 2.0) *
                            (currentSpeed / m_maxSpeed) *
                            m_maxTorque / m_parent.size * m_mainBody.getInertia(), true);
        } else {
            m_mainBody.applyTorque(
                    Math.signum(currentAngle / 180f) * (float) Math.sqrt((double) Math.abs(currentAngle / 180f)) *
                            (float) Math.pow((double) Math.max(0f, Math.min(1f - (currentDistance - m_parent.size) / m_parent.size, 1f)) / 2f + 0.5f, 2.0) *
                            (currentSpeed / m_maxSpeed) *
                            m_maxTorque / m_parent.size * m_mainBody.getInertia(), true);
        }

        return null;
    }

    @Override
    public boolean isExisting() {
        return super.isLive();
    }
}
