package engine;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class CombinedAnimation extends Animation {
    private ArrayList<Animation> animations;

    public CombinedAnimation(Animation[] animations) {
        super(null, 0);

        this.animations = new ArrayList<Animation>(Arrays.asList(animations));
    }

    @Override
    public AnimationConfig requestAnimationConfig(long pastMillSeconds) {
        // merge animation configs
        AnimationConfig mergedConfig = new AnimationConfig(animations.get(0).current);

        for (Animation animation: animations) {
            AnimationConfig config = animation.requestAnimationConfig(pastMillSeconds);

            if (config == null) {
                return null;
            } else {
                mergedConfig.merge(config);
            }
        }

        return mergedConfig;
    }
}
