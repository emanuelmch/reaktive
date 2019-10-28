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

package bill.reaktive.publishers

import bill.reaktive.*

internal open class SubscriberPublisher<T, V>(
    private val origin: Publisher<T>? = null,
    private val setup: ((Subscriber<T>) -> Unit)? = null,
    private val mapper: ((T) -> V)? = null
) : BasePublisher<V>(), OpenPublisher<T, V>, Subscriber<T> {

    private val subscribers = mutableSetOf<Subscriber<V>>()
    private var isTerminated = false

    open fun safeOnNext(element: V) = subscribers.forEach { it.onNext(element) }

    final override fun onNext(element: T) {
        check(isTerminated.not()) { "Can't emit onNext on terminated publishers" }

        try {
            safeOnNext(map(element))
        } catch (ex: Throwable) {
            onError(ex)
        }
    }

    override fun onComplete() {
        subscribers.forEach(Subscriber<V>::onComplete)
        onTerminalSignal()
    }

    override fun onCancel() {
        subscribers.forEach(Subscriber<V>::onCancel)
        onTerminalSignal()
    }

    override fun onError(error: Throwable) {
        subscribers.forEach { it.onError(error) }
        onTerminalSignal()
    }

    private fun onTerminalSignal() {
        subscribers.clear()
        isTerminated = true
    }

    @Suppress("UNCHECKED_CAST")
    private fun map(element: T) = mapper?.invoke(element) ?: element as V

    override fun subscribe(subscriber: Subscriber<V>): Cancellable {
        this.subscribers += subscriber
        setup?.invoke(this)

        return if (origin != null) {
            origin.subscribe(this)
        } else {
            object : Cancellable {
                override fun cancel() {
                    subscriber.onCancel()
                    subscribers -= subscriber
                }
            }
        }
    }
}
