package levelEditor;

import engine.TileData;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class LevelEditor extends JPanel {
    private JFrame frame;
    private ArrayList<Image> images = new ArrayList<Image>();
    private int[][] tileImageIndices;
    private int rows;
    private int cols;
    private int tileSize;
    private MapPreview mp;
    private ImageSelector is;
    private DraggingImage di;

    private int padding = 20;

    public LevelEditor(String imagesFolder, String imageExt, int rows, int cols, int tileSize) {
        // set vars
        this.rows = rows;
        this.cols = cols;
        this.tileSize = tileSize;
        this.tileImageIndices = new int[rows][cols];

        // load resources
        int index = 0;

        while (true) {
            InputStream is = getClass().getResourceAsStream(imagesFolder + "/" + String.valueOf(index++) + "." + imageExt);

            if (is == null) {
                break;
            }

            try {
                images.add(ImageIO.read(is));
            } catch (IOException e) {
                break;
            }
        }

        // init tiles with 0-th image
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                tileImageIndices[i][j] = 0;
            }
        }

        // add to frame
        this.frame = new JFrame("Level Editor");

        this.frame.setResizable(false);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.add(this);

        // set absolute position
        this.setLayout(null);

        // add dragging image
        di = new DraggingImage();
        this.add(di);

        // add map preview
        mp = new MapPreview();
        this.add(mp);

        // add image selector
        is = new ImageSelector();
        this.add(is);

        // add event listeners
        this.setFocusable(true);
        this.requestFocus();

        this.addMouseListener(is);
        this.addMouseMotionListener(is);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()) {
                    case 's':
                        save();
                        break;
                    case 'l':
                        load();
                        break;
                    default:
                        break;
                }
            }
        });

        // show
        this.setBounds();
        this.frame.setLocationRelativeTo(null);
        this.frame.setVisible(true);
    }

    private void setBounds() {
        this.frame.getContentPane().setPreferredSize(new Dimension((cols + 1) * tileSize + padding, rows * tileSize));
        this.frame.pack();

        di.setBounds(0, 0, (cols + 1) * tileSize + padding, rows * tileSize);
        mp.setBounds(0, 0, cols * tileSize, rows * tileSize);
        is.setBounds(cols * tileSize + padding, 0, tileSize, rows * tileSize);
    }

    private void save() {
        int[][] encodedTilesData = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                encodedTilesData[row][col] = new TileData(tileImageIndices[row][col],
                        is.collisional[tileImageIndices[row][col]] == 1).encodedData;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream("0.lvl");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(encodedTilesData);
        } catch (IOException e) {
            System.out.println("write file failed");
        }
    }

    private void load() {
        try {
            FileInputStream fis = new FileInputStream("0.lvl");
            ObjectInputStream ois = new ObjectInputStream(fis);

            int[][] encodedTilesData = (int[][])ois.readObject();

            if (encodedTilesData.length > 0) {
                int[][] imageIndices = new int[encodedTilesData.length][encodedTilesData[0].length];
                int[] collisional = new int[images.size()];

                for (int row = 0; row < encodedTilesData.length; row++) {
                    for (int col = 0; col < encodedTilesData[row].length; col++) {
                        TileData tileData = new TileData(encodedTilesData[row][col]);
                        imageIndices[row][col] = tileData.imageIndex;
                        collisional[tileData.imageIndex] = tileData.isCollisional ? 1 : 0;
                    }
                }

                this.tileImageIndices = imageIndices;
                is.collisional = collisional;
                rows = encodedTilesData.length;
                cols = encodedTilesData[0].length;

                this.setBounds();
                repaint();
            }
        } catch (IOException e) {
            System.out.println("read file failed");
        } catch (ClassNotFoundException e) {
            System.out.println("invalid file format");
        }
    }

    class DraggingImage extends JPanel {
        @Override
        public void paint(Graphics graphics) {
            // graphics.getColor();
            graphics.setColor(new Color(255, 0, 0));
            if (is.pressedImageIndex != -1) {
                graphics.drawImage(images.get(is.pressedImageIndex), is.draggingX, is.draggingY, tileSize, tileSize, this);
                graphics.drawRect(is.draggingX, is.draggingY, tileSize, tileSize);
            }
        }
    }

    class ImageSelector extends JPanel implements MouseListener, MouseMotionListener {
        private int[] collisional;
        int pressedImageIndex = -1;
        int draggingX = 0;
        int draggingY = 0;
        int droppingCol = -1;
        int droppingRow = -1;

        private int draggedX = 0;
        private int draggedY = 0;
        private int pressedX = 0;
        private int pressedY = 0;

        ImageSelector() {
            collisional = new int[images.size()];
        }

        @Override
        public void paint(Graphics graphics) {
            graphics.setColor(new Color(255, 0, 0));

            int offsetY = 0;

            for (int i = 0; i < images.size(); i++) {
                graphics.drawImage(images.get(i), 0, offsetY, tileSize, tileSize, this);
                graphics.drawString(String.valueOf(collisional[i]), 0, offsetY + tileSize / 2);

                offsetY += tileSize;
            }
        }

        @Override
        public void mouseMoved(MouseEvent mouseEvent) {}

        @Override
        public void mouseDragged(MouseEvent mouseEvent) {
            draggedX = mouseEvent.getX() - pressedX;
            draggedY = mouseEvent.getY() - pressedY;
            draggingX = tileSize * cols + padding + draggedX;
            draggingY = pressedImageIndex * tileSize + draggedY;

            // determine dropping tile
            droppingCol = (draggingX % tileSize) > (tileSize / 2) ? (draggingX / tileSize + 1) : (draggingX / tileSize);
            droppingRow = (draggingY % tileSize) > (tileSize / 2) ? (draggingY / tileSize + 1) : (draggingY / tileSize);

            if (droppingCol >= cols) {
                droppingCol = -1;
            }

            if (droppingRow >= rows) {
                droppingRow = -1;
            }

            frame.repaint();
        }

        @Override
        public void mouseClicked(MouseEvent mouseEvent) {}

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
            pressedX = mouseEvent.getX();
            pressedY = mouseEvent.getY();

            int imageIndex = pressedY / tileSize;

            if (imageIndex < collisional.length && pressedX >= (tileSize * cols + padding)) {
                pressedImageIndex = imageIndex;
            } else {
                pressedImageIndex = -1;
            }
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
            if (pressedImageIndex != -1) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    collisional[pressedImageIndex] = collisional[pressedImageIndex] == 0 ? 1 : 0;
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    if (droppingRow != -1 && droppingCol != -1) {
                        tileImageIndices[droppingRow][droppingCol] = pressedImageIndex;
                    }
                }
            }

            pressedImageIndex = -1;
            droppingCol = -1;
            droppingRow = -1;
            frame.repaint();
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {}

        @Override
        public void mouseExited(MouseEvent mouseEvent) {}
    }

    class MapPreview extends JPanel {
        @Override
        public void paint(Graphics graphics) {
            int offsetX = 0;
            int offsetY = 0;

            for (int row = 0; row < tileImageIndices.length; row++) {
                for (int col = 0; col < tileImageIndices[row].length; col++) {
                    int imageIndex = tileImageIndices[row][col];

                    if (is.droppingRow == row && is.droppingCol == col) {
                        graphics.setColor(new Color(255, 0, 0));
                    } else {
                        graphics.setColor(new Color(0, 0, 0));
                    }

                    graphics.drawImage(images.get(imageIndex), offsetX, offsetY, tileSize, tileSize, this);
                    graphics.drawRect(offsetX, offsetY, tileSize - 1, tileSize - 1);

                    offsetX += tileSize;
                }

                offsetY += tileSize;
                offsetX = 0;
            }
        }
    }
}
