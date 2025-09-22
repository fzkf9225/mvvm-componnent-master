package com.casic.otitan.media.helper;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.casic.otitan.media.enums.MediaTypeEnum;

/**
 * created by fz on 2025/8/7 15:16
 * describe:
 */
public class OpenPickMediaSelector extends ActivityResultContracts.PickVisualMedia {
    private PickVisualMediaRequest pickVisualMediaRequest;

    public PickVisualMediaRequest getPickVisualMediaRequest() {
        return pickVisualMediaRequest;
    }

    public ActivityResultContracts.PickVisualMedia.VisualMediaType getPickVisualMediaType() {
        return pickVisualMediaRequest.getMediaType();
    }

    public MediaTypeEnum getMediaType() {
        if (pickVisualMediaRequest.getMediaType() == ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE) {
            return MediaTypeEnum.IMAGE;
        } else if (pickVisualMediaRequest.getMediaType() == ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE) {
            return MediaTypeEnum.VIDEO;
        } else {
            return MediaTypeEnum.IMAGE_AND_VIDEO;
        }
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull PickVisualMediaRequest input) {
        this.pickVisualMediaRequest = input;
        return super.createIntent(context, input);
    }
}

