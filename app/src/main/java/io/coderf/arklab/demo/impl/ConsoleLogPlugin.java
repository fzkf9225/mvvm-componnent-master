package io.coderf.arklab.demo.impl;

import javax.inject.Inject;

import io.coderf.arklab.demo.inter.HiltLogPlugin;

/**
 * ConsoleLogPlugin 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
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
