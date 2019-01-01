package koumuu.game.engine;

import java.util.ArrayList;

public class CollisionSection {
    private ArrayList<CollisionComponent> components = new ArrayList<CollisionComponent>();

    public void addComponent(CollisionComponent component) {
        if (!components.contains(component)) {
            components.add(component);
        }
    }

    public int getSize() {
        return components.size();
    }

    public ArrayList<CollisionComponent> getComponents() {
        return components;
    }
}
