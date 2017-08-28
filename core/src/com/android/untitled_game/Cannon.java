package com.android.untitled_game;

import com.badlogic.gdx.physics.box2d.World;

public class Cannon extends Turret {
    public Cannon(World world) {
        super(world);

        applySettings(new TurretSettings() {{
            RELOAD_TIME = 1f;
            MAX_BULLETS_COUNT = 1;
            BULLET_SPREAD_ANGLE = 1f;
        }});
    }
}
