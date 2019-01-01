package koumuu.game.engine;

import java.awt.image.BufferedImage;

public interface CollisionComponent {
    int getLeft();
    int getRight();
    int getTop();
    int getBottom();

    BufferedImage getImage();

    void inCollision(CollisionComponent component);
}
