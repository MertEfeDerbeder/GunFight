package com.mertefe.gunfight;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
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
    private float jump_force = 600f;
    private boolean isJumping = false;
    private boolean isJumpCut = false;
    private Rectangle playerHitbox;
    private Rectangle platform1;

    @Override
    public void create() {
        batch = new SpriteBatch();
        player = new Texture("player.png");
        playerHitbox = new Rectangle(playerX, playerY, player.getWidth(), player.getHeight());
        platform1 = new Rectangle(200, 250, 300, 20);
        playerX = 140;
        playerY = 140;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // 1. X Ekseni (Sağ/Sol) Hareketi ve Solid Duvar Kontrolü
        float preX = playerX;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            playerX += 5;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            playerX -= 5;
        }
        
        playerHitbox.setPosition(playerX, playerY);
        if (playerHitbox.overlaps(platform1)) {
            playerX = preX; // İleri gidemezsen geri dön (Duvar)
            playerHitbox.setPosition(playerX, playerY);
        }

        // 2. Y Ekseni (Yukarı/Aşağı) Hareketi ve Solid Zemin/Tavan Kontrolü
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            if (isJumping == false) {
                v_y = jump_force;
                isJumpCut = false;
                isJumping = true;
            }
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.W) && isJumping == true && v_y > 0 && !isJumpCut) {
            v_y *= 0.4f;
            isJumpCut = true;
        }
        
        v_y -= gravity * Gdx.graphics.getDeltaTime();
        playerY += v_y * Gdx.graphics.getDeltaTime();

        if (playerY < 140) {
            playerY = 140;
            v_y = 0;
            isJumping = false;
        }

        playerHitbox.setPosition(playerX, playerY);
        if (playerHitbox.overlaps(platform1)) {
            if (v_y < 0) {
                // Aşağı düşerken platforma takıldı (Zemin)
                playerY = platform1.y + platform1.height;
                isJumping = false;
            } else if (v_y > 0) {
                // Yukarı zıplarken tuğlaya kafa attı (Tavan)
                playerY = platform1.y - player.getHeight();
            }
            v_y = 0; // İki durumda da hız sıfırlanır
            playerHitbox.setPosition(playerX, playerY);
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
