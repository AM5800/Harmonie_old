package am5800.harmonie.model

import java.io.File

public fun File.nameWithoutExt(): String {
    return name.substring(0, name.lastIndexOf('.'))
}


