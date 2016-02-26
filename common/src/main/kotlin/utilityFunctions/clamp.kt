package utilityFunctions


fun Int.clamp(lowerBound: Int, upperBound: Int): Int {
  if (this > upperBound) return upperBound
  if (this < lowerBound) return lowerBound
  return this
}