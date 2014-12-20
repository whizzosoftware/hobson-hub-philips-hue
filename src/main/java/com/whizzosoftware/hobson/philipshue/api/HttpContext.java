/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import java.net.URI;
import java.util.Map;

/**
 * An interface used by HueBridge objects to send HTTP requests to the Hue hub.
 *
 * @author Dan Noguerol
 */
public interface HttpContext {
    public void sendHttpGetRequest(URI uri, Map<String,String> headers, Object context);
    public void sendHttpPostRequest(URI uri, Map<String,String> headers, byte[] data, Object context);
    public void sendHttpPutRequest(URI uri, Map<String,String> headers, byte[] data, Object context);
}
