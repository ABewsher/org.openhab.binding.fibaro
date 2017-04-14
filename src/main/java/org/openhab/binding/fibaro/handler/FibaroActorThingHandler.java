/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fibaro.handler;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.fibaro.FibaroChannel;
import org.openhab.binding.fibaro.config.FibaroThingConfiguration;
import org.openhab.binding.fibaro.internal.exception.FibaroConfigurationException;
import org.openhab.binding.fibaro.internal.model.FibaroAction;
import org.openhab.binding.fibaro.internal.model.PropertyName;
import org.openhab.binding.fibaro.internal.model.json.FibaroApiResponse;
import org.openhab.binding.fibaro.internal.model.json.FibaroArguments;
import org.openhab.binding.fibaro.internal.model.json.FibaroUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FibaroActorThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Johan Williams - Initial contribution
 */
public class FibaroActorThingHandler extends FibaroAbstractThingHandler {

    private Logger logger = LoggerFactory.getLogger(FibaroActorThingHandler.class);

    public FibaroActorThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        try {
            init();
            updateStatus(ThingStatus.ONLINE);
        } catch (FibaroConfigurationException e) {
            // TODO Auto-generated catch block
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void init() throws FibaroConfigurationException {
        super.init();

        int id = getThingId();
        logger.debug("Initializing the binary switch handler with id {}", id);

        if (id < 1) {
            throw new FibaroConfigurationException(FibaroThingConfiguration.ID + "' must be larget than 0");
        }

        try {
            bridge.getDeviceData(id);
        } catch (Exception e) {
            throw new FibaroConfigurationException(
                    "Could not get device data from the Fibaro api for id " + id + ". Does this id exist?", e);
        }

        setThingId(id);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            String url = "http://" + bridge.getIpAddress() + "/api/devices/" + getId() + "/action/";
            if (command instanceof RefreshType) {
                updateChannel(channelUID.getId(), bridge.getDeviceData(getThingId()));
            } else if (command instanceof OnOffType) {
                url += command.equals(OnOffType.ON) ? FibaroAction.TURN_ON.getAction()
                        : FibaroAction.TURN_OFF.getAction();
                FibaroApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST, url, "", FibaroApiResponse.class);
                logger.debug(apiResponse.toString());
                // TODO: Check FibaroApiResponse for error codes
            } else if (command instanceof IncreaseDecreaseType) {
                url += command.equals(IncreaseDecreaseType.INCREASE) ? FibaroAction.LEVEL_INCREASE.getAction()
                        : FibaroAction.LEVEL_DECREASE.getAction();
                FibaroApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST, url, "", FibaroApiResponse.class);
                logger.debug(apiResponse.toString());
                // TODO: Check FibaroApiResponse for error codes
            } else if (command instanceof PercentType) {
                url += FibaroAction.SET_VALUE.getAction();
                int percentValue = ((PercentType) command).intValue();
                FibaroArguments arguments = new FibaroArguments();
                arguments.addArgs(percentValue);
                String content = gson.toJson(arguments);
                FibaroApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST, url, content,
                        FibaroApiResponse.class);
                logger.debug(apiResponse.toString());
                // TODO: Check FibaroApiResponse for error codes
            } else if (command instanceof DecimalType) {
                url += FibaroAction.SET_VALUE.getAction();
                double decimalValue = ((DecimalType) command).doubleValue();
                FibaroArguments arguments = new FibaroArguments();
                arguments.addArgs(decimalValue);
                String content = gson.toJson(arguments);
                FibaroApiResponse apiResponse = bridge.callFibaroApi(HttpMethod.POST, url, content,
                        FibaroApiResponse.class);
                logger.debug(apiResponse.toString());
                // TODO: Check FibaroApiResponse for error codes
            } else {
                logger.debug("Can't handle command: " + command.toString());
            }
        } catch (Exception e) {
            logger.debug("Failed to handle command " + command.toString() + " : " + e.getMessage());
        }
    }

    @Override
    public void update(FibaroUpdate fibaroUpdate) {
        PropertyName property = PropertyName.fromName(fibaroUpdate.getProperty());
        switch (property) {
            case BATTERY:
                updateChannel(FibaroChannel.BATTERY, stringToDecimal(fibaroUpdate.getValue()));
            case DEAD:
                updateChannel(FibaroChannel.DEAD, stringToOnOff(fibaroUpdate.getValue()));
                break;
            case ENERGY:
                updateChannel(FibaroChannel.ENERGY, stringToDecimal(fibaroUpdate.getValue()));
                break;
            case POWER:
                updateChannel(FibaroChannel.POWER, stringToDecimal(fibaroUpdate.getValue()));
                break;
            case VALUE:
                updateChannel(FibaroChannel.ALARM, stringToOnOff(fibaroUpdate.getValue()));
                updateChannel(FibaroChannel.DIMMER, stringToPercent(fibaroUpdate.getValue()));
                updateChannel(FibaroChannel.POWER_OUTLET, stringToOnOff(fibaroUpdate.getValue()));
                updateChannel(FibaroChannel.SWITCH, stringToOnOff(fibaroUpdate.getValue()));
                updateChannel(FibaroChannel.THERMOSTAT, stringToDecimal(fibaroUpdate.getValue()));
                break;
            default:
                logger.debug("Update received for an unknown property: {}", fibaroUpdate.getProperty());
                break;
        }

        // Remove this device from the cache as it has been updated
        bridge.removeFromCache(fibaroUpdate.getId());
    }

    /**
     * Returns the configured id
     *
     * @return Thing id
     */
    public int getId() {
        return getConfigAs(FibaroThingConfiguration.class).id;
    }

}
