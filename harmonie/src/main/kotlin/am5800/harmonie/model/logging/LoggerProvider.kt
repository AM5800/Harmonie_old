package am5800.harmonie.model.logging

import am5800.harmonie.model.Logger

public interface LoggerProvider {
    fun getLogger(name: String): Logger
    fun getLogger<_>(javaClass: Class<_>): Logger = getLogger(javaClass.name)
}