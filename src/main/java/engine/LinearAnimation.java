package engine;

import java.awt.image.BufferedImage;

public class LinearAnimation extends Animation {
    private float directX;
    private float directY;
    private double slope;
    private int endX;
    private int endY;

    public LinearAnimation(BufferedImage image, int x, int y, int endX, int endY, double speed) {
        super(new AnimationConfig(image, x, y,
                image.getWidth(null),
                image.getHeight(null)),
                speed);

        directX = Math.signum(endX - x);
        directY = Math.signum(endY - y);
        slope = (endY - y) * 1.0 / (endX - x);

        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public AnimationConfig requestAnimationConfig(long pastMillSeconds) {
        AnimationConfig config = new AnimationConfig(current);
        double step = (pastMillSeconds / 100.0 * speed) * directX;

        config.setX((int)Math.round(step + from.getX()));
        config.setY((int)Math.round(step * slope + from.getY()));

        // check boundary
        if (config.getX() * directX > endX || config.getY() * directY > endY) {
            return null;
        }

        return config;
    }
}
