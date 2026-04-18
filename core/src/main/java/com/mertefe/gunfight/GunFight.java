package com.mertefe.gunfight;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class GunFight extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture player;
    private Texture crosshair;
    private float mouseWorldX;
    private float mouseWorldY;
    private float playerX;
    private float playerY;
    private float v_y = 0;
    private float v_x = 0;
    private float gravity = 1000f;
    private float jump_force = 600f;
    private boolean isJumping = false;
    private boolean isJumpCut = false;
    private Rectangle playerHitbox;
    private Rectangle platform1;
    private Rectangle wall1;

    // Camera and viewport
    private OrthographicCamera camera;
    private Viewport viewport;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Create a 1280x720 virtual world. FitViewport preserves aspect ratio.
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);

        player = new Texture("player.png");
        crosshair = new Texture("crosshair.png");
        Gdx.input.setCursorCatched(true); // Hide the OS cursor

        playerHitbox = new Rectangle(playerX, playerY, player.getWidth(), player.getHeight());
        platform1 = new Rectangle(200, 250, 300, 20);
        wall1 = new Rectangle(500, 140, 40, 300);
        playerX = 140;
        playerY = 140;
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport on window resize to keep aspect ratio intact
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // 1. X Axis: Horizontal movement with velocity and friction
        float accel = 2500f;
        float walk_speed = 300f;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            v_x += accel * Gdx.graphics.getDeltaTime();
            if (v_x > walk_speed) v_x = walk_speed;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            v_x -= accel * Gdx.graphics.getDeltaTime();
            if (v_x < -walk_speed) v_x = -walk_speed;
        } else {
            v_x *= 0.8f; // Friction
        }

        float preX = playerX;
        playerX += v_x * Gdx.graphics.getDeltaTime();

        boolean isTouchingWall = false;
        int wallSide = 0; // 1 = wall on right, -1 = wall on left

        playerHitbox.setPosition(playerX, playerY);
        if (playerHitbox.overlaps(platform1) || playerHitbox.overlaps(wall1)) {
            if (playerX > preX) wallSide = 1;
            else if (playerX < preX) wallSide = -1;

            playerX = preX; // Push back on wall hit
            v_x = 0;        // Stop horizontal velocity
            playerHitbox.setPosition(playerX, playerY);
            isTouchingWall = true;
        }

        // 2. Y Axis: Jumping, gravity, wall jump, wall slide
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (!isJumping) {
                // Normal ground jump
                v_y = jump_force;
                isJumpCut = false;
                isJumping = true;
            } else if (isTouchingWall) {
                // Advanced wall jump: kick off in the opposite direction
                v_y = jump_force * 0.9f;
                if (wallSide == 1) {
                    v_x = -600f; // Wall on right, kick left
                } else if (wallSide == -1) {
                    v_x = 600f;  // Wall on left, kick right
                }
                isJumpCut = false;
            }
        }
        // Variable jump height: cut upward speed when W is released early
        if (!Gdx.input.isKeyPressed(Input.Keys.W) && isJumping && v_y > 0 && !isJumpCut) {
            v_y *= 0.4f;
            isJumpCut = true;
        }

        v_y -= gravity * Gdx.graphics.getDeltaTime();

        // Wall slide: slow down fall speed when hugging a wall
        if (isTouchingWall && v_y < 0) {
            float maxSlideSpeed = -150f;
            if (v_y < maxSlideSpeed) v_y = maxSlideSpeed;
        }

        playerY += v_y * Gdx.graphics.getDeltaTime();

        // Ground check
        if (playerY < 140) {
            playerY = 140;
            v_y = 0;
            isJumping = false;
        }

        playerHitbox.setPosition(playerX, playerY);

        // Find the collided object and snap player correctly
        Rectangle hitObj = null;
        if (playerHitbox.overlaps(platform1)) hitObj = platform1;
        else if (playerHitbox.overlaps(wall1)) hitObj = wall1;

        if (hitObj != null) {
            if (v_y < 0) {
                // Landed on top
                playerY = hitObj.y + hitObj.height;
                isJumping = false;
            } else if (v_y > 0) {
                // Hit the ceiling
                playerY = hitObj.y - player.getHeight();
            }
            v_y = 0;
            playerHitbox.setPosition(playerX, playerY);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Convert screen-space mouse coords to world-space
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        mouseWorldX = mousePos.x;
        mouseWorldY = mousePos.y;

        batch.begin();
        batch.draw(player, playerX, playerY);
        // Draw crosshair centered on the mouse cursor
        batch.draw(crosshair,
            mouseWorldX - crosshair.getWidth() / 2f,
            mouseWorldY - crosshair.getHeight() / 2f);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
        crosshair.dispose();
    }
}
