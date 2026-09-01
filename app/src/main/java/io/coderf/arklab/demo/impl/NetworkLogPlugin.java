package io.coderf.arklab.demo.impl;

import javax.inject.Inject;

import io.coderf.arklab.demo.inter.HiltLogPlugin;

/**
 * NetworkLogPlugin 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class NetworkLogPlugin implements HiltLogPlugin {

    @Inject
    public NetworkLogPlugin() {
    }

    @Override
    public String name() {
        return "NetworkLog";
    }

    @Override
    public void log(String message) {
        System.out.println("[NetworkLog] upload -> " + message);
    }
}
