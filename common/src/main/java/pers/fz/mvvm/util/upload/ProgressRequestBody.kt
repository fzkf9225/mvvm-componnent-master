package pers.fz.mvvm.util.upload


import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer
/**
 * created by fz on 2024/11/12 10:12
 * describe:
 */
class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val uri: Uri,
    private val progressListener: (Uri, Int) -> Unit
) : RequestBody() {
    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = countingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(
        sink: Sink
    ) : ForwardingSink(sink) {

        private var bytesWritten = 0L
        private var contentLength = requestBody.contentLength()

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            val progress = (100 * bytesWritten / contentLength).toInt()
            progressListener(uri, progress)
        }
    }
}
