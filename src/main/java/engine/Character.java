package koumuu.game.engine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Character implements CollisionComponent {
    protected Game game;
    private Map<String, ArrayList<BufferedImage>> images = new HashMap<String, ArrayList<BufferedImage>>();

    // controller
    private KeyListener keyListener;
    private char up;
    private char down;
    private char left;
    private char right;

    // render
    private int row;
    private int col;
    private ArrayList<BufferedImage> renderingImages;
    private BufferedImage renderedImage;
    private long lastMovedTimestamp = 0;

    public Character(Game game, String imageFolder) {
        this.game = game;

        // load images
        String[] directions = {"up", "down", "left", "right"};

        for (String direction: directions) {
            int index = 0;
            ArrayList<BufferedImage> subImages = new ArrayList<BufferedImage>();

            while (true) {
                try {
                    InputStream is = getClass().getResourceAsStream(game.getAssetsPath() + imageFolder + direction + "_" + String.valueOf(index++) + ".png");

                    if (is == null) {
                        break;
                    }

                    subImages.add(ImageIO.read(is));
                } catch (IOException e) {
                    break;
                }
            }

            images.put(direction, subImages);
        }

        // set controller
        // add event listeners
        keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                move(keyEvent.getKeyChar());
            }
        };

        game.setFocusable(true);
        game.requestFocus();
        game.addKeyListener(keyListener);

        // default rendering image
        renderingImages = images.get("down");
    }

    public void dispose() {
        game.removeKeyListener(keyListener);
    }

    public void bindKeys(char up, char down, char left, char right) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void render(Graphics graphics) {
        if (renderingImages.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        long pastMillSeconds = (timestamp - lastMovedTimestamp);

        // determine rendering image
        int tileSize = game.getTileSize();
        int frames = renderingImages.size();
        double interval = 100;
        int index = (int)Math.floor((pastMillSeconds / interval) % 1000.0);

        renderedImage = renderingImages.get(index % frames);

        // animation at current position
        graphics.drawImage(renderedImage, getLeft(), getTop(),
                getRight(), getBottom(),
                0, 0,
                tileSize, tileSize,
                null);
    }

    private void move(char direction) {
        int nextRow = row;
        int nextCol = col;

        if (direction == up) {
            nextRow--;
            renderingImages = images.get("up");
        } else if (direction == down) {
            nextRow++;
            renderingImages = images.get("down");
        } else if (direction == left) {
            nextCol--;
            renderingImages = images.get("left");
        } else if (direction == right) {
            nextCol++;
            renderingImages = images.get("right");
        } else {
            return;
        }

        // check movable
        int nextNextRow = nextRow + (nextRow - row);
        int nextNextCol = nextCol + (nextCol - col);
        Tile nextTile = game.getTile(nextRow, nextCol);
        Tile nextNextTile = game.getTile(nextNextRow, nextNextCol);

        if (nextTile != null && nextNextTile != null && nextTile.isMovable() && !nextNextTile.isCollisional()) {
            nextTile.setPosition(nextNextRow, nextNextCol);
            nextNextTile.setPosition(nextRow, nextCol);

            return;
        }


        // check collision
        if (nextTile != null && !nextTile.isCollisional()) {
            row = nextRow;
            col = nextCol;
            lastMovedTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public int getLeft() {
        return col * game.getTileSize();
    }

    @Override
    public int getRight() {
        return (col + 1) * game.getTileSize();
    }

    @Override
    public int getTop() {
        return row * game.getTileSize();
    }

    @Override
    public int getBottom() {
        return (row + 1) * game.getTileSize();
    }

    @Override
    public BufferedImage getImage() {
        return renderedImage;
    }

    @Override
    public void inCollision(CollisionComponent component) {
        System.out.println("char col");
    }
}
