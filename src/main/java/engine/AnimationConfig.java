package engine;

import java.awt.image.BufferedImage;

public class AnimationConfig {
    private BufferedImage image;
    private int x;
    private int y;
    private int width;
    private int height;

    private boolean imageChanged = false;
    private boolean xChanged = false;
    private boolean yChanged = false;
    private boolean widthChanged = false;
    private boolean heightChanged = false;

    public AnimationConfig(BufferedImage image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AnimationConfig(AnimationConfig animationConfig) {
        if (animationConfig != null) {
            this.image = animationConfig.image;
            this.x = animationConfig.x;
            this.y = animationConfig.y;
            this.width = animationConfig.width;
            this.height = animationConfig.height;

            imageChanged = animationConfig.imageChanged;
            xChanged = animationConfig.xChanged;
            yChanged = animationConfig.yChanged;
            widthChanged = animationConfig.widthChanged;
            heightChanged = animationConfig.heightChanged;
        }
    }

    public void merge(AnimationConfig config) {
        if (config == null) {
            return;
        }

        if (config.imageChanged) {
            setImage(config.image);
        }

        if (config.xChanged) {
            setX(config.x);
        }

        if (config.yChanged) {
            setY(config.y);
        }

        if (config.widthChanged) {
            setWidth(config.width);
        }

        if (config.heightChanged) {
            setHeight(config.height);
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setImage(BufferedImage image) {
        this.imageChanged = true;
        this.image = image;
    }

    public void setX(int x) {
        this.xChanged = true;
        this.x = x;
    }

    public void setY(int y) {
        this.yChanged = true;
        this.y = y;
    }

    public void setWidth(int width) {
        this.widthChanged = true;
        this.width = width;
    }

    public void setHeight(int height) {
        this.heightChanged = true;
        this.height = height;
    }
}
