/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.disco;

import com.whizzosoftware.hobson.api.disco.DeviceAdvertisement;
import com.whizzosoftware.hobson.api.disco.DeviceAdvertisementListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * An DeviceAdvertisementListener implementation that can detect Philips Hue bridges via SSDP.
 *
 * @author Dan Noguerol
 */
public class HueBridgeAdvertisementListener implements DeviceAdvertisementListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String PROTOCOL_ID = "ssdp";

    private final List<String> discoveredAddresses = new ArrayList<String>();
    private HueBridgeListener listener;

    public HueBridgeAdvertisementListener(HueBridgeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDeviceAdvertisement(DeviceAdvertisement advertisement) {
        String data = advertisement.getData();
        if (advertisement.getProtocolId().equals(PROTOCOL_ID) && data.startsWith("NOTIFY *") && data.contains("FreeRTOS/") && data.contains("IpBridge/")) {
            int ix1 = data.indexOf("LOCATION: ");
            if (ix1 > -1) {
                ix1 += 10;
                int ix2 = data.indexOf("\n", ix1);
                URI uri = URI.create(data.substring(ix1, ix2).trim());
                String address = uri.getHost();
                if (address != null) {
                    boolean isNew = false;
                    synchronized (discoveredAddresses) {
                        if (!discoveredAddresses.contains(address)) {
                            discoveredAddresses.add(address);
                            isNew = true;
                        }
                    }
                    if (isNew) {
                        logger.info("Found Hue bridge at {}", address);
                        if (listener != null) {
                            listener.onHueHubFound(address);
                        }
                    }
                }
            } else {
                logger.debug("Found possible Hue bridge but no LOCATION header found");
            }
        }
    }
}
