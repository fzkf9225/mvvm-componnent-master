package pers.fz.media.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import pers.fz.media.bean.SelectorOptions;
import pers.fz.media.enums.MediaTypeEnum;

/**
  * created by fz on 2025/8/7 15:12 
  * describe: 
  */
public class OpenSingleSelector extends ActivityResultContract<SelectorOptions, Uri> {
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
                .setType("*/*");
    }

    @Nullable
    @Override
    public final ActivityResultContract.SynchronousResult<Uri> getSynchronousResult(
            @NonNull Context context,
            SelectorOptions input
    ) {
        return null;
    }

    @Nullable
    @Override
    public final Uri parseResult(int resultCode, @Nullable Intent intent) {
        return (resultCode == Activity.RESULT_OK && intent != null) ? intent.getData() : null;
    }
}


