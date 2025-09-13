package org.example.ca1.models;

import javafx.scene.paint.Color;

public enum CellType {
    RED("Red Blood Cell",
            Color.rgb(200, 0, 0),
            Color.GREEN,
            Color.BLUE,
            0.3, 0.7),

    CLUSTER("Red Blood Cell Cluster",
            Color.rgb(200, 0, 0),
            Color.GREEN,
            Color.BLUE,
            0.3, 0.7),

    WHITE("White Blood Cell",
            Color.PURPLE,
            Color.PURPLE,
            Color.PURPLE,
            0.7, 0.9);

    private final String name;
    private final Color detectionColor;
    private final Color singleCellColor;
    private final Color clusterColor;
    private final double minHue;
    private final double maxHue;

    CellType(String name, Color detectionColor, Color singleCellColor,
             Color clusterColor, double minHue, double maxHue) {
        this.name = name;
        this.detectionColor = detectionColor;
        this.singleCellColor = singleCellColor;
        this.clusterColor = clusterColor;
        this.minHue = minHue;
        this.maxHue = maxHue;
    }

    public String getName() { return name; }

    public Color getDetectionColor() { return detectionColor; }

    public Color getDisplayColor(boolean isCluster) {
        return isCluster ? clusterColor : singleCellColor;
    }

    public boolean matches(Color pixelColor) {
        double hue = pixelColor.getHue() / 360.0;
        return hue >= minHue && hue <= maxHue &&
                pixelColor.getSaturation() > 0.3;
    }
}