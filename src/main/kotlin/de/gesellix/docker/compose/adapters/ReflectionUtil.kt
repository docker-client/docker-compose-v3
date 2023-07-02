package de.gesellix.docker.compose.adapters

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties

fun <R : Any?> readProperty(instance: Any, propertyName: String): R {
    val clazz = instance.javaClass.kotlin
    @Suppress("UNCHECKED_CAST")
    return clazz.declaredMemberProperties.first { it.name == propertyName }.get(instance) as R
}

fun writeProperty(instance: Any, propertyName: String, value: Any?) {
    val clazz = instance.javaClass.kotlin
    @Suppress("UNCHECKED_CAST")
    return clazz.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .first { it.name == propertyName }.setter.call(instance, value)
}
