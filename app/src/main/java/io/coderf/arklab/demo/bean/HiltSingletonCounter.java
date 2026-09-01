package io.coderf.arklab.demo.bean;

/**
 * 演示 @Singleton：全局唯一实例，计数在多次注入间共享。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class HiltSingletonCounter {

    private int count;

    public int incrementAndGet() {
        return ++count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "HiltSingletonCounter{count=" + count + ", identity=" + System.identityHashCode(this) + '}';
    }
}
