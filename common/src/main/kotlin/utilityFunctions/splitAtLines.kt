package utilityFunctions

fun String.splitAtLines(): Array<String> {
  return this.split("\\r?\\n".toRegex()).toTypedArray()
}




