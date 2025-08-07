package pers.fz.media.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import pers.fz.media.bean.SelectorOptions;
import pers.fz.media.enums.MediaTypeEnum;


/**
 * created by fz on 2025/8/7 9:13
 * describe:
 */
public class OpenMultiSelector extends ActivityResultContract<SelectorOptions, List<Uri>> {
    private SelectorOptions selectorOptions;

    public SelectorOptions getSelectorOptions() {
        return selectorOptions;
    }

    public MediaTypeEnum getMediaType() {
        return selectorOptions.getMediaTypeEnum();
    }

    @NonNull
    @CallSuper
    @Override
    public Intent createIntent(@NonNull Context context, SelectorOptions input) {
        this.selectorOptions = input;
        return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .putExtra(Intent.EXTRA_MIME_TYPES, input.getType())
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                .setType("*/*");
    }

    @Nullable
    @Override
    public final ActivityResultContract.SynchronousResult<List<Uri>> getSynchronousResult(
            @NonNull Context context,
            SelectorOptions input
    ) {
        return null;
    }

    @Override
    public final List<Uri> parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return getClipDataUris(intent);
        }
        return new ArrayList<>();
    }

    private List<Uri> getClipDataUris(Intent intent) {
        List<Uri> uris = new ArrayList<>();
        if (intent.getClipData() != null) {
            for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
                Uri uri = intent.getClipData().getItemAt(i).getUri();
                if (uri != null) {
                    uris.add(uri);
                }
            }
        } else if (intent.getData() != null) {
            uris.add(intent.getData());
        }
        return uris;
    }
}

