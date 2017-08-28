package com.android.untitled_game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.Locale;


public class Game implements ApplicationListener, InputProcessor {
    protected SpriteBatch m_batch;
    protected BitmapFont m_font;
    protected World m_world;
    protected Swarm m_swarm;
    protected GameObject[] m_gameObjects;
    protected Box2DDebugRenderer m_debugRenderer;

    protected static final float PPM = 1080f / 13f;

    @Override
    public void create() {
        m_font = new BitmapFont();
        m_font.setColor(Color.RED);
        m_font.getData().setScale(3);

        m_batch = new SpriteBatch();

        m_world = new World(new Vector2(0, -9.8f), true);

        m_swarm = new Swarm(m_world, new Vector2(8f, 8f));
        m_gameObjects = new GameObject[100];
        //m_gameObjects[0] = new Box(m_world, 100f);
        //m_gameObjects[1] = new Box(m_world, 1000f);
        //m_gameObjects[2] = new Human(m_world);
        //m_gameObjects[3] = new Cannon(m_world);
        m_gameObjects[4] = new GatlingGun(m_world);

        float hs = 5 / PPM;
        float l = 0 / PPM;
        float r = Gdx.graphics.getWidth() / PPM;
        float cx = (l + r) / 2;
        float t = 0 / PPM;
        float b = Gdx.graphics.getHeight() / PPM;
        float cy = (t + b) / 2;

        m_gameObjects[24] = new Wall(m_world, new Vector2(l + hs, cy), new Vector2(hs, cy));
        m_gameObjects[25] = new Wall(m_world, new Vector2(cx, t + hs), new Vector2(cx, hs));
        m_gameObjects[26] = new Wall(m_world, new Vector2(r - hs, cy), new Vector2(hs, cy));
        m_gameObjects[27] = new Wall(m_world, new Vector2(cx, b - hs), new Vector2(cx, hs));

        m_debugRenderer = new Box2DDebugRenderer();

        Gdx.input.setInputProcessor(this);

        m_world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
                if (objectA != null) {
                    objectA.beginContact(contact.getFixtureA(), contact.getFixtureB(), objectB, contact);
                }
                if (objectB != null) {
                    objectB.beginContact(contact.getFixtureB(), contact.getFixtureA(), objectA, contact);
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
                if (objectA != null) {
                    objectA.preSolve(contact.getFixtureA(), contact.getFixtureB(), objectB, contact, oldManifold);
                }
                if (objectB != null) {
                    objectB.preSolve(contact.getFixtureB(), contact.getFixtureA(), objectA, contact, oldManifold);
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
                if (objectA != null) {
                    objectA.postSolve(contact.getFixtureA(), contact.getFixtureB(), objectB, contact, impulse);
                }
                if (objectB != null) {
                    objectB.postSolve(contact.getFixtureB(), contact.getFixtureA(), objectA, contact, impulse);
                }
            }

            @Override
            public void endContact(Contact contact) {
                GameObject objectA = (GameObject) contact.getFixtureA().getBody().getUserData();
                GameObject objectB = (GameObject) contact.getFixtureB().getBody().getUserData();
                if (objectA != null) {
                    objectA.endContact(contact.getFixtureA(), contact.getFixtureB(), objectB, contact);
                }
                if (objectB != null) {
                    objectB.endContact(contact.getFixtureB(), contact.getFixtureA(), objectA, contact);
                }
            }
        });
    }

    @Override
    public void dispose() {
        m_debugRenderer.dispose();
        m_swarm.dispose();
        m_world.dispose();
        for (int i = 0; i < m_gameObjects.length; i++) {
            if (m_gameObjects[i] != null) {
                m_gameObjects[i].dispose();
            }
        }
        m_batch.dispose();
        m_font.dispose();
    }

    protected void renderInfo() {
        String message;

        message = "FPS: " + Gdx.graphics.getFramesPerSecond() + "\n";
        message += "Device rotated to:" + Gdx.input.getRotation() + " degrees\n";
        message += String.format(Locale.ENGLISH, "Device Resolution: %dx%d\n", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        message += String.format(Locale.ENGLISH, "Device Acceleration: (%.2f,%.2f,%.2f)\n", Gdx.input.getAccelerometerX(), Gdx.input.getAccelerometerY(), Gdx.input.getAccelerometerZ());
        if (Gdx.input.isPeripheralAvailable(Peripheral.Compass)) {
            message += String.format(Locale.ENGLISH, "Device Direction: (%.2f,%.2f,%.2f)\n", Gdx.input.getAzimuth(), Gdx.input.getPitch(), Gdx.input.getRoll());
        } else {
            message += "No compass available\n";
        }
        message += String.format(Locale.ENGLISH, "Gravity Direction: (%.2f,%.2f)\n", m_world.getGravity().x, m_world.getGravity().y);
        message += String.format(Locale.ENGLISH, "Swarm Position: (%.2f,%.2f)\n", m_swarm.position.x, m_swarm.position.y);

        m_font.draw(m_batch, message, 0, Gdx.graphics.getHeight());
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        m_world.step(delta, 8, 4);

        m_swarm.update(delta);
        for (int i = 0; i < m_gameObjects.length; i++) {
            if (m_gameObjects[i] != null) {
                GameObject[] result = m_gameObjects[i].update(delta);
                if (result != null) {
                    for (int j = 0; j < result.length; j++) {
                        if (result[j] != null) {
                            for (int k = 0; k < m_gameObjects.length; k++) {
                                if (m_gameObjects[k] == null) {
                                    m_gameObjects[k] = result[j];
                                    break;
                                }
                            }
                        }
                    }
                }

                if (!m_gameObjects[i].isExisting()) {
                    m_gameObjects[i].dispose();
                    m_gameObjects[i] = null;
                }
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        m_batch.begin();

        Matrix4 original = m_batch.getProjectionMatrix();

        m_debugRenderer.render(m_world, m_batch.getProjectionMatrix().cpy().scale(PPM, PPM, 0));

        m_batch.end();

        m_batch.begin();

        m_batch.setProjectionMatrix(original);

        renderInfo();

        m_batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Gdx.app.log("Events", "touch down: (" + screenX + ":" + screenY + ")[" + pointer + "]");
        if (pointer == 0) {
            m_swarm.position.x = screenX / PPM;
            m_swarm.position.y = (Gdx.graphics.getHeight() - screenY) / PPM;
            m_swarm.size = 1f;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Gdx.app.log("Events", "touch dragged: (" + screenX + ":" + screenY + ")[" + pointer + "]");
        m_swarm.position.x = screenX / PPM;
        m_swarm.position.y = (Gdx.graphics.getHeight() - screenY) / PPM;
        m_swarm.size = 1f;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Gdx.app.log("Events", "touch up: (" + screenX + ":" + screenY + ")[" + pointer + "]");
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
