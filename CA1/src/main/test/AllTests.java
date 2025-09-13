import org.example.ca1.ImageProcessor;
import org.example.ca1.UnionFind;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;




public class AllTests {

    //Union Find
    @Test
    public void unionFind_basicUnionAndFind() {
        UnionFind uf = new UnionFind(10);
        uf.union(1, 2);
        uf.union(2, 3);
        assertEquals(uf.find(1), uf.find(3), "1 and 3 should be connected");
    }

    @Test
    public void unionFind_countReduction() {
        UnionFind uf = new UnionFind(5);
        assertEquals(5, uf.getCount());
        uf.union(0, 1);
        assertEquals(4, uf.getCount());
        uf.union(1, 2);
        assertEquals(3, uf.getCount());
        uf.union(2, 3);
        assertEquals(2, uf.getCount());
        uf.union(3, 4);
        assertEquals(1, uf.getCount());
    }

    @Test
    public void unionFind_connectedLogic() {
        UnionFind uf = new UnionFind(6);
        uf.union(0, 1);
        uf.union(1, 2);
        uf.union(3, 4);
        assertTrue(uf.connected(0, 2), "0 and 2 should be connected");
        assertFalse(uf.connected(0, 3), "0 and 3 should not be connected");
    }

    @Test
    public void unionFind_sizeTracking() {
        UnionFind uf = new UnionFind(5);
        uf.union(0, 1);
        uf.union(1, 2);
        assertEquals(3, uf.getSize(0), "Size of component with root 0 should be 3");
        assertEquals(3, uf.getSize(2), "Size of component with root 2 should be 3");
        uf.union(3, 4);
        assertEquals(2, uf.getSize(4), "Size of component with root 4 should be 2");
    }

    //ImageProcessor
    @Test
    public void imageProcessor_convertToTricolor_redCellDetection() {
        //test image with a red cell
        WritableImage testImage = createTestImage(100, 100, (x, y) -> {
            // red blood cell in the middle
            if (x >= 40 && x < 60 && y >= 40 && y < 60) {
                return Color.hsb(0, 0.8, 0.7); // Red hue
            }
            return Color.WHITE;
        });

        Image result = ImageProcessor.convertToTricolor(testImage, 330, 30, 260, 300, 0.2, 0.3);

        // Check pixels were classified correctly
        PixelReader reader = result.getPixelReader();
        assertEquals(Color.RED, reader.getColor(50, 50), "Center pixel should be classified as RED");
        assertEquals(Color.WHITE, reader.getColor(20, 20), "Background pixel should be WHITE");
    }

    @Test
    public void imageProcessor_convertToTricolor_whiteCellDetection() {
        //test image with a white cell - purple colour
        WritableImage testImage = createTestImage(100, 100, (x, y) -> {
            // white blood cell in the middle
            if (x >= 40 && x < 60 && y >= 40 && y < 60) {
                return Color.hsb(280, 0.8, 0.7); // Purple hue
            }
            return Color.WHITE;
        });

        Image result = ImageProcessor.convertToTricolor(testImage, 330, 30, 260, 300, 0.2, 0.3);

        PixelReader reader = result.getPixelReader();
        assertEquals(Color.PURPLE, reader.getColor(50, 50), "Center pixel should be classified as PURPLE");
        assertEquals(Color.WHITE, reader.getColor(20, 20), "Background pixel should be WHITE");
    }

    @Test
    public void imageProcessor_convertToTricolor_crossBoundaryHueDetection() {
        // red cells that cross the 360/0 boundary (e.g., 350-30 degrees)
        WritableImage testImage = createTestImage(100, 100, (x, y) -> {
            if (x >= 30 && x < 50 && y >= 30 && y < 50) {
                return Color.hsb(350, 0.8, 0.7); // Reddish at 350 degrees
            } else if (x >= 60 && x < 80 && y >= 30 && y < 50) {
                return Color.hsb(15, 0.8, 0.7); // Reddish at 15 degrees
            }
            return Color.WHITE;
        });


        Image result = ImageProcessor.convertToTricolor(testImage, 330, 30, 260, 300, 0.2, 0.3);

        // Check both red cells are detected
        PixelReader reader = result.getPixelReader();
        assertEquals(Color.RED, reader.getColor(40, 40), "First cell should be RED");
        assertEquals(Color.RED, reader.getColor(70, 40), "Second cell should be RED");
    }

    // Helper method to create test images
    private interface PixelGenerator {
        Color getColor(int x, int y);
    }

    private WritableImage createTestImage(int width, int height, PixelGenerator generator) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter writer = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setColor(x, y, generator.getColor(x, y));
            }
        }

        return image;
    }
}