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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GunFight extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture playerTexture;
    private Texture crosshair;
    private Texture bulletTexture;
    private Array<Bullet> bullets;
    private Player player1;
    private Player player2;
    private Array<Player> players;
    private BitmapFont font;
    private float mouseWorldX;
    private float mouseWorldY;
    private Array<Rectangle> platforms;
    private Array<Rectangle> walls;
    private Texture wallTexture;
    private Texture platformTexture;

    // Camera and viewport
    private OrthographicCamera camera;
    private Viewport viewport;

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0);

        playerTexture = new Texture("player.png");
        crosshair = new Texture("crosshair.png");
        Gdx.input.setCursorCatched(true);

        bulletTexture = new Texture("bullet.png");
        bullets = new Array<>();

        font = new BitmapFont(); // Default font for damage %
        font.getData().setScale(1.5f);

        players = new Array<>();
        player1 = new Player(playerTexture, 640, 30);
        player2 = new Player(playerTexture, 640, 240); // On the middle platform
        players.add(player1);
        players.add(player2);

        // Load map textures
        wallTexture = new Texture("wall.png");
        platformTexture = new Texture("platform.png");

        // Build arena map
        platforms = new Array<>();
        walls = new Array<>();

        float ph = 20f; // platform height
        float wt = 30f; // wall thickness

        // Floor (full width)
        platforms.add(new Rectangle(0, 0, 1280, ph));

        // Level 2 platform (centered)
        platforms.add(new Rectangle(400, 220, 480, ph));

        // Level 3 platform (centered)
        platforms.add(new Rectangle(450, 420, 380, ph));

        // Ceiling (full width, closes the top)
        platforms.add(new Rectangle(0, 700, 1280, ph));

        // Left edge walls (flush to left side of screen)
        walls.add(new Rectangle(0, 120, wt, 160)); // Left-bottom (gap at bottom to exit map)
        walls.add(new Rectangle(0, 480, wt, 220)); // Left-top (stops below ceiling)

        // Left middle wall (closer to center platforms, wall-jump distance from edge)
        walls.add(new Rectangle(200, 280, wt, 160)); // Left-middle

        // Right edge walls (flush to right side of screen)
        walls.add(new Rectangle(1250, 120, wt, 160)); // Right-bottom (gap at bottom to exit map)
        walls.add(new Rectangle(1250, 480, wt, 220)); // Right-top (stops below ceiling)

        // Right middle wall (closer to center platforms, wall-jump distance from edge)
        walls.add(new Rectangle(1050, 280, wt, 160)); // Right-middle

        player1.x = 640;
        player1.y = 30;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        // Handle player input
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player1.moveRight(Gdx.graphics.getDeltaTime());
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player1.moveLeft(Gdx.graphics.getDeltaTime());
        } else {
            player1.applyFriction();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            player1.jump();
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.W)) {
            player1.cutJump();
        }

        // Update all players
        for (Player p : players) {
            p.update(Gdx.graphics.getDeltaTime(), platforms, walls);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);
        mouseWorldX = mousePos.x;
        mouseWorldY = mousePos.y;

        // Shoot a bullet on left click
        if (Gdx.input.justTouched()) {
            float startX = player1.x + player1.getWidth() / 2f;
            float startY = player1.y + player1.getHeight() / 2f;
            bullets.add(new Bullet(player1, startX, startY, mouseWorldX, mouseWorldY));
        }

        // Update bullets and remove off-screen ones or those hitting players
        float delta = Gdx.graphics.getDeltaTime();
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            if (!b.hasLeftOwner) {
                if (!b.hitbox.overlaps(b.owner.hitbox)) {
                    b.hasLeftOwner = true;
                }
            }

            boolean hit = false;
            for (Player p : players) {
                if ((p != b.owner || b.hasLeftOwner) && b.hitbox.overlaps(p.hitbox)) {
                    p.takeDamage(10); // Each hit adds 10%
                    hit = true;
                    break;
                }
            }

            if (hit || b.isOffScreen(1280, 720)) {
                bullets.removeIndex(i);
            }
        }

        batch.begin();
        // Draw platforms
        for (Rectangle p : platforms) {
            batch.draw(platformTexture, p.x, p.y, p.width, p.height);
        }
        // Draw walls
        for (Rectangle w : walls) {
            batch.draw(wallTexture, w.x, w.y, w.width, w.height);
        }
        for (Player p : players) {
            p.draw(batch);
            font.draw(batch, p.damage + "%", p.x, p.y + p.getHeight() + 20);
        }
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
        playerTexture.dispose();
        crosshair.dispose();
        bulletTexture.dispose();
        wallTexture.dispose();
        platformTexture.dispose();
        font.dispose();
    }
}
