package com.android.untitled_game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public class Limb extends GameObject {
    public CollisionType getCollisionType() {
        return CollisionType.DESTROYABLE_DYNAMIC;
    }

    public Limb(World world, float health, Body body) {
        super(world, health, body);
    }
}
