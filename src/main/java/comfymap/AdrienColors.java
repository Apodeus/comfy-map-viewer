package comfymap;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;


//Z = 3 => Correspond au plus grand dezoom qu'on ait Z = 3 correspond à Z = 8
// Z = 11 => Correspond au niveau le plus zoomé Z = 11 correspond à Z = 0
public enum AdrienColors {
    DEEP_WATER(240f/360f, -36f/360f, 1f, -0.6f, 0.6f, 0.4f, -195),
    WATER(204f/360f, -164f/360f, 0.4f, 0.1f, 1, 0, 0),
    BEACH(40f/360f, 80f/360f, 0.5f, 0.5f, 1, -0.6f, 45),
    PLAIN(120f/360f, -90f/360f, 1, -0.5f, 0.4f, 0.4f, 400),
    LOW_MOUNTAIN(30f/360f, 0, 0.5f, 0.5f, 0.8f, -0.4f, 800),
    MOUNTAIN(30f/360f, 0, 1, -0.2f, 0.4f, 0.5f, 2000),
    HIGH_MOUNTAIN(30f/360f, 0, 0, 0, 0.9f, 0.1f, 8900);

    private final float initHue;
    private final float variationHue;
    private final float initSat;
    private final float variationSat;
    private final float initVal;
    private final float variationVal;
    private int maxHeight;

    AdrienColors(float initHue, float variationHue, float initSat, float variationSat, float initVal, float variationVal, int maxHeight){
        this.initHue = initHue;
        this.variationHue = variationHue;
        this.initSat = initSat;
        this.variationSat = variationSat;
        this.initVal = initVal;
        this.variationVal = variationVal;
        this.maxHeight = maxHeight;
    }

    public float getInitHue() {
        return initHue;
    }

    public float getVariationHue() {
        return variationHue;
    }

    public float getInitSat() {
        return initSat;
    }

    public float getVariationSat() {
        return variationSat;
    }

    public float getInitVal() {
        return initVal;
    }

    public float getVariationVal() {
        return variationVal;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public static int getRGBFromHeight(int height){
        AdrienColors[] values = AdrienColors.values();
        AdrienColors resultScheme = DEEP_WATER;
        int lowCap = DEEP_WATER.maxHeight; // -200
        int topCap = WATER.maxHeight; // 0
        if(height > 0) {
            for (int i = 0; i < values.length - 1; ++i) {
                if (height > values[i].maxHeight && height <= values[i + 1].maxHeight) {
                    resultScheme = values[i + 1];
                    lowCap = values[i].maxHeight;
                    topCap = values[i + 1].maxHeight;
                    break;
                }
            }
        }

        double hueRatio = 1f / (topCap - lowCap) * resultScheme.variationHue;
        double satRatio = 1f / (topCap - lowCap) * resultScheme.variationSat;
        double valRatio = 1f / (topCap - lowCap) * resultScheme.variationVal;

        double hue = resultScheme.initHue + (height - lowCap) * hueRatio;
        double sat = resultScheme.initSat + (height - lowCap) * satRatio;
        double val = resultScheme.initVal + (height - lowCap) * valRatio;

        return Color.HSBtoRGB((float)hue, (float)sat, (float)val);
    }
}
