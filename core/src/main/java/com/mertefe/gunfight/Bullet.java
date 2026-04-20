package com.mertefe.gunfight;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    public float x;
    public float y;
    public float velocityX;
    public float velocityY;
    public Rectangle hitbox;
    public Player owner;
    public boolean hasLeftOwner = false;

    private static final float SPEED = 800f;
    private static final float SIZE = 8f;

    public Bullet(Player owner, float startX, float startY, float targetX, float targetY) {
        this.owner = owner;
        this.hasLeftOwner = false;
        this.x = startX;
        this.y = startY;

        // Calculate direction from player to mouse and normalize it
        Vector2 direction = new Vector2(targetX - startX, targetY - startY).nor();
        this.velocityX = direction.x * SPEED;
        this.velocityY = direction.y * SPEED;

        this.hitbox = new Rectangle(x, y, SIZE, SIZE);
    }

    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;
        hitbox.setPosition(x, y);
    }

    public boolean isOffScreen(float worldWidth, float worldHeight) {
        return x < -50 || x > worldWidth + 50 || y < -50 || y > worldHeight + 50;
    }
}
