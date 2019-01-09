import engine.*;
import levelEditor.LevelEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        try{
            new GameLoader();
        } catch (NullPointerException e) {
            System.out.println("it seems that some assets are not existed");
        }
    }
}

class GameLoader extends JPanel {
    private JFrame frame;

    public GameLoader() {
        this.frame = new JFrame("猿の冒険");

        this.frame.setResizable(false);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel description = new JLabel(new ImageIcon(getClass().getResource("/assets/loader/description.png")));
        JButton startGame = new JButton("ゲーム開始");
        JButton launchEditor = new JButton("レベルエディター");

        description.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        launchEditor.setAlignmentX(Component.CENTER_ALIGNMENT);

        startGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.hide();
                loadGame();
            }
        });

        launchEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.hide();
                loadLevelEditor();
            }
        });

        this.setBackground(new Color(0x491773));

        this.add(description);
        this.add(startGame);
        this.add(launchEditor);

        // show
        this.frame.setLocationRelativeTo(null);
        this.frame.pack();
        this.frame.setVisible(true);
    }

    private static void loadLevelEditor() {
        new LevelEditor("/assets/map", "png", 20, 20, 40);
    }

    private static void loadGame() {
        new GameImpl("/assets/", 40).load();
    }
}

class GameImpl extends Game {
    private ArrayList<Tile> coins = new ArrayList<Tile>();
    private int currentLevel = 0;
    private ArrayList<Timer> timers = new ArrayList<Timer>();
    private BufferedImage tips;
    private KeyAdapter reloadKeyAdapter;

    public GameImpl(String assetsPath, int tileSize) {
        super(assetsPath, tileSize);
    }

    public void load() {
        final GameImpl game = this;
        Monkey monkey = new Monkey(game, "monkey/");

        monkey.bindKeys('w', 's', 'a', 'd');
        game.addCharacter(monkey, 0, 0);

        // add animations
        // cannons
        ArrayList<Tile> leftCannons = game.getTilesByImageIndex(3);
        ArrayList<Tile> rightCannons = game.getTilesByImageIndex(2);
        HashMap<Tile, Integer> cannons = new HashMap<Tile, Integer>();

        for (Tile tile: leftCannons) {
            cannons.put(tile, 45);
        }

        for (Tile tile: rightCannons) {
            cannons.put(tile, -45);
        }

        for (Map.Entry<Tile, Integer> cannon: cannons.entrySet()) {
            final Tile tile = cannon.getKey();
            final int degree = cannon.getValue();
            final int x = degree == 45 ? 760 : 0;
            final int y = 0;

            Timer timer = new Timer();
            timers.add(timer);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    game.addAnimation(new LinearAnimation(game.loadImage("animation/bullet.png", degree, true), tile.getLeft(), tile.getTop(), x, y, 8) {
                        @Override
                        public void inCollision(CollisionComponent component) {
                            // disappear when touch a collisional tile
                            if (component instanceof Tile) {
                                Tile targetTile = (Tile)component;

                                if (targetTile != tile && targetTile.isCollisional()) {
                                    game.removeAnimation(this);
                                }
                            } else if (component instanceof Monkey) {
                                game.pauseOnNextFrame(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent actionEvent) {
                                        showTips("died");
                                    }
                                });
                            }
                        }
                    });
                }
            }, 0, 400);
        }

        // make boxes movable
        ArrayList<Tile> boxes = game.getTilesByImageIndex(1);

        for (Tile box: boxes) {
            box.setMovable(true);
        }

        // add coin animate
        ArrayList<BufferedImage> coinImages = game.loadImages("coin/");
        this.coins = game.getTilesByImageIndex(17);

        for (final Tile coin: coins) {
            coin.remove();

            game.addAnimation(new ImageAnimation(coinImages, coin.getLeft(), coin.getTop(), 1) {
                @Override
                public void inCollision(CollisionComponent component) {
                    if (component instanceof Monkey) {
                        coins.remove(coin);
                    }

                    if (coins.size() == 0) {
                        game.pauseOnNextFrame(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent actionEvent) {
                                unload();

                                if (game.loadLevel(currentLevel + 1)) {
                                    currentLevel++;
                                    load();
                                } else {
                                    showTips("congrats");
                                }
                            }
                        });
                    }
                }
            });
        }

        // add reload listener
        reloadKeyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == 'r') {
                    unload();

                    if (loadLevel(currentLevel)) {
                        tips = null;
                        load();
                    }
                }
            }
        };

        this.addKeyListener(reloadKeyAdapter);
    }

    public void unload() {
        removeKeyListener(reloadKeyAdapter);

        // clear timers
        for (Timer timer: timers) {
            timer.cancel();
        }

        timers.clear();
    }

    public void showTips(String tips) {
        this.tips = loadImage("tips/" + tips + ".png", 0, false);

        setSize(new Dimension(this.tips.getWidth(null), this.tips.getHeight(null)));
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        // render tips
        if (tips != null) {
            // draw overlay
            graphics.setColor(Color.white);
            graphics.fillRect(0, 0, getWidth(), getHeight());

            // set font size
            int tipsWidth = tips.getWidth(null);
            int tipsHeight = tips.getHeight(null);
            int tipsX = (getWidth() - tipsWidth) / 2;
            int tipsY = (getHeight() - tipsHeight) / 2;

            graphics.drawImage(tips, tipsX, tipsY, tipsX + tipsWidth, tipsY + tipsHeight, 0, 0, tipsWidth, tipsHeight, null);
        }
    }
}

class Monkey extends engine.Character {
    public Monkey(Game game, String imageFolder) {
        super(game, imageFolder);
    }

    @Override
    public void inCollision(CollisionComponent component) {
        if (component instanceof Animation) {
            Animation animation = (Animation)component;

            game.removeAnimation(animation);
        }
    }
}