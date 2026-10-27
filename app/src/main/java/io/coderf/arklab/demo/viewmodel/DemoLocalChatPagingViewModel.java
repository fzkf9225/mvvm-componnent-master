package io.coderf.arklab.demo.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.api.RepositoryFactory;
import io.coderf.arklab.common.datasource.RxRoomPagingSource;
import io.coderf.arklab.common.viewmodel.PagingViewModel;
import io.coderf.arklab.core.bean.RoomPagingQuery;
import io.coderf.arklab.demo.bean.LocalChatMessage;
import io.coderf.arklab.demo.database.LocalChatDatabase;
import io.coderf.arklab.demo.repository.LocalChatPagingRepositoryImpl;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Room + Paging3 本地发消息 Demo：insert 后 {@link #refreshData()} 重建 PagingSource。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
@HiltViewModel
public class DemoLocalChatPagingViewModel
        extends PagingViewModel<LocalChatPagingRepositoryImpl, LocalChatMessage, RoomPagingQuery> {

    private static final int SEED_COUNT = 48;
    private final AtomicBoolean seedStarted = new AtomicBoolean(false);

    @Inject
    public DemoLocalChatPagingViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public LiveData<PagingData<LocalChatMessage>> createPagingData() {
        return PagingLiveData.getLiveData(new Pager<>(getPagingConfig(),
                () -> new RxRoomPagingSource<>(iRepository, getPagingQuery())));
    }

    @Override
    protected LocalChatPagingRepositoryImpl createRepository() {
        return RepositoryFactory.create(
                LocalChatPagingRepositoryImpl.class,
                LocalChatDatabase.getInstance(getApplication()).getLocalChatMessageDao());
    }

    @Override
    public int getStartPage() {
        return 0;
    }

    @NonNull
    @Override
    protected RoomPagingQuery createPagingQuery() {
        RoomPagingQuery q = new RoomPagingQuery();
        q.setOrderBy("createdAt");
        return q;
    }

    /**
     * 空库时写入一批历史消息，便于演示向上翻页加载更早记录。
     */
    public void ensureSeedHistory(@Nullable Action onReady, @Nullable Consumer<? super Throwable> onError) {
        if (!seedStarted.compareAndSet(false, true)) {
            if (onReady != null) {
                try {
                    onReady.run();
                } catch (Throwable ignored) {
                }
            }
            return;
        }
        Disposable ignored = getIRepository().countAll(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
                    if (count != null && count > 0) {
                        if (onReady != null) {
                            onReady.run();
                        }
                        return;
                    }
                    List<LocalChatMessage> seed = buildSeedMessages();
                    Disposable d = getIRepository().insert(seed, false)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                if (onReady != null) {
                                    onReady.run();
                                }
                            }, e -> {
                                seedStarted.set(false);
                                if (onError != null) {
                                    onError.accept(e);
                                }
                            });
                }, e -> {
                    seedStarted.set(false);
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

    /**
     * 本地发送一条消息（写入 Room）。
     */
    public void sendMessage(@NonNull String text,
                            @Nullable Action onSuccess,
                            @Nullable Consumer<? super Throwable> onError) {
        String content = text.trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        LocalChatMessage message = new LocalChatMessage(content, System.currentTimeMillis(), true);
        Disposable ignored = getIRepository().insert(message, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    refreshData();
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }, e -> {
                    if (onError != null) {
                        onError.accept(e);
                    }
                });
    }

    @NonNull
    private List<LocalChatMessage> buildSeedMessages() {
        long now = System.currentTimeMillis();
        List<LocalChatMessage> list = new ArrayList<>(SEED_COUNT);
        String[] peer = {
                "你好，这是本地 Room 预置的历史消息。",
                "向上滑动可以加载更早的分页记录。",
                "底部输入框发送会 insert 到 Room，再刷新 Paging。",
                "不涉及真实网络通信，仅演示单条插入。",
                "Paging3 本身不支持随意插 item，落库后 refresh 即可。"
        };
        String[] mine = {
                "收到，我先翻翻历史。",
                "明白，发送走本地库。",
                "这条也会按时间排到最底部。",
                "好的，继续。"
        };
        for (int i = 0; i < SEED_COUNT; i++) {
            // 越靠后越新；列表 orderBy createdAt DESC → index0 最新
            long createdAt = now - (SEED_COUNT - i) * 60_000L;
            boolean isMine = i % 3 == 2;
            String content = isMine
                    ? mine[(i / 3) % mine.length]
                    : peer[i % peer.length] + " (#" + (i + 1) + ")";
            list.add(new LocalChatMessage(content, createdAt, isMine));
        }
        return list;
    }
}
