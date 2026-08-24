package dev.gaphunter.connectionpoolconfigcompanion.detect

import dev.gaphunter.connectionpoolconfigcompanion.model.PoolProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HikariConfigScannerTest {

    @Test
    fun `connectionTimeout below 250ms is flagged`() {
        val text = "spring.datasource.hikari.connection-timeout=100"
        val hits = HikariConfigScanner.scan(text)
        assertEquals(1, hits.size)
        assertEquals(PoolProblem.CONNECTION_TIMEOUT_TOO_LOW, hits[0].problem)
    }

    @Test
    fun `connectionTimeout at the documented minimum is not flagged`() {
        val text = "spring.datasource.hikari.connectionTimeout=250"
        assertTrue(HikariConfigScanner.scan(text).isEmpty())
    }

    @Test
    fun `maxLifetime below 30000ms is flagged`() {
        val text = "spring.datasource.hikari.max-lifetime=5000"
        val hits = HikariConfigScanner.scan(text)
        assertEquals(1, hits.size)
        assertEquals(PoolProblem.MAX_LIFETIME_TOO_LOW, hits[0].problem)
    }

    @Test
    fun `idleTimeout below 10000ms is flagged`() {
        val text = "hikari.idleTimeout=5000"
        val hits = HikariConfigScanner.scan(text)
        assertEquals(1, hits.size)
        assertEquals(PoolProblem.IDLE_TIMEOUT_TOO_LOW, hits[0].problem)
    }

    @Test
    fun `idleTimeout equal to maxLifetime is flagged`() {
        val text = """
            spring.datasource.hikari.max-lifetime=1800000
            spring.datasource.hikari.idle-timeout=1800000
        """.trimIndent()
        val hits = HikariConfigScanner.scan(text)
        assertTrue(hits.any { it.problem == PoolProblem.IDLE_TIMEOUT_NOT_LESS_THAN_MAX_LIFETIME })
    }

    @Test
    fun `healthy config is never flagged`() {
        val text = """
            spring.datasource.hikari.connection-timeout=30000
            spring.datasource.hikari.max-lifetime=1800000
            spring.datasource.hikari.idle-timeout=600000
        """.trimIndent()
        assertTrue(HikariConfigScanner.scan(text).isEmpty())
    }

    @Test
    fun `keys without hikari in the path are ignored`() {
        val text = "connectionTimeout=100"
        assertTrue(HikariConfigScanner.scan(text).isEmpty())
    }
}
