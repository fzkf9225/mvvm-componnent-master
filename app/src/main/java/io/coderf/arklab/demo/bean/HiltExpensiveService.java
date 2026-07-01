package io.coderf.arklab.demo.bean;

/**
 * 演示 Lazy / Provider：每次 Provider.get() 或 Lazy.get() 首次访问时会创建新实例。
 */
public class HiltExpensiveService {

    private final long createdAt = System.currentTimeMillis();

    public HiltExpensiveService() {
        System.out.println("HiltExpensiveService 被创建，identity=" + System.identityHashCode(this));
    }

    public String describe() {
        return "HiltExpensiveService{identity=" + System.identityHashCode(this)
                + ", createdAt=" + createdAt + '}';
    }
}
