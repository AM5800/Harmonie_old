package am5800.common.utils.functions

fun String.splitAtLines(): Array<String> {
  return this.split("\\r?\\n".toRegex()).toTypedArray()
}




