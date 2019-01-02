package engine;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class RotateAnimation extends Animation {
    public RotateAnimation(BufferedImage image, int x, int y, int speed) {
        super(new AnimationConfig(image, x, y,
                image.getWidth(null),
                image.getHeight(null)),
                speed);
    }

    @Override
    public AnimationConfig requestAnimationConfig(long pastMillSeconds) {
        AnimationConfig config = new AnimationConfig(current);

        AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians((pastMillSeconds / 100.0 * speed) % 360), config.getWidth() / 2.0, config.getHeight() / 2.0);
        AffineTransformOp ao = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage rotatedImage = ao.filter(from.getImage(), null);

        config.setImage(rotatedImage);
        config.setWidth(rotatedImage.getWidth());
        config.setHeight(rotatedImage.getHeight());

        return config;
    }
}
