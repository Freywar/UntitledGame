package com.android.untitled_game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

public abstract class GameObject {
    public enum CollisionType {
        NONE(0),
        WALL(1 << 0),
        DESTROYABLE_STATIC(1 << 1),
        DESTROYABLE_DYNAMIC(1 << 2),
        BULLET(1 << 3),
        SWARM(1 << 4);

        public final int bit;

        CollisionType(int bit) {
            this.bit = bit;
        }
    }

    public CollisionType getCollisionType() {
        return CollisionType.NONE;
    }

    protected World m_world;
    protected Body m_mainBody;
    protected float m_health = 100f;
    protected float m_totalHealth = m_health;

    public GameObject(World world, float health) {
        m_world = world;
        m_health = m_totalHealth = health;
    }

    public GameObject(World world, float health, Body mainBody) {
        m_world = world;
        m_health = m_totalHealth = health;
        m_mainBody = mainBody;
        m_mainBody.setUserData(this);
    }

    public void beginContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
    }

    public void preSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, Manifold oldManifold) {
    }

    public void postSolve(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact, ContactImpulse impulse) {
    }

    public void endContact(Fixture selfFixture, Fixture otherFixture, GameObject otherObject, Contact contact) {
    }

    public GameObject[] update(float delta) {
        return null;
    }

    public void attack(float value) {
        if (m_health > 0) {
            m_health -= value;
        }
    }

    public boolean isLive() {
        return m_health > 0;
    }

    public boolean isExisting() {
        return m_mainBody != null && m_mainBody.getWorldCenter().len() < 1000f;
    }

    public void dispose() {
        if (m_mainBody != null) {
            m_world.destroyBody(m_mainBody);
        }
    }
}
