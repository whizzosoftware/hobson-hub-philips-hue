/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.disco;

import com.whizzosoftware.hobson.api.disco.DeviceBridge;

/**
 * A local listener for bridge discoveries.
 *
 * TODO: needs to become a more generic OSGi-based mechanism; this is a stop-gap solution for now
 *
 * @author Dan Noguerol
 */
public interface HueBridgeListener {
    /**
     * Callback when a new bridge is found.
     *
     * @param bridge the discovered bridge
     */
    public void onHueHubFound(DeviceBridge bridge);
}
