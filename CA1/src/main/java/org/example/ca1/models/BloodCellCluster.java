package org.example.ca1.models;

import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class BloodCellCluster extends BloodCell {
    private final List<BloodCell> constituentCells;

    public BloodCellCluster(BloodCell firstCell) {
        super(CellType.RED, firstCell.getBoundingBox(), firstCell.getPixelCount());
        this.constituentCells = new ArrayList<>();
        this.constituentCells.add(firstCell);
        super.markAsCluster();
    }

    public BloodCellCluster(int x, int y, int width, int height, int pixelCount) {
        super(CellType.RED, x, y, width, height, pixelCount);
        this.constituentCells = new ArrayList<>();
        super.markAsCluster();
    }

    public void addCell(BloodCell cell) {
        constituentCells.add(cell);
        // Update bounding box to include the new cell
        Rectangle currentBounds = getBoundingBox();
        Rectangle cellBounds = cell.getBoundingBox();

        double minX = Math.min(currentBounds.getX(), cellBounds.getX());
        double minY = Math.min(currentBounds.getY(), cellBounds.getY());
        double maxX = Math.max(currentBounds.getX() + currentBounds.getWidth(),
                cellBounds.getX() + cellBounds.getWidth());
        double maxY = Math.max(currentBounds.getY() + currentBounds.getHeight(),
                cellBounds.getY() + cellBounds.getHeight());

        currentBounds.setX(minX);
        currentBounds.setY(minY);
        currentBounds.setWidth(maxX - minX);
        currentBounds.setHeight(maxY - minY);
    }

    public List<BloodCell> getConstituentCells() {
        return new ArrayList<>(constituentCells);
    }

    @Override
    public double calculateApproxCellCount() {
        if (!constituentCells.isEmpty()) {
            return constituentCells.size();
        }
        // Fallback calculation for clusters detected as single blobs
        return super.calculateApproxCellCount();
    }
}