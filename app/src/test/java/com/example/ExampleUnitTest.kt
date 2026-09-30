package com.example

import com.example.data.downloader.YtDlpService
import com.example.utils.FormatUtils
import com.example.utils.SampleData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun formatUtils_formatDuration_isCorrect() {
        assertEquals("00:45", FormatUtils.formatDuration(45))
        assertEquals("03:20", FormatUtils.formatDuration(200))
        assertEquals("01:05:00", FormatUtils.formatDuration(3900))
    }

    @Test
    fun formatUtils_formatBytes_isCorrect() {
        assertTrue(FormatUtils.formatBytes(1024 * 1024 * 15).contains("15"))
    }

    @Test
    fun sampleData_containsValidSamples() {
        assertTrue(SampleData.samples.isNotEmpty())
        val random = SampleData.getRandomSample()
        assertNotNull(random.url)
        assertTrue(random.funnyPhrase.isNotBlank())
    }
}
