package io.coderf.arklab.demo.inter;

/**
 * 演示 @IntoSet 多绑定：同一接口的多个实现会被收集到 Set 中注入。
 */
public interface HiltLogPlugin {

    String name();

    void log(String message);
}
