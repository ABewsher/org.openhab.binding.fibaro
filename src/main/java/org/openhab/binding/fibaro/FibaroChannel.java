/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fibaro;

/**
 * Enum class for the Fibaro channel id:s
 *
 * @author Johan Williams - Initial contribution
 *
 */
public enum FibaroChannel {

    ALARM("alarm"),
    BATTERY("battery"),
    BLINDS("blinds"),
    COLOR_LIGHT("color-light"),
    ELECTRIC_CURRENT("electric-current"),
    DEAD("dead"),
    DIMMER("dimmer"),
    DOOR("door"),
    ENERGY("energy"),
    HEAT("heat"),
    ILLUMINANCE("illuminance"),
    LAST_BREACHED("last-breached"),
    MOTION("motion"),
    POWER("power"),
    POWER_OUTLET("power-outlet"),
    SMOKE("smoke"),
    SWITCH("switch"),
    TEMPERATURE("temperature"),
    THERMOSTAT("thermostat"),
    VOLTAGE("voltage"),
    WINDOW("window");

    private final String id;

    private FibaroChannel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}