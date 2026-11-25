package io.coderf.arklab.demo.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import io.coderf.arklab.common.base.BasePagingAdapter
import io.coderf.arklab.common.base.BaseViewHolder
import io.coderf.arklab.core.utils.FileUtil
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ItemSpeakDemoBinding
import io.coderf.arklab.speak.db.VoiceMessage
import io.coderf.arklab.speak.widget.SpeakerView

/**
 * 语音消息 Paging Adapter（BasePagingAdapter）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 14:05
 */
class PagingSpeakAdapter : BasePagingAdapter<VoiceMessage, ItemSpeakDemoBinding>(DIFF) {

    @SuppressLint("SetTextI18n")
    override fun onBindHolder(
        holder: BaseViewHolder<ItemSpeakDemoBinding>,
        item: VoiceMessage?,
        pos: Int
    ) {
        if (item == null) return
        val binding = holder.binding
        binding.item = item
        val sec = (item.durationMs / 1000L).coerceAtLeast(1L)
        binding.speakerView.setType(SpeakerView.TYPE_MINE)
        binding.speakerView.setText("${sec}″")
        binding.tvMeta.text = "${item.filePath}\n${FileUtil.formatFileSize(item.sizeBytes)}"
        // 单击播放 / 长按删除由 Activity 的 PagingAdapterListener 处理
        binding.executePendingBindings()
    }

    override fun getLayoutId(): Int = R.layout.item_speak_demo

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VoiceMessage>() {
            override fun areItemsTheSame(oldItem: VoiceMessage, newItem: VoiceMessage): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VoiceMessage, newItem: VoiceMessage): Boolean {
                return oldItem.filePath == newItem.filePath
                        && oldItem.durationMs == newItem.durationMs
                        && oldItem.sizeBytes == newItem.sizeBytes
                        && oldItem.createdAt == newItem.createdAt
            }
        }
    }
}
