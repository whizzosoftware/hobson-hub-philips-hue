/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import org.junit.Test;
import static org.junit.Assert.*;

public class ColorConversionTest {
    @Test
    public void testCalculateXY() {
        ColorConversion.PointF p = ColorConversion.calculateXY(255, 173, 90, "LCT002");
        assertEquals(0.513, p.x, 0.001);
        assertEquals(0.394, p.y, 0.001);
    }

    @Test
    public void testCreateColorFromRGBString() {
        ColorConversion.Color c = ColorConversion.createColorFromRGBString("rgb(255,0,0)");
        assertEquals(255, c.r);
        assertEquals(0, c.g);
        assertEquals(0, c.b);

        c = ColorConversion.createColorFromRGBString("rgb( 255 , 0 , 0 )");
        assertEquals(255, c.r);
        assertEquals(0, c.g);
        assertEquals(0, c.b);
    }
}
