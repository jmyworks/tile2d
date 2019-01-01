package koumuu.game.engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Timer;

public class Game extends JPanel implements Runnable {
    private JFrame frame;
    private boolean pauseOnNextFrame = false;
    private boolean paused = true;

    // resources
    private String assetsPath;
    private int tileSize;
    private int rows;
    private int cols;
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

    // components
    private ArrayList<Tile> tiles = new ArrayList<Tile>();
    private ArrayList<ArrayList<Tile>> tilesByImageIndex = new ArrayList<ArrayList<Tile>>();
    private ArrayList<Character> characters = new ArrayList<Character>();
    private CopyOnWriteArrayList<Animation> animations = new CopyOnWriteArrayList<Animation>();

    // collision
    private CollisionSectionManager collisionSectionManager;

    public Game(String assetsPath, int tileSize) {
        this.assetsPath = assetsPath;
        this.tileSize = tileSize;

        // init images pool
        this.images = loadImages("map/");

        // add to frame
        this.frame = new JFrame("Game");

        this.frame.setResizable(false);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this);

        // set absolute position
        this.setLayout(null);

        // load first level
        loadLevel(0);

        // show
        this.setPreferredBounds(null);
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);

        // start painting thread
        new Thread(this).start();
    }

    public void setPreferredBounds(Dimension dimension) {
        this.frame.setPreferredSize(dimension == null ? new Dimension(cols * tileSize, rows * tileSize) : dimension);
        this.frame.getContentPane().setPreferredSize(dimension == null ? new Dimension(cols * tileSize, rows * tileSize) : dimension);
        this.frame.pack();
    }

    public boolean loadLevel(int levelIndex) {
        try {
            InputStream is = getClass().getResourceAsStream(assetsPath + "levels/" + levelIndex + ".lvl");

            if (is == null) {
                return false;
            }

            ObjectInputStream ois = new ObjectInputStream(is);

            int[][] encodedTilesData = (int[][])ois.readObject();

            if (encodedTilesData.length > 0) {
                this.rows = encodedTilesData.length;
                this.cols = encodedTilesData[0].length;
                this.setPreferredBounds(null);

                // set collision section manager
                this.collisionSectionManager = new CollisionSectionManager(cols * tileSize, rows * tileSize, 2 * tileSize);

                // clear arrays
                this.animations.clear();
                this.tiles.clear();
                this.tilesByImageIndex.clear();

                // clean characters
                for (Character character: characters) {
                    character.dispose();
                }

                characters.clear();

                // init tilesByImageIndex
                for (int i = 0; i < images.size(); i++) {
                    tilesByImageIndex.add(i, new ArrayList<Tile>());
                }

                try {
                    for (int row = 0; row < encodedTilesData.length; row++) {
                        for (int col = 0; col < encodedTilesData[row].length; col++) {
                            TileData tileData = new TileData(encodedTilesData[row][col]);
                            Tile tile = new Tile(this, images.get(tileData.imageIndex), tileData.isCollisional, row, col);

                            this.tiles.add(tile);
                            tilesByImageIndex.get(tileData.imageIndex).add(tile);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("map images not satisfied");

                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println("read level file failed");
            System.out.println(e.getMessage());

            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("invalid level file format");

            return false;
        }

        paused = false;
        return true;
    }

    public String getAssetsPath() {
        return assetsPath;
    }

    public ArrayList<BufferedImage> loadImages(String dir) {
        ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
        int index = 0;

        while (true) {
            BufferedImage image = loadImage(dir + String.valueOf(index++) + ".png", 0, true);

            if (image != null) {
                images.add(image);
            } else {
                break;
            }
        }

        return images;
    }

    public BufferedImage loadImage(String path, int degree, boolean needResize) {
        try {
            InputStream is = getClass().getResourceAsStream(assetsPath + path);

            if (is == null) {
                return null;
            }

            BufferedImage image = ImageIO.read(is);

            if (needResize) {
                Image scaledImage = image.getScaledInstance(tileSize, tileSize, Image.SCALE_FAST);

                // create buffered image
                BufferedImage bufferedImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);

                // Draw the image on to the buffered image
                Graphics2D graphics = bufferedImage.createGraphics();
                graphics.drawImage(scaledImage, 0, 0, null);
                graphics.dispose();

                image = bufferedImage;
            }

            // need rotate?
            degree = degree % 360;
            if (degree == 0) {
                return image;
            }

            AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(degree), tileSize / 2.0, tileSize / 2.0);
            AffineTransformOp ao = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

            return ao.filter(image, null);
        } catch (IOException e) {
            System.out.println(e.getMessage());

            return null;
        }
    }

    public void pauseOnNextFrame(final ActionListener actionListener) {
        if (paused || pauseOnNextFrame) {
            return;
        }

        final Timer timer = new Timer();

        pauseOnNextFrame = true;
        timer.schedule(new TimerTask() {
            @Override
            synchronized public void run() {
                if (paused) {
                    timer.cancel();

                    if (actionListener != null) {
                        actionListener.actionPerformed(null);
                    }
                }
            }
        }, 0, 10);
    }

    public void addCharacter(Character character, int row, int col) {
        character.setRow(row);
        character.setCol(col);

        characters.add(character);
    }

    public void addAnimation(Animation animation) {
        animation.start();
        animations.add(animation);
    }

    public void removeAnimation(Animation animation) {
        animations.remove(animation);
    }

    public int getTileSize() {
        return tileSize;
    }

    public ArrayList<Tile> getTilesByImageIndex(int index) {
        return tilesByImageIndex.get(index);
    }

    public Tile getTile(int row, int col) {
        for (Tile tile: tiles) {
            if (row == tile.getRow() && col == tile.getCol()) {
                return tile;
            }
        }

        return null;
    }

    public BufferedImage getImage(int index) {
        return images.get(index);
    }

    @Override
    synchronized public void paint(Graphics graphics) {
        super.paint(graphics);

        if (paused) {
            return;
        }

        // background
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                graphics.drawImage(images.get(0), col * tileSize, row * tileSize, tileSize, tileSize, null);
            }
        }

        for (Tile tile: tiles) {
            tile.render(graphics);
        }

        for (Character character: characters) {
            character.render(graphics);
        }

        for (Animation animation: animations) {
            animation.render(graphics);
        }

        // check collision
        collisionSectionManager.rebuild();

        for (Tile tile: tiles) {
            if (tile.isCollisional()) {
                collisionSectionManager.addToSection(tile);
            }
        }

        for (Character character: characters) {
            collisionSectionManager.addToSection(character);
        }

        for (Animation animation: animations) {
            collisionSectionManager.addToSection(animation);
        }

        collisionSectionManager.checkCollisions();

        if (pauseOnNextFrame) {
            paused = true;
            pauseOnNextFrame = false;
        }
    }

    @Override
    public void run() {
        while (true) {
            repaint();
        }
    }
}
