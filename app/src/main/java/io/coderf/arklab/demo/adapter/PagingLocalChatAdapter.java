package io.coderf.arklab.demo.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.LocalChatMessage;
import io.coderf.arklab.demo.databinding.ChatMessageItemBinding;

/**
 * 本地聊天气泡 Adapter（Room + Paging）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
public class PagingLocalChatAdapter extends BasePagingAdapter<LocalChatMessage, ChatMessageItemBinding> {

    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public PagingLocalChatAdapter() {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull LocalChatMessage oldItem,
                                           @NonNull LocalChatMessage newItem) {
                return Objects.equals(oldItem.getId(), newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull LocalChatMessage oldItem,
                                              @NonNull LocalChatMessage newItem) {
                return oldItem.getCreatedAt() == newItem.getCreatedAt()
                        && oldItem.isMine() == newItem.isMine()
                        && Objects.equals(oldItem.getContent(), newItem.getContent());
            }
        });
    }

    @Override
    public void onBindHolder(BaseViewHolder<ChatMessageItemBinding> holder,
                             LocalChatMessage item,
                             int pos) {
        ChatMessageItemBinding binding = holder.getBinding();
        String time = timeFormat.format(new Date(item.getCreatedAt()));
        if (item.isMine()) {
            binding.mineContainer.setVisibility(View.VISIBLE);
            binding.peerContainer.setVisibility(View.GONE);
            binding.tvMineContent.setText(item.getContent());
            binding.tvMineTime.setText(time);
        } else {
            binding.peerContainer.setVisibility(View.VISIBLE);
            binding.mineContainer.setVisibility(View.GONE);
            binding.tvPeerContent.setText(item.getContent());
            binding.tvPeerTime.setText(time);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chat_message_item;
    }
}
