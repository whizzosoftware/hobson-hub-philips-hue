/*******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

import com.whizzosoftware.hobson.api.color.Color;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LightStateTest {
    @Test
    public void testColorConstructor() {
        LightState state = new LightState(true, new Color(5000, 100), null, null);
        assertEquals(200, (int)state.getColorTemperature());
        assertEquals(254, (int)state.getBrightness());
    }
}
