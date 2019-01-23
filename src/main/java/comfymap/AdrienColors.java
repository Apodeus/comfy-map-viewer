package comfymap;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public enum AdrienColors {
    DEEP_WATER(240f/360f, -36f/360f, 1f, -0.6f, 0.6f, 0.4f, -200),
    WATER(204f/360f, -164f/360f, 0.4f, 0.1f, 1, 0, 0),
    BEACH(40f/360f, 80f/360f, 0.5f, 0.5f, 1, -0.3f, 45),
    PLAIN(120f/360f, -90f/360f, 1, -0.5f, 0.7f, 0.1f, 400),
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
        //Arrays.sort(values, Comparator.comparingInt(AdrienColors::getMaxHeight));
        AdrienColors resultScheme = WATER;
        int lowCap = WATER.maxHeight;
        int topCap = BEACH.maxHeight;

        for(int i = 0; i < values.length - 1; ++i){
            if(height > values[i].maxHeight && height <= values[i+1].maxHeight){
                resultScheme = values[i+1];
                lowCap = values[i].maxHeight;
                topCap = values[i+1].maxHeight;
                break;
            }
        }

        double hueRatio = 1f / (topCap - lowCap) * resultScheme.variationHue;
        double satRatio = 1f / (topCap - lowCap) * resultScheme.variationSat;
        double valRatio = 1f / (topCap - lowCap) * resultScheme.variationVal;

        double hue = resultScheme.initHue + (height - lowCap) * hueRatio;
        double sat = resultScheme.initSat + (height - lowCap) * satRatio;
        double val = resultScheme.initVal + (height - lowCap) * valRatio;

        //return HSVtoRGB(hue, sat, val);
        return Color.HSBtoRGB((float)hue, (float)sat, (float)val);
    }

    private static int HSVtoRGB(double hue, double sat, double val){
        int i = (int)Math.floor(hue  * 6);
        double f = hue * 6 - 1;
        double p = val * (1 - sat);
        double q = val * (1 - f * sat);
        double t = val * (1 - (1 - f) * sat);
        double r, g, b;
        switch(i % 6){
            case 0: r = val; g = t;   b = p; break;
            case 1: r = q;   g = val; b = p; break;
            case 2: r = p;   g = val; b = t; break;
            case 3: r = p;   g = q;   b = val; break;
            case 4: r = t;   g = p;   b = val; break;
            case 5: r = val; g = p;   b = q; break;
            default: r = val; g = t; b = p; break;
        }

        int red =   (int)Math.min(255,   Math.max(Math.round(r * 255), 0));
        int green = (int)Math.min(255,   Math.max(Math.round(g * 255), 0));
        int blue =  (int)Math.min(255,   Math.max(Math.round(b * 255), 0));
        return new Color(red, green, blue).getRGB();
    }
}
