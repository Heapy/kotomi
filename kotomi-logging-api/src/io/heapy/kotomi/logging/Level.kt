package io.heapy.kotomi.logging

/**
 * Log severity levels, ordered by increasing severity.
 * [OFF] disables logging entirely.
 */
enum class Level {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF,
}
