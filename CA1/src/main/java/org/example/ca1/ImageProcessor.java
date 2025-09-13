package org.example.ca1;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.example.ca1.models.CellType;

public class ImageProcessor {

    public static Image convertToTricolor(Image original, double redHueMin, double redHueMax,
                                          double whiteHueMin, double whiteHueMax,
                                          double redSaturationThreshold, double whiteSaturationThreshold) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();

        WritableImage tricolorImage = new WritableImage(width, height);
        PixelWriter writer = tricolorImage.getPixelWriter();
        PixelReader reader = original.getPixelReader();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color originalColor = reader.getColor(x, y);
                Color newColor = classifyPixel(originalColor, redHueMin, redHueMax, whiteHueMin, whiteHueMax,
                        redSaturationThreshold, whiteSaturationThreshold);
                writer.setColor(x, y, newColor);
            }
        }

        return tricolorImage;
    }

    private static Color classifyPixel(Color color, double redHueMin, double redHueMax,
                                       double whiteHueMin, double whiteHueMax,
                                       double redSaturationThreshold, double whiteSaturationThreshold) {
        double hue = color.getHue();
        double saturation = color.getSaturation();
        double brightness = color.getBrightness();


        //red hue range
        if (isInHueRange(hue, redHueMin, redHueMax) && saturation >= redSaturationThreshold) {
            return Color.RED; // Red blood cell
        }

        //white blood cell, purple hue
        else if (isInHueRange(hue, whiteHueMin, whiteHueMax) && saturation >= whiteSaturationThreshold) {
            return Color.PURPLE;
        }

        // background
        return Color.WHITE;
    }


    private static boolean isInHueRange(double hue, double min, double max) {
        // If the range doesn't cross the 0/360 boundary
        if (min <= max) {
            return hue >= min && hue <= max;
        }
        // If the range crosses the 0/360 boundary (e.g., 350-30)
        else {
            return hue >= min || hue <= max;
        }
    }
}