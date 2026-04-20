package com.mertefe.gunfight;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {
    public float x;
    public float y;
    public float v_x = 0;
    public float v_y = 0;
    public int damage = 0; // Starts at 0%
    public boolean isJumping = false;
    public boolean isJumpCut = false;
    public Rectangle hitbox;
    private Texture texture;

    private final float GRAVITY = 1000f;
    private final float JUMP_FORCE = 600f;
    private final float ACCEL = 2500f;
    private final float WALK_SPEED = 300f;
    private final float FRICTION = 0.8f;

    public boolean isTouchingWall = false;
    public int wallSide = 0;

    public Player(Texture texture, float spawnX, float spawnY) {
        this.texture = texture;
        this.x = spawnX;
        this.y = spawnY;
        this.hitbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void moveLeft(float delta) {
        v_x -= ACCEL * delta;
        if (v_x < -WALK_SPEED)
            v_x = -WALK_SPEED;
    }

    public void moveRight(float delta) {
        v_x += ACCEL * delta;
        if (v_x > WALK_SPEED)
            v_x = WALK_SPEED;
    }

    public void applyFriction() {
        v_x *= FRICTION;
    }

    public void jump() {
        if (!isJumping) {
            v_y = JUMP_FORCE;
            isJumpCut = false;
            isJumping = true;
        } else if (isTouchingWall) {
            v_y = JUMP_FORCE * 0.9f;
            if (wallSide == 1) {
                v_x = -600f;
            } else if (wallSide == -1) {
                v_x = 600f;
            }
            isJumpCut = false;
        }
    }

    public void cutJump() {
        if (isJumping && v_y > 0 && !isJumpCut) {
            v_y *= 0.4f;
            isJumpCut = true;
        }
    }

    public void takeDamage(int amount) {
        damage += amount;
        if (damage > 100)
            damage = 100;
    }

    public void update(float delta, Array<Rectangle> platforms, Array<Rectangle> walls) {
        // Horizontal Movement
        float preX = x;
        x += v_x * delta;

        isTouchingWall = false;
        wallSide = 0;

        hitbox.setPosition(x, y);
        boolean xCollision = false;
        for (Rectangle r : platforms) {
            if (hitbox.overlaps(r)) {
                xCollision = true;
                break;
            }
        }
        if (!xCollision) {
            for (Rectangle r : walls) {
                if (hitbox.overlaps(r)) {
                    xCollision = true;
                    break;
                }
            }
        }

        if (xCollision) {
            if (x > preX)
                wallSide = 1;
            else if (x < preX)
                wallSide = -1;
            x = preX;
            v_x = 0;
            hitbox.setPosition(x, y);
            isTouchingWall = true;
        }

        // Mid-air logic
        v_y -= GRAVITY * delta;

        if (isTouchingWall && v_y < 0) {
            float maxSlideSpeed = -150f;
            if (v_y < maxSlideSpeed)
                v_y = maxSlideSpeed;
        }

        // Vertical Movement
        float preY = y;
        y += v_y * delta;
        hitbox.setPosition(x, y);

        Rectangle hitObj = null;
        for (Rectangle r : platforms) {
            if (hitbox.overlaps(r)) {
                hitObj = r;
                break;
            }
        }
        if (hitObj == null) {
            for (Rectangle r : walls) {
                if (hitbox.overlaps(r)) {
                    hitObj = r;
                    break;
                }
            }
        }

        if (hitObj != null) {
            if (v_y < 0) {
                y = hitObj.y + hitObj.height;
                isJumping = false;
            } else if (v_y > 0) {
                y = hitObj.y - texture.getHeight();
            }
            v_y = 0;
            hitbox.setPosition(x, y);
        }
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public float getWidth() {
        return texture.getWidth();
    }

    public float getHeight() {
        return texture.getHeight();
    }
}
