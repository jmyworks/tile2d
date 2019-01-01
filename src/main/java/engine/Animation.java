package koumuu.game.engine;

import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class Animation implements CollisionComponent {
    protected AnimationConfig from;
    protected AnimationConfig current;
    protected double speed;

    private long startTimestamp;

    public Animation(AnimationConfig from, double speed) {
        this.from = from;
        this.current = new AnimationConfig(from);
        this.speed = speed;
    }

    public abstract AnimationConfig requestAnimationConfig(long pastMillSeconds);

    public void start() {
        startTimestamp = System.currentTimeMillis();
    }

    public void render(Graphics graphics) {
        long currentTimestamp = System.currentTimeMillis();
        long pastMillSeconds = currentTimestamp - startTimestamp;

        current = this.requestAnimationConfig(pastMillSeconds);

        if (current == null) {
            return;
        }

        graphics.drawImage(current.getImage(), current.getX(), current.getY(),
                current.getX() + current.getWidth(), current.getY() + current.getHeight(),
                0, 0, current.getWidth(), current.getHeight(), null);
    }

    @Override
    public int getLeft() {
        return current == null ? -1 : current.getX();
    }

    @Override
    public int getRight() {
        return current == null ? -1 : (current.getX() + current.getWidth());
    }

    @Override
    public int getTop() {
        return current == null ? -1 : current.getY();
    }

    @Override
    public int getBottom() {
        return current == null ? -1 : (current.getY() + current.getHeight());
    }

    @Override
    public BufferedImage getImage() {
        return current == null ? null : current.getImage();
    }

    @Override
    public void inCollision(CollisionComponent component) {
        // do nothing
    }
}
