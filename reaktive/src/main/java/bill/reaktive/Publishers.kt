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

import java.util.concurrent.TimeUnit

object Publishers {

    fun <T> empty(): Publisher<T> = EmptyPublisher()

    fun <T> elements(vararg elements: T): Publisher<T> {
        return HotPublisher { streamer ->
            elements.forEach { streamer.onNext(it) }
            streamer.onComplete()
        }
    }

    fun <T> open(): OpenPublisher<T> = object : HotPublisher<T>(), OpenPublisher<T> {}

    fun <T> onSubscribe(setup: (Subscriber<T>) -> Unit): Publisher<T> = HotPublisher(setup)
}

internal abstract class BasePublisher<T> : Publisher<T> {
    override fun subscribe(onNext: (T) -> Unit) = subscribe(BaseSubscriber(onNext))
    override fun test() = TestSubscriber(this)

    override fun distinctUntilChanged() = DistinctUntilChangedProcessor(this)
    override fun <V> map(function: (T) -> V) = MapperProcessor(this, function)
    override fun startWith(element: T) = StartWithProcessor(this, element)

    override fun delay(delay: Long, unit: TimeUnit) = DelayProcessor(this, delay, unit)
    override fun signalOnBackground() = SignalOnThreadProcessor(this, BackgroundThreadWorker())
    override fun signalOnForeground() = SignalOnThreadProcessor(this, ForegroundThreadWorker())
    override fun blockingLast(): T = BlockingLastSubscriber<T>().subscribeTo(this)

    override fun doOnNext(action: (T) -> Unit) = DoOnNextProcessor(this, action)
    override fun doOnCancel(action: () -> Unit) = DoOnCancelProcessor(this, action)
    override fun doOnFinish(action: () -> Unit) = DoOnFinishProcessor(this, action)
}

//FIXME Create tests for EmptyPublisher
internal class EmptyPublisher<T> : BasePublisher<T>() {
    override fun subscribe(subscriber: Subscriber<T>) = EmptySubscription()
}

//FIXME Create tests for HotPublisher
internal open class HotPublisher<T>(private val setup: (Subscriber<T>) -> Unit = {}) : BasePublisher<T>(), Subscriber<T>, Subscription {

    private var subscriber: Subscriber<T> = BaseSubscriber { }

    override fun subscribe(subscriber: Subscriber<T>): Subscription {
        this.subscriber = subscriber
        setup(this)
        return this
    }

    override fun onNext(element: T) = subscriber.onNext(element)
    override fun onComplete() = subscriber.onComplete()
    override fun onCancel() = subscriber.onCancel()
    override fun cancel() = onCancel()
}
