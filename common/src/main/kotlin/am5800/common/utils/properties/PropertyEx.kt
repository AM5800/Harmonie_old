package am5800.common.utils.properties

import am5800.common.utils.Lifetime

fun <TSrc : Any, TDst : Any> Property<TSrc>.convert(srcDst: (TSrc) -> TDst, dstSrc: (TDst) -> TSrc): Property<TDst> {
  val result = Property(this.lifetime, srcDst(this.value))
  onChange(this.lifetime, { result.value = srcDst(it.newValue) })
  result.onChange(this.lifetime, { value = dstSrc(it.newValue) })
  return result
}

fun <TSrc : Any, TDst : Any> ReadonlyProperty<TSrc>.onChange(lifetime: Lifetime, dst: Property<TDst>, converter: (TSrc) -> TDst) {
  this.onChange(lifetime, {
    dst.value = converter(it.newValue)
  })
}

fun <T : Any> NullableReadonlyProperty<T>.onChangeNotNull(lifetime: Lifetime, handler: (T) -> Unit) {
  this.onChange(lifetime, {
    val value = it.newValue
    if (value != null) handler(value)
  })
}


