package io.coderf.arklab.templates.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileHeader {
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy/M/d")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun java(now: LocalDateTime = LocalDateTime.now()): String {
        val date = now.format(dateFmt)
        val time = now.format(timeFmt)
        return """
/**
 *
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created $date $time
 */
""".trimIndent()
    }

    fun kotlin(now: LocalDateTime = LocalDateTime.now()): String = java(now)
}
