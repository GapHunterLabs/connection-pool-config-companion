package dev.gaphunter.connectionpoolconfigcompanion.model

enum class PoolProblem {
    /** connectionTimeout below HikariCP's documented minimum (250ms). */
    CONNECTION_TIMEOUT_TOO_LOW,

    /** maxLifetime below HikariCP's documented minimum (30000ms / 30s). */
    MAX_LIFETIME_TOO_LOW,

    /** idleTimeout below HikariCP's documented minimum (10000ms / 10s). */
    IDLE_TIMEOUT_TOO_LOW,

    /** idleTimeout set >= maxLifetime -- HikariCP silently ignores idleTimeout in this case. */
    IDLE_TIMEOUT_NOT_LESS_THAN_MAX_LIFETIME,
}

/** One HikariCP config key with a value outside its documented safe range. */
data class PoolHit(val key: String, val value: Long, val problem: PoolProblem, val lineNumber: Int)
