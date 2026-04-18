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
    private Rectangle wall1;

    public void create() {
        batch = new SpriteBatch();
        player = new Texture("player.png");
        playerHitbox = new Rectangle(playerX, playerY, player.getWidth(), player.getHeight());
        platform1 = new Rectangle(200, 250, 300, 20);
        wall1 = new Rectangle(500, 140, 40, 300); // Test için dikey bir duvar
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
        
        boolean isTouchingWall = false;
        playerHitbox.setPosition(playerX, playerY);
        if (playerHitbox.overlaps(platform1) || playerHitbox.overlaps(wall1)) {
            playerX = preX; // İleri gidemezsen geri dön (Duvar)
            playerHitbox.setPosition(playerX, playerY);
            isTouchingWall = true; // Duvara çarptık!
        }

        // 2. Y Ekseni (Yukarı/Aşağı) Hareketi ve Solid Zemin/Tavan Kontrolü
        // Zıplamak için basılı tutmak yerine sadece tuşa basıldığı "o anı" yakalayan JustPressed'i kullandım (Wall Jump bug'ı yaratmaması için)
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            if (isJumping == false) {
                // Yerden Zıplama
                v_y = jump_force;
                isJumpCut = false;
                isJumping = true;
            } else if (isTouchingWall) {
                // Duvardan Zıplama (Simple Wall Jump!)
                v_y = jump_force;
                isJumpCut = false;
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
        
        // Hangi cisme çarptığımızı bulalım ki onun 'y' ve 'height' değerine göre oturtalım
        Rectangle hitObj = null;
        if (playerHitbox.overlaps(platform1)) hitObj = platform1;
        else if (playerHitbox.overlaps(wall1)) hitObj = wall1;

        if (hitObj != null) {
            if (v_y < 0) {
                // Aşağı düşerken platforma takıldı (Zemin)
                playerY = hitObj.y + hitObj.height;
                isJumping = false;
            } else if (v_y > 0) {
                // Yukarı zıplarken tuğlaya kafa attı (Tavan)
                playerY = hitObj.y - player.getHeight();
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
