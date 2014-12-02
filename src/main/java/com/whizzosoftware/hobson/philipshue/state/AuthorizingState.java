/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.HueAuthenticationException;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.HueLinkButtonNotPressedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state representing the authorization process. This includes detecting if API calls can be made, creating a
 * Hobson user if one doesn't exist and checking if the Hue Bridge button needs to be pressed.
 *
 * @author Dan Noguerol
 */
public class AuthorizingState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public State onLoop(StateContext context) {
        State nextState = this;

        // do a simple call to see if the userName has been added to the controller
        try {
            context.getHueChannel().getAllLights();
            nextState = new RunningState();
        } catch (HueAuthenticationException hae) {
            try {
                // if there's an auth exception, attempt to create the user
                context.getHueChannel().createUser(context.getHueDeviceString(), context.getHueUserString());
            } catch (HueLinkButtonNotPressedException hlbnpe) {
                onHueLinkButtonNotPressed(context);
            } catch (HueException e) {
                logger.error("Error attempting to create Hue user", e);
                context.setPluginStatus(new PluginStatus(PluginStatus.Status.FAILED, "Unable to create Hobson user on Hue bridge"));
                nextState = new FailedState();
            }
        } catch (HueLinkButtonNotPressedException hlbnpe) {
            onHueLinkButtonNotPressed(context);
        } catch (HueException he) {
            logger.error("Error attempting to authorize with Hue bridge", he);
        }

        return nextState;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        return new InitializingState();
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set variable request while waiting for authorization; ignoring");
        return this;
    }

    protected void onHueLinkButtonNotPressed(StateContext context) {
        logger.warn("Plugin not authorized to talk to Hue bridge; waiting for user to press bridge button");
        context.setPluginStatus(new PluginStatus(PluginStatus.Status.FAILED, "The plugin is not authorized to talk to the Hue bridge. Please press the button on your bridge."));
    }
}
