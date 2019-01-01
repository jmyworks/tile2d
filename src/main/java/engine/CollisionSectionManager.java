package koumuu.game.engine;

import java.util.ArrayList;

public class CollisionSectionManager {
    private int sectionSize;
    private int sectionsPerRow;
    private int sectionsPerCol;
    private ArrayList<CollisionSection> sections = new ArrayList<CollisionSection>();

    public CollisionSectionManager(int width, int height, int sectionSize) {
        this.sectionSize = sectionSize;
        this.sectionsPerRow = (int)Math.ceil(width * 1.0 / sectionSize);
        this.sectionsPerCol = (int)Math.ceil(height * 1.0 / sectionSize);

        this.rebuild();
    }

    public void rebuild() {
        sections = new ArrayList<CollisionSection>();

        for (int row = 0; row < sectionsPerCol; row++) {
            for (int col = 0; col < sectionsPerRow; col++) {
                sections.add(row * sectionsPerRow + col, new CollisionSection());
            }
        }
    }

    public void addToSection(CollisionComponent component) {
        int minX = component.getLeft();
        int minY = component.getTop();
        int maxX = component.getRight();
        int maxY = component.getBottom();

        try {
            if ((minX > -1 && minY > -1 && maxX > -1 && maxY > -1) && minX < maxX && minY < maxY) {
                sections.get(getSectionIndex(minX + 0.1, minY + 0.1)).addComponent(component);
                sections.get(getSectionIndex(minX + 0.1, maxY - 0.1)).addComponent(component);
                sections.get(getSectionIndex(maxX - 0.1, minY + 0.1)).addComponent(component);
                sections.get(getSectionIndex(maxX - 0.1, maxY - 0.1)).addComponent(component);
            }
        } catch (IndexOutOfBoundsException e) {}
    }

    public void checkCollisions() {
        ArrayList<CollisionSection> allPossiblyCollisionSections = getAllPossiblyCollisionSections();

        for (CollisionSection section: allPossiblyCollisionSections) {
            // check collision by pixels
            ArrayList<CollisionComponent> components = section.getComponents();

            for (CollisionComponent component1: components) {
                for (CollisionComponent component2: components) {
                    if (component1 != component2) {
                        if (checkComponentPixels(component1, component2)) {
                            component1.inCollision(component2);
                            component2.inCollision(component1);
                        }
                    }
                }
            }
        }
    }

    public boolean checkComponentPixels(CollisionComponent component1, CollisionComponent component2) {
        // intersection
        int intersectionLeft = Math.max(component1.getLeft(), component2.getLeft());
        int intersectionRight = Math.min(component1.getRight(), component2.getRight());
        int intersectionTop = Math.max(component1.getTop(), component2.getTop());
        int intersectionBottom = Math.min(component1.getBottom(), component2.getBottom());
        int intersectionWidth = intersectionRight - intersectionLeft;
        int intersectionHeight = intersectionBottom - intersectionTop;

        // is intersection a valid rectangle?
        if (intersectionWidth <= 0 || intersectionHeight <= 0) {
            return false;
        }

        int[] intersectionImageData1 = component1.getImage()
                .getRGB(Math.max(0, intersectionLeft - component1.getLeft()),
                        Math.max(0, intersectionTop - component1.getTop()),
                        intersectionWidth, intersectionHeight,
                        null, 0, intersectionWidth);

        int[] intersectionImageData2 = component2.getImage()
                .getRGB(Math.max(0, intersectionLeft - component2.getLeft()),
                        Math.max(0, intersectionTop - component2.getTop()),
                        intersectionWidth, intersectionHeight,
                        null, 0, intersectionWidth);

        for (int i = 0; i < intersectionImageData1.length; i++) {
            int color1 = intersectionImageData1[i];
            int color2 = intersectionImageData2[i];

            if (color1 != 0 && color2 != 0) {
                return true;
            }
        }

        return false;
    }

    private ArrayList<CollisionSection> getAllPossiblyCollisionSections() {
        ArrayList<CollisionSection> allPossiblyCollisionSections = new ArrayList<CollisionSection>();

        for (CollisionSection section: sections) {
            if (section.getSize() > 1) {
                allPossiblyCollisionSections.add(section);
            }
        }

        return allPossiblyCollisionSections;
    }

    private int getSectionIndex(double x, double y) {
        return (int)Math.floor(y * 1.0 / sectionSize) * sectionsPerRow + (int)Math.floor(x * 1.0 / sectionSize);
    }
}
