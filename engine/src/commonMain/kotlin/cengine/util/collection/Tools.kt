package cengine.util.collection

inline fun <reified T : Any> Array<*>.firstInstance(validator: (element: T) -> Boolean = { true }): T? = filterIsInstance<T>().firstOrNull { validator(it) }
