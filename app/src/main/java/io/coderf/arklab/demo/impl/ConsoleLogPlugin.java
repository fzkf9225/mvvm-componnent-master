package io.coderf.arklab.demo.impl;

import javax.inject.Inject;

import io.coderf.arklab.demo.inter.HiltLogPlugin;

public class ConsoleLogPlugin implements HiltLogPlugin {

    @Inject
    public ConsoleLogPlugin() {
    }

    @Override
    public String name() {
        return "ConsoleLog";
    }

    @Override
    public void log(String message) {
        System.out.println("[ConsoleLog] " + message);
    }
}
