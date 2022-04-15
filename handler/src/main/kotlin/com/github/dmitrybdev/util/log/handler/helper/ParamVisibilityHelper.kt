package com.github.dmitrybdev.util.log.handler.helper

import com.github.dmitrybdev.util.log.LogDebug
import com.github.dmitrybdev.util.log.LogDebugHide
import com.github.dmitrybdev.util.log.LogTrace
import com.github.dmitrybdev.util.log.LogTraceHide
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.kotlinFunction

object ParamVisibilityHelper {
    fun isReturnTypeVisible(isTrace: Boolean, method: Method) = isVisible(
        isTrace,
        method.kotlinFunction?.returnType?.annotations.orEmpty(),
        method.annotations.asList()
    )

    fun isVisible(
        isTrace: Boolean,
        param: Parameter,
        declaringMethod: Method,
    ) = isVisible(isTrace, param.annotations.asList(), declaringMethod.annotations.asList())

    fun isVisible(
        isTrace: Boolean,
        memberProperty: KProperty<*>,
        constructorParam: KParameter?,
        declaringType: KClass<*>,
    ) = isVisible(
        isTrace,
        memberProperty.annotations + constructorParam?.annotations.orEmpty(),
        declaringType.annotations
    )

    private fun isVisible(
        isTrace: Boolean,
        targetAnnotations: Collection<Annotation>,
        declaringTypeAnnotations: Collection<Annotation>,
    ) = when {
        isTrace && targetAnnotations.any { it is LogTraceHide } -> false
        isTrace && targetAnnotations.any { it is LogTrace } -> true

        isTrace && declaringTypeAnnotations.any { it is LogTraceHide } -> false
        isTrace && declaringTypeAnnotations.any { it is LogTrace } -> true

        targetAnnotations.any { it is LogDebugHide } -> false
        targetAnnotations.any { it is LogDebug } -> true

        declaringTypeAnnotations.any { it is LogDebugHide } -> false
        declaringTypeAnnotations.any { it is LogDebug } -> true

        else -> false
    }
}