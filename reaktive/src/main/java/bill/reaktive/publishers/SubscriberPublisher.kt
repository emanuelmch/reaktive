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

import bill.reaktive.BasePublisher
import bill.reaktive.Subscriber

internal abstract class SubscriberPublisher<T> : BasePublisher<T>(), Subscriber<T> {
    protected var subscriber: Subscriber<T>? = null

    final override fun onNext(element: T) {
        try {
            safeOnNext(element)
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    open fun safeOnNext(element: T) = subscriber?.onNext(element) ?: Unit
    override fun onComplete() = subscriber?.onComplete() ?: Unit
    override fun onCancel() = subscriber?.onCancel() ?: Unit
    override fun onError(error: Throwable) = subscriber?.onError(error) ?: Unit
}

internal abstract class MappingSubscriberPublisher<T, V> : BasePublisher<V>(), Subscriber<T> {
    protected var subscriber: Subscriber<V>? = null

    protected abstract fun map(element: T): V

    final override fun onNext(element: T) {
        try {
            safeOnNext(element)
        } catch (ex: Exception) {
            onError(ex)
        }
    }

    open fun safeOnNext(element: T) = subscriber?.onNext(map(element)) ?: Unit
    override fun onComplete() = subscriber?.onComplete() ?: Unit
    override fun onCancel() = subscriber?.onCancel() ?: Unit
    override fun onError(error: Throwable) = subscriber?.onError(error) ?: Unit
}
