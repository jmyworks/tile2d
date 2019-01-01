package koumuu.game.engine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

public class Tile implements CollisionComponent {
    private Game game;
    private BufferedImage image;
    private boolean isCollisional;
    private boolean isMovable;
    private int row;
    private int col;

    Tile(Game game, BufferedImage image, boolean isCollisional, int row, int col) {
        this.game = game;
        this.image = image;
        this.isCollisional = isCollisional;
        this.row = row;
        this.col = col;
    }

    public boolean isCollisional() {
        return this.isCollisional;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void setMovable(boolean movable) {
        isMovable = movable;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void remove() {
        this.image = game.getImage(0);
        this.isCollisional = false;
        this.isMovable = false;
    }

    @Override
    public int getLeft() {
        return col * game.getTileSize();
    }

    @Override
    public int getRight() {
        return col * game.getTileSize() + game.getTileSize();
    }

    @Override
    public int getTop() {
        return row * game.getTileSize();
    }

    @Override
    public int getBottom() {
        return row * game.getTileSize() + game.getTileSize();
    }

    @Override
    public void inCollision(CollisionComponent component) {
    }

    public void render(Graphics graphics) {
        int tileSize = game.getTileSize();

        graphics.drawImage(image, getLeft(), getTop(), tileSize, tileSize, null);
    }
}
