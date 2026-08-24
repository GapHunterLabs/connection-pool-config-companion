package dev.gaphunter.connectionpoolconfigcompanion.detect

import dev.gaphunter.connectionpoolconfigcompanion.model.PoolHit
import dev.gaphunter.connectionpoolconfigcompanion.model.PoolProblem

/**
 * Plain-text line scanner for HikariCP connection-pool settings in
 * `.properties`/`.yml`/`.yaml` config files (the common Spring Boot
 * `spring.datasource.hikari.*` prefix, or a bare `hikari.*` prefix for
 * non-Spring setups) -- flags a value outside HikariCP's own
 * documented safe range (its README/wiki, not an invented number):
 * `connectionTimeout` &lt; 250ms, `maxLifetime` &lt; 30000ms,
 * `idleTimeout` &lt; 10000ms, or `idleTimeout` &gt;= `maxLifetime`
 * (HikariCP silently ignores `idleTimeout` in that last case, a real
 * documented gotcha).
 *
 * **Deliberately property-name-agnostic on casing** -- matches both
 * `connectionTimeout` (Java-property style) and `connection-timeout`
 * (Spring Boot's kebab-case relaxed binding), since either is valid in
 * a real `application.properties`/`application.yml`.
 *
 * **v0.1 scope, stated honestly:** only single-line `key=value`/
 * `key: value` pairs are scanned, plain-text (no real YAML parser) --
 * same discipline as `ConfigLineScanner`
 * (config-secrets-file-companion). `maxLifetime` vs `idleTimeout` is
 * only cross-checked when both appear in the *same file*.
 */
object HikariConfigScanner {

    private const val MIN_CONNECTION_TIMEOUT_MS = 250L
    private const val MIN_MAX_LIFETIME_MS = 30_000L
    private const val MIN_IDLE_TIMEOUT_MS = 10_000L

    private val PROPERTIES_OR_ENV_LINE = Regex("""^([A-Za-z0-9_.-]+)\s*[=:]\s*["']?(\d+)["']?\s*$""")

    private val KEY_ALIASES = mapOf(
        "connectionTimeout" to setOf("connectiontimeout", "connection-timeout"),
        "maxLifetime" to setOf("maxlifetime", "max-lifetime"),
        "idleTimeout" to setOf("idletimeout", "idle-timeout"),
    )

    fun scan(text: String): List<PoolHit> {
        var maxLifetimeValue: Long? = null
        var idleTimeoutHit: Pair<Long, Int>? = null
        val hits = mutableListOf<PoolHit>()

        text.lines().forEachIndexed { index, rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachIndexed
            val match = PROPERTIES_OR_ENV_LINE.find(trimmed) ?: return@forEachIndexed
            val fullKey = match.groupValues[1]
            val value = match.groupValues[2].toLongOrNull() ?: return@forEachIndexed
            val lastSegment = fullKey.substringAfterLast('.').lowercase()

            val canonical = KEY_ALIASES.entries.firstOrNull { (_, aliases) -> lastSegment in aliases }?.key ?: return@forEachIndexed
            if (!fullKey.contains("hikari", ignoreCase = true)) return@forEachIndexed

            when (canonical) {
                "connectionTimeout" -> if (value < MIN_CONNECTION_TIMEOUT_MS) {
                    hits += PoolHit(fullKey, value, PoolProblem.CONNECTION_TIMEOUT_TOO_LOW, index + 1)
                }
                "maxLifetime" -> {
                    maxLifetimeValue = value
                    if (value < MIN_MAX_LIFETIME_MS) {
                        hits += PoolHit(fullKey, value, PoolProblem.MAX_LIFETIME_TOO_LOW, index + 1)
                    }
                }
                "idleTimeout" -> {
                    idleTimeoutHit = value to (index + 1)
                    if (value < MIN_IDLE_TIMEOUT_MS) {
                        hits += PoolHit(fullKey, value, PoolProblem.IDLE_TIMEOUT_TOO_LOW, index + 1)
                    }
                }
            }
        }

        val maxLifetime = maxLifetimeValue
        val idleTimeout = idleTimeoutHit
        if (maxLifetime != null && idleTimeout != null && idleTimeout.first >= maxLifetime) {
            hits += PoolHit("idleTimeout", idleTimeout.first, PoolProblem.IDLE_TIMEOUT_NOT_LESS_THAN_MAX_LIFETIME, idleTimeout.second)
        }

        return hits
    }
}
