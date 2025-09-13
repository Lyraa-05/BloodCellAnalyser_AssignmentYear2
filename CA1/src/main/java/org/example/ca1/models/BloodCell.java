package org.example.ca1.models;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BloodCell {
    private final CellType type;
    private final Rectangle boundingBox;
    private final int pixelCount;
    private int id;
    private boolean isCluster;

    public BloodCell(CellType type, Rectangle boundingBox, int pixelCount) {
        this.type = type;
        this.boundingBox = boundingBox;
        this.pixelCount = pixelCount;
        this.isCluster = false;
    }

    public BloodCell(CellType type, int x, int y, int width, int height, int pixelCount) {
        this.type = type;
        this.boundingBox = new Rectangle(x, y, width, height);
        this.pixelCount = pixelCount;
        this.isCluster = false;
    }

    public CellType getType() { return type; }
    public Rectangle getBoundingBox() { return boundingBox; }
    public int getPixelCount() { return pixelCount; }
    public int getId() { return id; }
    public boolean isCluster() { return isCluster; }

    public void setId(int id) { this.id = id; }
    public void markAsCluster() { this.isCluster = true; }

    public Color getDisplayColor() {
        return type.getDisplayColor(isCluster);
    }

    public double calculateApproxCellCount() {
        if (type == CellType.RED) {
            double avgCellArea = 100;
            return pixelCount / avgCellArea;
        }
        return 1; // White blood cells always individual
    }
}