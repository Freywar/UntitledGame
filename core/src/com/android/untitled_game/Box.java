package com.android.untitled_game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Box extends Fragment {
    public GameObject.CollisionType getCollisionType() {
        return GameObject.CollisionType.DESTROYABLE_DYNAMIC;
    }

    protected static float DAMAGE_VELOCITY = 1.4f;
    protected static float BREAK_VELOCITY = 7f;

    protected boolean m_isLive = true;

    public Box(World world, float health) {
        super(world, health,
                world.createBody(new BodyDef() {{
                    type = BodyType.DynamicBody;
                    position.x = 2.0f;
                    position.y = 6.0f;
                }}),
                DAMAGE_VELOCITY, BREAK_VELOCITY
        );

        final PolygonShape mainShape = new PolygonShape() {{
            setAsBox(0.5f, 0.5f);
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = mainShape;
            density = m_health / 100;
            restitution = 0.3f;
            friction = 0.8f;
        }});

        mainShape.dispose();
    }

    public Box(World world, float health, Body body) {
        super(world, health, body, DAMAGE_VELOCITY, BREAK_VELOCITY);
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
