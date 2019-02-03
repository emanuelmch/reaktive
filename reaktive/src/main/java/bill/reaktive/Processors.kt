/*
 * Copyright (c) 2018 Emanuel Machado da Silva <emanuel.mch@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package bill.reaktive

object Processors {
    fun <T> cold(): Processor<T, T> = ColdPublisher()
}

internal open class BaseProcessor<T>(protected val origin: Publisher<T>) : BasePublisher<T>(), Processor<T, T> {

    private lateinit var subscriber: Subscriber<T>

    override fun subscribe(subscriber: Subscriber<T>): Subscription {
        this.subscriber = subscriber
        return origin.subscribe(this)
    }

    override fun onNext(element: T) = subscriber.onNext(element)
    override fun onComplete() = subscriber.onComplete()
    override fun onCancel() = subscriber.onCancel()
}

internal abstract class BaseMappingProcessor<T, V>(private val origin: Publisher<T>) : BasePublisher<V>(), Processor<T, V> {

    private lateinit var subscriber: Subscriber<V>
    protected abstract fun map(element: T): V

    override fun subscribe(subscriber: Subscriber<V>): Subscription {
        this.subscriber = subscriber
        return origin.subscribe(this)
    }

    override fun onNext(element: T) = subscriber.onNext(map(element))
    override fun onComplete() = subscriber.onComplete()
    override fun onCancel() = subscriber.onCancel()
}

internal class DistinctUntilChangedProcessor<T>(origin: Publisher<T>) : BaseProcessor<T>(origin) {

    override fun subscribe(subscriber: Subscriber<T>): Subscription {
        var lastEmission: T? = null
        return origin.subscribe {
            if (it != lastEmission) {
                lastEmission = it
                subscriber.onNext(it)
            }
        }
    }
}

internal class MapperProcessor<T, V>(origin: Publisher<T>, private val mapper: (T) -> V) : BaseMappingProcessor<T, V>(origin) {

    override fun map(element: T) = mapper(element)
}

internal class StartWithProcessor<T>(origin: Publisher<T>, private val initialElement: T) : BaseProcessor<T>(origin) {

    override fun subscribe(subscriber: Subscriber<T>): Subscription {
        subscriber.onNext(initialElement)
        return origin.subscribe(subscriber)
    }
}

internal class DoOnNextProcessor<T>(origin: Publisher<T>, private val action: (T) -> Unit) : BaseProcessor<T>(origin) {

    override fun onNext(element: T) {
        action(element)
        super.onNext(element)
    }
}

internal class DoOnCancelProcessor<T>(origin: Publisher<T>, private val action: () -> Unit) : BaseProcessor<T>(origin) {

    override fun onCancel() {
        action()
        super.onCancel()
    }
}

internal class DoOnFinishProcessor<T>(origin: Publisher<T>, private val action: () -> Unit) : BaseProcessor<T>(origin) {

    override fun onComplete() {
        action()
        super.onComplete()
    }

    override fun onCancel() {
        action()
        super.onCancel()
    }
}
