package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Swarm extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.SWARM;
    }

    public Vector2 position;
    public float size = 2f;

    protected Insect[] m_insects;

    public Swarm(World world, final Vector2 pasition) {
        super(world, 0, world.createBody(new BodyDef() {{
            type = BodyType.StaticBody;
            position.x = pasition.x;
            position.y = pasition.y;
            gravityScale = 0f;
        }}));

        final CircleShape mainShape = new CircleShape() {{
            setRadius(size);
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = mainShape;
            density = 0f;
            restitution = 0f;
            friction = 0f;
            filter.categoryBits = 0;
        }});


        mainShape.dispose();

        position = pasition;

        m_insects = new Insect[40];

        for (int i = 0; i < m_insects.length; i++) {
            m_insects[i] = new Insect(this, world);
        }
    }

    @Override
    public GameObject[] update(float delta) {
        for (int i = 0; i < m_insects.length; i++) {
            if (m_insects[i] != null) {
                m_insects[i].update(delta);
                if (!m_insects[i].isExisting()) {
                    m_insects[i].dispose();
                    m_insects[i] = null;
                }
            }
        }

        if (size > 0.5) {
            size *= 1 - 0.2 * delta;
        }

        m_mainBody.setTransform(position, 0);

        m_mainBody.destroyFixture(m_mainBody.getFixtureList().get(0));


        final CircleShape mainShape = new CircleShape() {{
            setRadius(size);
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = mainShape;
            density = 0f;
            restitution = 0f;
            friction = 0f;
            filter.categoryBits = 0;
        }});


        mainShape.dispose();

        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        for (int i = 0; i < m_insects.length; i++) {
            if (m_insects[i] != null) {
                m_insects[i].dispose();
            }
        }
    }
}
