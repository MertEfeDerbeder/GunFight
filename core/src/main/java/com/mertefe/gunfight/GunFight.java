package com.mertefe.gunfight;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GunFight extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture player;
    private float playerX;
    private float playerY;
    private float v_y = 0;
    private float gravity = 1000f;
    private float jump_force = 500f;
    private boolean isJumping = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Texture("player.png");
        playerX = 140;
        playerY = 140;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            if (isJumping == false) {
                v_y = jump_force;
                isJumping = true;
            }
        }
        v_y -= gravity * Gdx.graphics.getDeltaTime();
        playerY += v_y * Gdx.graphics.getDeltaTime();

        if (playerY < 140) {
            playerY = 140;
            v_y = 0;
            isJumping = false;
        }
        batch.begin();
        batch.draw(player, playerX, playerY);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        player.dispose();
    }
}
