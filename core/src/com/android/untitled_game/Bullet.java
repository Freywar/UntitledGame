package com.android.untitled_game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Bullet extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.BULLET;
    }

    public Bullet(World world, final Vector2 pasition, final Vector2 vilocity, final float radius) {
        super(world, 0,
                world.createBody(new BodyDef() {{
                    type = BodyType.KinematicBody;
                    position.x = pasition.x;
                    position.y = pasition.y;
                    bullet = true;
                    linearVelocity.x = vilocity.x;
                    linearVelocity.y = vilocity.y;
                }})
        );

        final CircleShape bulletShape = new CircleShape() {{
            setRadius(radius);
            setPosition(new Vector2(0f, 0f));
        }};

        m_mainBody.createFixture(new FixtureDef() {{
            shape = bulletShape;
            density = 10;
            restitution = 0.3f;
            friction = 0.8f;
        }});

        bulletShape.dispose();
    }

    @Override
    public boolean isLive() {
        return true;
    }
}
