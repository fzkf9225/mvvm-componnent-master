package io.coderf.arklab.demo.impl;

import javax.inject.Inject;

import io.coderf.arklab.demo.inter.HiltLogPlugin;

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
