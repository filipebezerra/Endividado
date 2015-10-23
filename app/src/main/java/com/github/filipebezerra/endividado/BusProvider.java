package com.github.filipebezerra.endividado;

import com.squareup.otto.Bus;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 23/10/2015
 * @since #
 */
public class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
