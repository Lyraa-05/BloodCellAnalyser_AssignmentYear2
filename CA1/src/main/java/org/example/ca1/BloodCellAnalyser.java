package org.example.ca1;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.ca1.models.BloodCell;
import org.example.ca1.models.BloodCellCluster;
import org.example.ca1.models.CellType;

import java.util.*;

public class BloodCellAnalyser {
    private static final int MIN_RED_CELL_PIXELS = 50;
    private static final int MIN_WHITE_CELL_PIXELS = 200;
    private static final int CLUSTER_DISTANCE_THRESHOLD = 20;

    public static List<BloodCell> analyzeImage(Image tricolorImage) {
        PixelReader reader = tricolorImage.getPixelReader();
        int width = (int) tricolorImage.getWidth();
        int height = (int) tricolorImage.getHeight();

        UnionFind uf = new UnionFind(width * height);
        detectConnectedComponents(reader, width, height, uf);

        Map<Integer, BloodCell> cellMap = identifyCells(reader, width, height, uf);

        return formClusters(new ArrayList<>(cellMap.values()));
    }

    //union find implementation
    private static void detectConnectedComponents(PixelReader reader, int width, int height, UnionFind uf) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                int currentIdx = y * width + x;

                if (pixel.equals(Color.RED)) {
                    connectNeighbors(reader, width, height, x, y, Color.RED, uf, currentIdx);
                } else if (pixel.equals(Color.PURPLE)) {
                    connectNeighbors(reader, width, height, x, y, Color.PURPLE, uf, currentIdx);
                }
            }
        }
    }

    private static void connectNeighbors(PixelReader reader, int width, int height,
                                         int x, int y, Color targetColor,
                                         UnionFind uf, int currentIdx) {
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                Color neighbor = reader.getColor(nx, ny);
                if (neighbor.equals(targetColor)) {
                    int neighborIdx = ny * width + nx;
                    uf.union(currentIdx, neighborIdx);
                }
            }
        }
    }

    //convert pixel groups to bloodcell objects
    private static Map<Integer, BloodCell> identifyCells(PixelReader reader, int width,
                                                         int height, UnionFind uf) {
        Map<Integer, BloodCell> cellMap = new HashMap<>();
        int[] minX = new int[width * height];
        int[] minY = new int[width * height];
        int[] maxX = new int[width * height];
        int[] maxY = new int[width * height];
        int[] pixelCounts = new int[width * height];
        Arrays.fill(minX, Integer.MAX_VALUE);
        Arrays.fill(minY, Integer.MAX_VALUE);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                if (pixel.equals(Color.RED) || pixel.equals(Color.PURPLE)) {
                    int idx = y * width + x;
                    int root = uf.find(idx);

                    minX[root] = Math.min(minX[root], x);
                    minY[root] = Math.min(minY[root], y);
                    maxX[root] = Math.max(maxX[root], x);
                    maxY[root] = Math.max(maxY[root], y);
                    pixelCounts[root]++;
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                if (pixel.equals(Color.RED) || pixel.equals(Color.PURPLE)) {
                    int idx = y * width + x;
                    int root = uf.find(idx);

                    if (!cellMap.containsKey(root)) {
                        CellType type = pixel.equals(Color.RED) ? CellType.RED : CellType.WHITE;
                        int minPixelThreshold = type == CellType.RED ? MIN_RED_CELL_PIXELS : MIN_WHITE_CELL_PIXELS;

                        if (pixelCounts[root] >= minPixelThreshold) {
                            Rectangle bounds = new Rectangle(
                                    minX[root], minY[root],
                                    maxX[root] - minX[root] + 1,
                                    maxY[root] - minY[root] + 1
                            );
                            cellMap.put(root, new BloodCell(type, bounds, pixelCounts[root]));
                        }
                    }
                }
            }
        }

        return cellMap;
    }

    //cluster nearby red blood cells to BloodCellCluster
    private static List<BloodCell> formClusters(List<BloodCell> detectedCells) {
        List<BloodCell> results = new ArrayList<>();
        List<BloodCellCluster> clusters = new ArrayList<>();

        // Separate red and white cells
        List<BloodCell> redCells = new ArrayList<>();
        List<BloodCell> whiteCells = new ArrayList<>();

        for (BloodCell cell : detectedCells) {
            if (cell.getType() == CellType.RED) {
                redCells.add(cell);
            } else {
                whiteCells.add(cell);
            }
        }

        // Cluster red cells
        for (BloodCell cell : redCells) {
            boolean addedToCluster = false;

            for (BloodCellCluster cluster : clusters) {
                if (isWithinClusterDistance(cluster, cell)) {
                    cluster.addCell(cell);
                    addedToCluster = true;
                    break;
                }
            }

            if (!addedToCluster) {
                BloodCellCluster newCluster = new BloodCellCluster(cell);
                clusters.add(newCluster);
            }
        }

        results.addAll(clusters);
        results.addAll(whiteCells);

        return results;
    }

    //determine if red cell is near cluster
    private static boolean isWithinClusterDistance(BloodCellCluster cluster, BloodCell cell) {
        Rectangle clusterBounds = cluster.getBoundingBox();
        Rectangle cellBounds = cell.getBoundingBox();

        double centerX1 = clusterBounds.getX() + clusterBounds.getWidth() / 2;
        double centerY1 = clusterBounds.getY() + clusterBounds.getHeight() / 2;
        double centerX2 = cellBounds.getX() + cellBounds.getWidth() / 2;
        double centerY2 = cellBounds.getY() + cellBounds.getHeight() / 2;

        double distance = Math.sqrt(Math.pow(centerX2 - centerX1, 2) + Math.pow(centerY2 - centerY1, 2));
        return distance <= CLUSTER_DISTANCE_THRESHOLD;
    }
}