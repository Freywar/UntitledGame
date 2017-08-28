package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Wall extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.WALL;
    }

    public Wall(World world, final Vector2 pasition, final Vector2 size) {
        super(world, 0,
                world.createBody(new BodyDef() {{
                    type = BodyType.StaticBody;
                    position.set(pasition);
                }})
        );

        final PolygonShape mainShape = new PolygonShape() {{
            setAsBox(size.x, size.y);
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = mainShape;
        }});

        mainShape.dispose();
    }

    @Override
    public void attack(float value) {
    }

    @Override
    public boolean isLive() {
        return true;
    }
}
