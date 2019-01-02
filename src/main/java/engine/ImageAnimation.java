package engine;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageAnimation extends Animation {
    private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();

    public ImageAnimation(ArrayList<BufferedImage> images, int x, int y, double speed) {
        super(new AnimationConfig(images.get(0), x, y,
                images.get(0).getWidth(null),
                images.get(0).getHeight(null)),
                speed);

        this.images = images;
    }

    @Override
    public AnimationConfig requestAnimationConfig(long pastMillSeconds) {
        AnimationConfig config = new AnimationConfig(current);

        config.setImage(images.get((int)Math.round(pastMillSeconds / 100.0 * speed) % images.size()));

        return config;
    }
}
