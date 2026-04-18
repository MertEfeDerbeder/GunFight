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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GunFight extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture player;
    private Texture crosshair;
    private Texture bulletTexture;
    private Array<Bullet> bullets;
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

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);

        player = new Texture("player.png");
        crosshair = new Texture("crosshair.png");
        Gdx.input.setCursorCatched(true);

        bulletTexture = new Texture("bullet.png");
        bullets = new Array<>();

        playerHitbox = new Rectangle(playerX, playerY, player.getWidth(), player.getHeight());
        platform1 = new Rectangle(200, 250, 300, 20);
        wall1 = new Rectangle(500, 140, 40, 300);
        playerX = 140;
        playerY = 140;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float accel = 2500f;
        float walk_speed = 300f;

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            v_x += accel * Gdx.graphics.getDeltaTime();
            if (v_x > walk_speed)
                v_x = walk_speed;
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            v_x -= accel * Gdx.graphics.getDeltaTime();
            if (v_x < -walk_speed)
                v_x = -walk_speed;
        } else {
            v_x *= 0.8f;
        }

        float preX = playerX;
        playerX += v_x * Gdx.graphics.getDeltaTime();

        boolean isTouchingWall = false;
        int wallSide = 0;

        playerHitbox.setPosition(playerX, playerY);
        if (playerHitbox.overlaps(platform1) || playerHitbox.overlaps(wall1)) {
            if (playerX > preX)
                wallSide = 1;
            else if (playerX < preX)
                wallSide = -1;

            playerX = preX;
            v_x = 0;
            playerHitbox.setPosition(playerX, playerY);
            isTouchingWall = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (!isJumping) {
                v_y = jump_force;
                isJumpCut = false;
                isJumping = true;
            } else if (isTouchingWall) {
                v_y = jump_force * 0.9f;
                if (wallSide == 1) {
                    v_x = -600f;
                } else if (wallSide == -1) {
                    v_x = 600f;
                }
                isJumpCut = false;
            }
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.W) && isJumping && v_y > 0 && !isJumpCut) {
            v_y *= 0.4f;
            isJumpCut = true;
        }

        v_y -= gravity * Gdx.graphics.getDeltaTime();

        if (isTouchingWall && v_y < 0) {
            float maxSlideSpeed = -150f;
            if (v_y < maxSlideSpeed)
                v_y = maxSlideSpeed;
        }

        playerY += v_y * Gdx.graphics.getDeltaTime();

        if (playerY < 140) {
            playerY = 140;
            v_y = 0;
            isJumping = false;
        }

        playerHitbox.setPosition(playerX, playerY);

        Rectangle hitObj = null;
        if (playerHitbox.overlaps(platform1))
            hitObj = platform1;
        else if (playerHitbox.overlaps(wall1))
            hitObj = wall1;

        if (hitObj != null) {
            if (v_y < 0) {
                playerY = hitObj.y + hitObj.height;
                isJumping = false;
            } else if (v_y > 0) {
                playerY = hitObj.y - player.getHeight();
            }
            v_y = 0;
            playerHitbox.setPosition(playerX, playerY);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        mouseWorldX = mousePos.x;
        mouseWorldY = mousePos.y;

        // Shoot a bullet on left click
        if (Gdx.input.justTouched()) {
            float startX = playerX + player.getWidth() / 2f;
            float startY = playerY + player.getHeight() / 2f;
            bullets.add(new Bullet(startX, startY, mouseWorldX, mouseWorldY));
        }

        // Update bullets and remove off-screen ones
        float delta = Gdx.graphics.getDeltaTime();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);
            if (b.isOffScreen(1280, 720)) {
                bullets.removeIndex(i);
            }
        }

        batch.begin();
        batch.draw(player, playerX, playerY);
        for (Bullet b : bullets) {
            batch.draw(bulletTexture, b.x, b.y);
        }
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
        bulletTexture.dispose();
    }
}
