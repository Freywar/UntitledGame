package com.android.untitled_game;

import com.badlogic.gdx.physics.box2d.World;

public class GatlingGun extends Turret {
    public GatlingGun(World world) {
        super(world);

        applySettings(new TurretSettings() {{
            RECHARGE_TIME = 0.1f;
            RELOAD_TIME = 4f;
            MAX_BULLETS_COUNT = 100;
            MAX_BARREL_ANGULAR_VELOCITY = 4f;
            ANGLE_TOLERANCE = 4f;
            BULLET_RADIUS = 0.1f;
        }});
    }
}
