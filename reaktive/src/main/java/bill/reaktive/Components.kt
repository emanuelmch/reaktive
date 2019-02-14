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

interface Publisher<T> {
    // Subscribing
    fun subscribe(subscriber: Subscriber<T> = EmptySubscriber()): Subscription
    fun test(): TestSubscriber<T>

    // Processors
    fun distinctUntilChanged(): Publisher<T>

    fun filter(function: (T) -> Boolean): Publisher<T>
    fun <V> map(function: (T) -> V): Publisher<V>
    fun startWith(element: T): Publisher<T>

    // Threading Processors
    fun delay(delay: Long, unit: TimeUnit): Publisher<T>

    fun signalOnBackground(): Publisher<T>
    fun signalOnForeground(): Publisher<T>
    fun blockingLast(): T

    // Events
    fun doOnNext(action: (T) -> Unit): Publisher<T>

    fun doOnCancel(action: () -> Unit): Publisher<T>
    fun doOnFinish(action: () -> Unit): Publisher<T>
    fun doOnError(action: (Throwable) -> Unit): Publisher<T>
}

interface Subscriber<T> {
    fun onNext(element: T)
    fun onComplete()
    fun onCancel()
    fun onError(error: Throwable)
}

interface Subscription {
    fun onCancel()
}

interface OpenPublisher<T, V> : Subscriber<T>, Publisher<V>
