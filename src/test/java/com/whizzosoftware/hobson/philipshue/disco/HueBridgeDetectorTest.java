/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.disco;

import com.whizzosoftware.hobson.api.disco.DeviceBridge;
import com.whizzosoftware.hobson.api.disco.DeviceBridgeDetectionContext;
import com.whizzosoftware.hobson.api.disco.DeviceBridgeMetaData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HueBridgeDetectorTest {
    @Test
    public void testDiscoveryAdd() {
        HueBridgeDetector hhda = new HueBridgeDetector();
        hhda.setPluginId("myplugin");
        MockDeviceBridgeDetectionContext context = new MockDeviceBridgeDetectionContext();
        DeviceBridgeMetaData entity = new DeviceBridgeMetaData("ssdp", "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: http://192.168.0.220:80/description.xml\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n");
        assertEquals(0, context.getIdentifiedEntities().size());
        assertTrue(hhda.identify(context, entity));
        assertEquals(1, context.getIdentifiedEntities().size());
        DeviceBridge e = context.getIdentifiedEntities().get(0);
        assertEquals("192.168.0.220", e.getValue());
        assertEquals("myplugin", e.getPluginId());
        assertEquals(HueBridgeDetector.ID, e.getType());
    }

    @Test
    public void testDiscoveryWithNoLocationHeader() {
        HueBridgeDetector hhda = new HueBridgeDetector();
        hhda.setPluginId("myplugin");
        MockDeviceBridgeDetectionContext context = new MockDeviceBridgeDetectionContext();
        DeviceBridgeMetaData entity = new DeviceBridgeMetaData("ssdp", "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n");
        assertEquals(0, context.getIdentifiedEntities().size());
        assertFalse(hhda.identify(context, entity));
        assertEquals(0, context.getIdentifiedEntities().size());
    }

    @Test
    public void testMalformedLocationHeader() {
        HueBridgeDetector hhda = new HueBridgeDetector();
        hhda.setPluginId("myplugin");
        MockDeviceBridgeDetectionContext context = new MockDeviceBridgeDetectionContext();
        DeviceBridgeMetaData entity = new DeviceBridgeMetaData("ssdp", "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: ht\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n");
        assertEquals(0, context.getIdentifiedEntities().size());
        assertTrue(hhda.identify(context, entity));
        assertEquals(0, context.getIdentifiedEntities().size());
    }

    @Test
    public void testIgnoreOtherData() {
        HueBridgeDetector hhda = new HueBridgeDetector();
        hhda.setPluginId("myplugin");
        MockDeviceBridgeDetectionContext context = new MockDeviceBridgeDetectionContext();
        DeviceBridgeMetaData entity = new DeviceBridgeMetaData("ssdp", "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=90\r\n" +
                "LOCATION: http://192.168.0.13:49153/nmsDescription.xml\r\n" +
                "NT: urn:schemas-upnp-org:service:ConnectionManager:1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "SERVER: Windows2000/0.0 UPnP/1.0 PhilipsIntelSDK/1.4 DLNADOC/1.50\r\n" +
                "X-User-Agent: redsonic\r\n" +
                "USN: uuid:5AFEF00D-BABE-DADA-FA5A-00113215F871::urn:schemas-upnp-org:service:ConnectionManager:1\r\n" +
                "CONTENT-LENGTH: 0\r\n");
        assertEquals(0, context.getIdentifiedEntities().size());
        assertFalse(hhda.identify(context, entity));
        assertEquals(0, context.getIdentifiedEntities().size());
    }

    @Test
    public void testNoDuplicates() {
        HueBridgeDetector hhda = new HueBridgeDetector();
        hhda.setPluginId("myplugin");
        MockDeviceBridgeDetectionContext context = new MockDeviceBridgeDetectionContext();
        DeviceBridgeMetaData entity = new DeviceBridgeMetaData("ssdp", "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: http://192.168.0.220:80/description.xml\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n");
        assertEquals(0, context.getIdentifiedEntities().size());
        assertTrue(hhda.identify(context, entity));
        assertEquals(1, context.getIdentifiedEntities().size());
        DeviceBridge e = context.getIdentifiedEntities().get(0);
        assertEquals("192.168.0.220", e.getValue());
        assertEquals("myplugin", e.getPluginId());
        assertEquals(HueBridgeDetector.ID, e.getType());
        context.clear();
        assertEquals(0, context.getIdentifiedEntities().size());
        assertTrue(hhda.identify(context, entity));
        assertEquals(0, context.getIdentifiedEntities().size());
    }

    private class MockDeviceBridgeDetectionContext implements DeviceBridgeDetectionContext {
        private List<DeviceBridge> identifiedEntityList = new ArrayList<DeviceBridge>();

        public List<DeviceBridge> getIdentifiedEntities() {
            return identifiedEntityList;
        }

        public void clear() {
            identifiedEntityList.clear();
        }

        @Override
        public void addDeviceBridge(DeviceBridge entity) {
            identifiedEntityList.add(entity);
        }

        @Override
        public void removeDeviceBridge(String entityId) {
            // TODO
        }
    }
}
