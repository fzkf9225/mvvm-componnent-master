package pers.fz.mvvm.util.media

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts


/**
 * Created by fz on 2023/8/16 9:55
 * describe :
 */
class GetImagesMultipleContents {

    fun start(launcher: ActivityResultLauncher<PickVisualMediaRequest>) {
        var request: PickVisualMediaRequest = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        launcher.launch(request)
    }
}