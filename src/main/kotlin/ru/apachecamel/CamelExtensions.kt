package ru.apachecamel

import nl.topicus.overheid.kamel.input
import org.apache.camel.AggregationStrategy
import org.apache.camel.Exchange
import org.apache.camel.Expression
import org.apache.camel.Message
import org.apache.camel.Predicate
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.LoopDefinition
import org.apache.camel.model.MulticastDefinition
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.model.SplitDefinition
import org.apache.camel.model.TransactedDefinition
import org.apache.camel.model.TryDefinition
import kotlin.reflect.full.starProjectedType

inline fun <reified T> Message.body(): T = if (T::class.starProjectedType.isMarkedNullable) {
    getMandatoryBody(T::class.java)
} else {
    getBody(T::class.java)
}

inline fun <reified T> Exchange.body(): T = if (T::class.starProjectedType.isMarkedNullable) {
    this.input.getMandatoryBody(T::class.java)
} else {
    this.input.getBody(T::class.java)
}

inline fun <reified T> Exchange.property(name: String): T = this.getProperty(name, T::class.java)

inline fun <reified T> Exchange.header(name: String): T = this.getIn().getHeader(name, T::class.java)

val Exchange.headers: MutableMap<String, Any?>
    get() = this.`in`.headers

inline fun ProcessorDefinition<*>.transform(crossinline toApply: Exchange.() -> Any?): ProcessorDefinition<*> =
    transform(expression(toApply))

inline fun ProcessorDefinition<*>.body(crossinline toApply: Exchange.() -> Any?): ProcessorDefinition<*> =
    setBody(expression(toApply))

inline fun ProcessorDefinition<*>.header(
    name: String,
    crossinline toApply: Exchange.() -> Any?,
): ProcessorDefinition<*> = setHeader(name, expression(toApply))

inline fun ProcessorDefinition<*>.property(
    name: String,
    crossinline toApply: Exchange.() -> Any?,
): ProcessorDefinition<*> = setProperty(name, expression(toApply))

inline fun ProcessorDefinition<*>.doTry(crossinline toApply: TryDefinition.() -> Unit): ProcessorDefinition<*> =
    doTry().apply(toApply).end()

inline fun <T : Throwable> TryDefinition.doCatch(
    type: Class<T>,
    crossinline toApply: TryDefinition.() -> Unit,
): ProcessorDefinition<*> = endDoTry().doCatch(type).apply(toApply)

inline fun TryDefinition.doFinally(crossinline toApply: TryDefinition.() -> Unit): ProcessorDefinition<*> =
    endDoTry().doFinally().apply(toApply)

inline fun ProcessorDefinition<*>.process(crossinline toApply: Exchange.() -> Unit): ProcessorDefinition<*> =
    process { exchange -> toApply(exchange) }

fun ProcessorDefinition<*>.pipeline(toApply: ProcessorDefinition<*>.() -> Unit): ProcessorDefinition<*> {
    return pipeline()
        .apply(toApply)
        .end()
}

fun ProcessorDefinition<*>.transacted(ref: String, toApply: TransactedDefinition.() -> Unit): ProcessorDefinition<*> {
    return transacted(ref)
        .apply(toApply)
        .end()
}

fun ProcessorDefinition<*>.multicast(toApply: MulticastDefinition.() -> Unit): ProcessorDefinition<*> {
    return multicast()
        .apply(toApply)
        .end()
}

fun MulticastDefinition.configure(toApply: MulticastDefinition.() -> Unit): MulticastDefinition = apply(toApply)

fun ProcessorDefinition<*>.choice(toApply: ChoiceDefinition.() -> Unit): ProcessorDefinition<*> {
    return choice()
        .apply(toApply)
        .end()
}

fun ProcessorDefinition<*>.loopDoWhile(
    predicate: Predicate,
    toApply: LoopDefinition.() -> Unit,
): ProcessorDefinition<*> {
    return loopDoWhile(predicate)
        .apply(toApply)
        .end()
}

fun ProcessorDefinition<*>.split(
    value: Expression,
    aggStr: AggregationStrategy,
    toApply: SplitDefinition.() -> Unit,
): ProcessorDefinition<*> {
    return split(value, aggStr)
        .apply(toApply)
        .end()
}

inline fun expression(crossinline toApply: Exchange.() -> Any?): Expression = object : Expression {
    override fun <T : Any?> evaluate(exchange: Exchange?, type: Class<T>?): T? {
        @Suppress("UNCHECKED_CAST")
        return if (exchange == null) null else {
            val result = toApply(exchange)
            return if (result is Expression) result.evaluate(exchange, type) else result as? T
        }
    }
}

inline fun predicate(crossinline toApply: Exchange.() -> Any?): Predicate = Predicate { exchange ->
    val toApply1 = toApply(exchange)
    if (toApply1 is Predicate) toApply1.matches(exchange) else toApply1 as Boolean
}
