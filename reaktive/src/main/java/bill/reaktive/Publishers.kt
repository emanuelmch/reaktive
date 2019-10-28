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

import bill.reaktive.publishers.SubscriberPublisher
import java.util.concurrent.TimeUnit

object Publishers {

    fun <T> elements(vararg elements: T): Publisher<T> {
        return SubscriberPublisher<T, T>(setup = { subscriber ->
            elements.forEach(subscriber::onNext)
            subscriber.onComplete()
        })
    }

    fun <T> open(): OpenPublisher<T, T> = SubscriberPublisher()

    fun <T> onSubscribe(setup: (Subscriber<T>) -> Unit): Publisher<T> = SubscriberPublisher(setup = setup)
}

internal abstract class BasePublisher<T> : Publisher<T> {
    override fun test() = TestSubscriber(this)

    override fun distinctUntilChanged() = DistinctUntilChangedProcessor(this)
    override fun filter(condition: (T) -> Boolean) = FilterProcessor(this, condition)
    override fun <V> map(function: (T) -> V) = MapperProcessor(this, function)
    override fun startWith(element: T) = StartWithProcessor(this, element)
    override fun branch(condition: (T) -> Boolean) = Pair(FilterProcessor(this, condition), FilterProcessor(this, condition, invertCondition = true))

    override fun signalOnBackground() = SignalOnThreadProcessor(this, BackgroundThreadWorker())
    override fun signalOnForeground() = SignalOnThreadProcessor(this, ForegroundThreadWorker())
    override fun blockingLast(): T = BlockingLastSubscriber<T>().subscribeTo(this)

    override fun doOnNext(action: (T) -> Unit) = DoOnNextProcessor(this, action)
    override fun doOnComplete(action: () -> Unit) = DoOnCompleteProcessor(this, action)
    override fun doOnCancel(action: () -> Unit) = DoOnCancelProcessor(this, action)
    override fun doOnFinish(action: () -> Unit) = DoOnFinishProcessor(this, action)
    override fun doOnError(action: (Throwable) -> Unit) = DoOnErrorProcessor(this, action)
}
