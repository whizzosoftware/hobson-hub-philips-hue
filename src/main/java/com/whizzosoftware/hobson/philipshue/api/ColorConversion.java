/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Hue color conversion convenience methods.
 *
 * This was converted from Philips Objective-C code posted here:
 *
 * https://github.com/PhilipsHue/PhilipsHueSDK-iOS-OSX/blob/master/ApplicationDesignNotes/RGB%20to%20xy%20Color%20conversion.md
 */
public class ColorConversion {
    private static final int cptRED = 0;
    private static final int cptGREEN = 1;
    private static final int cptBLUE = 2;

    static public Color colorFromXY(PointF xy, String model) {
        List<PointF> colorPoints = colorPointsForModel(model);
        boolean inReachOfLamps = checkPointInLampsReach(xy, colorPoints);

        if (!inReachOfLamps) {
            // It seems the colour is out of reach; let's find the closest colour we can produce with our lamp and
            // send this XY value out.

            // Find the closest point on each line in the triangle
            PointF pAB = getClosestPointToPoints(colorPoints.get(cptRED), colorPoints.get(cptGREEN), xy);
            PointF pAC = getClosestPointToPoints(colorPoints.get(cptBLUE), colorPoints.get(cptRED), xy);
            PointF pBC = getClosestPointToPoints(colorPoints.get(cptGREEN), colorPoints.get(cptBLUE), xy);

            // Get the distances per point and see which point is closer to our Point
            float dAB = getDistanceBetweenTwoPoints(xy, pAB);
            float dAC = getDistanceBetweenTwoPoints(xy, pAC);
            float dBC = getDistanceBetweenTwoPoints(xy, pBC);

            float lowest = dAB;
            PointF closestPoint = pAB;

            if (dAC < lowest) {
                lowest = dAC;
                closestPoint = pAC;
            }
            if (dBC < lowest) {
                closestPoint = pBC;
            }

            // Change the xy value to a value which is within the reach of the lamp
            xy.x = closestPoint.x;
            xy.y = closestPoint.y;
        }

        float x = xy.x;
        float y = xy.y;
        float z = 1.0f - x - y;

        float Y = 1.0f;
        float X = (Y / y) * x;
        float Z = (Y / y) * z;

        // sRGB D65 conversion
        float r = X  * 3.2406f - Y * 1.5372f - Z * 0.4986f;
        float g = -X * 0.9689f + Y * 1.8758f + Z * 0.0415f;
        float b = X  * 0.0557f - Y * 0.2040f + Z * 1.0570f;

        if (r > b && r > g && r > 1.0f) {
            // red is too big
            g = g / r;
            b = b / r;
            r = 1.0f;
        }
        else if (g > b && g > r && g > 1.0f) {
            // green is too big
            r = r / g;
            b = b / g;
            g = 1.0f;
        }
        else if (b > r && b > g && b > 1.0f) {
            // blue is too big
            r = r / b;
            g = g / b;
            b = 1.0f;
        }

        // Apply gamma correction
        r = r <= 0.0031308f ? 12.92f * r : (1.0f + 0.055f) * (float)Math.pow((double)r, (double)((1.0f / 2.4f))) - 0.055f;
        g = g <= 0.0031308f ? 12.92f * g : (1.0f + 0.055f) * (float)Math.pow((double)g, (double)((1.0f / 2.4f))) - 0.055f;
        b = b <= 0.0031308f ? 12.92f * b : (1.0f + 0.055f) * (float)Math.pow((double)b, (double)((1.0f / 2.4f))) - 0.055f;

        if (r > b && r > g) {
            // red is biggest
            if (r > 1.0f) {
                g = g / r;
                b = b / r;
                r = 1.0f;
            }
        }
        else if (g > b && g > r) {
            // green is biggest
            if (g > 1.0f) {
                r = r / g;
                b = b / g;
                g = 1.0f;
            }
        }
        else if (b > r && b > g) {
            // blue is biggest
            if (b > 1.0f) {
                r = r / b;
                g = g / b;
                b = 1.0f;
            }
        }

        return new Color(r, g, b);
    }

    static public PointF calculateXY(int aRed, int aGreen, int aBlue, String model) {
        double normalizedRed = (double)aRed / 255.0;
        double normalizedGreen = (double)aGreen / 255.0;
        double normalizedBlue = (double)aBlue / 255.0;

        // apply gamma correction
        float red = (normalizedRed > 0.04045f) ? (float)Math.pow((normalizedRed + 0.055) / (1.0 + 0.055), 2.4) : (float)(normalizedRed / 12.92);
        float green = (normalizedGreen > 0.04045) ? (float)Math.pow((normalizedGreen + 0.055) / (1.0 + 0.055), 2.4) : (float)(normalizedGreen / 12.92);
        float blue = (normalizedBlue > 0.04045) ? (float)Math.pow((normalizedBlue + 0.055) / (1.0 + 0.055), 2.4) : (float)(normalizedBlue / 12.92);

        // wide gamut conversion D65
        float X = (red * 0.649926f + green * 0.103455f + blue * 0.197109f);
        float Y = (red * 0.234327f + green * 0.743075f + blue * 0.022598f);
        float Z = (red * 0.0000000f + green * 0.053077f + blue * 1.035763f);

        float cx = X / (X + Y + Z);
        float cy = Y / (X + Y + Z);

        // TODO: check for NaN

        PointF xyPoint = new PointF(cx, cy);
        List<PointF> colorPoints = colorPointsForModel(model);
        boolean inReachOfLamps = checkPointInLampsReach(xyPoint, colorPointsForModel(model));

        if (!inReachOfLamps) {
            // It seems the colour is out of reach let's find the closest colour we can produce with our lamp and
            // send this XY value out

            // Find the closest point on each line in the triangle
            PointF pAB = getClosestPointToPoints(colorPoints.get(cptRED), colorPoints.get(cptGREEN), xyPoint);
            PointF pAC = getClosestPointToPoints(colorPoints.get(cptBLUE), colorPoints.get(cptRED), xyPoint);
            PointF pBC = getClosestPointToPoints(colorPoints.get(cptGREEN), colorPoints.get(cptBLUE), xyPoint);

            // Get the distances per point and see which point is closer to our Point
            float dAB = getDistanceBetweenTwoPoints(xyPoint, pAB);
            float dAC = getDistanceBetweenTwoPoints(xyPoint, pAC);
            float dBC = getDistanceBetweenTwoPoints(xyPoint, pBC);

            float lowest = dAB;
            PointF closestPoint = pAB;

            if (dAC < lowest) {
                lowest = dAC;
                closestPoint = pAC;
            }
            if (dBC < lowest) {
                closestPoint = pBC;
            }

            // Change the xy value to a value which is within reach of the lamp
            cx = closestPoint.x;
            cy = closestPoint.y;
        }

        return new PointF(cx, cy);
    }

    static public List<PointF> colorPointsForModel(String model) {
        List<PointF> colorPoints = new ArrayList<PointF>();

        List<String> hueBulbs = Arrays.asList(
                "LCT001", // Hue A19
                "LCT002", // Hue BR30
                "LCT003"  // Hue GU10
        );
        List<String> livingColors = Arrays.asList(
                "LLC001", // Monet, Renoir, Mondriaan (gen II)
                "LLC005", // Bloom (gen II)
                "LLC006", // Iris (gen III)
                "LLC007", // Bloom, Aura (gen III)
                "LLC011", // Hue Bloom
                "LLC012", // Hue Bloom
                "LLC013", // Storylight
                "LST001"  // Light Strips
        );

        if (hueBulbs.contains(model)) {
            // Hue bulbs color gamut triangle
            colorPoints.add(new PointF(0.674f, 0.322f)); // Red
            colorPoints.add(new PointF(0.408f, 0.517f)); // Green
            colorPoints.add(new PointF(0.168f, 0.041f)); // Blue
        } else if (livingColors.contains(model)) {
            // LivingColors color gamut triangle
            colorPoints.add(new PointF(0.703f, 0.296f)); // Red
            colorPoints.add(new PointF(0.214f, 0.709f)); // Green
            colorPoints.add(new PointF(0.139f, 0.081f)); // Blue
        } else {
            // Default construct triangle which contains all values
            colorPoints.add(new PointF(1.0f, 0.0f)); // Red
            colorPoints.add(new PointF(0.0f, 1.0f)); // Green
            colorPoints.add(new PointF(0.0f, 0.0f)); // Blue
        }

        return colorPoints;
    }

    static public PointF getClosestPointToPoints(PointF A, PointF B, PointF P) {
        PointF AP = new PointF(P.x - A.x, P.y - A.y);
        PointF AB = new PointF(B.x - A.x, B.y - A.y);
        float ab2 = AB.x * AB.x + AB.y * AB.y;
        float ap_ab = AP.x * AB.x + AP.y * AB.y;

        float t = ap_ab / ab2;

        if (t < 0.0f) {
            t = 0.0f;
        } else if (t > 1.0f) {
            t = 1.0f;
        }

        return new PointF(A.x + AB.x * t, A.y + AB.y * t);
    }

    static public float getDistanceBetweenTwoPoints(PointF one, PointF two) {
        float dx = one.x - two.x; // horizontal difference
        float dy = one.y - two.y; // vertical difference
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    static public boolean checkPointInLampsReach(PointF p, List<PointF> colorPoints) {
        PointF red = colorPoints.get(cptRED);
        PointF green = colorPoints.get(cptGREEN);
        PointF blue = colorPoints.get(cptBLUE);

        PointF v1 = new PointF(green.x - red.x, green.y - red.y);
        PointF v2 = new PointF(blue.x - red.x, blue.y - red.y);

        PointF q = new PointF(p.x - red.x, p.y - red.y);

        float s = crossProduct(q, v2) / crossProduct(v1, v2);
        float t = crossProduct(v1, q) / crossProduct(v1, v2);

        return (s >= 0.0f) && (t >= 0.0f) && (s + t <= 1.0f);
    }

    static public float crossProduct(PointF p1, PointF p2) {
        return (p1.x * p2.y - p1.y * p2.x);
    }

    static public Color createColorFromRGBString(String color) {
        if (color.startsWith("rgb(") && color.endsWith(")")) {
            StringTokenizer tok = new StringTokenizer(color.substring(4, color.length() - 1), ",");
            return new Color(
                    Integer.parseInt(tok.nextToken().trim()),
                    Integer.parseInt(tok.nextToken().trim()),
                    Integer.parseInt(tok.nextToken().trim())
            );
        }
        return null;
    }

    /**
     * A color convenience class.
     */
    static public class Color {
        public int r;
        public int g;
        public int b;

        /**
         * Constructor.
         *
         * @param r the value of red as a float between 0 and 1
         * @param g the value of green as a float between 0 and 1
         * @param b the value of blue as a float between 0 and 1
         */
        public Color(float r, float g, float b) {
            this.r = Math.max((int)Math.ceil(r * 255), 0);
            this.g = Math.max((int)Math.ceil(g * 255), 0);
            this.b = Math.max((int)Math.ceil(b * 255), 0);
        }

        /**
         * Constructor.
         *
         * @param r the value of red as an int between 0 and 255
         * @param g the value of green as an int between 0 and 255
         * @param b the value of blue as an int between 0 and 255
         */
        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    /**
     * A point convenience class.
     */
    static public class PointF {
        public float x;
        public float y;

        public PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
