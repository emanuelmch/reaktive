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

import java.util.concurrent.CountDownLatch

internal class BaseSubscriber<T>(private val onNextFunction: (T) -> Unit) : Subscriber<T> {

    override fun onNext(element: T) {
        onNextFunction(element)
    }

    override fun onComplete() {
    }

    override fun onCancel() {
    }
}

//FIXME: Make this thread-safe
internal class BlockingLastSubscriber<T> : Subscriber<T> {
    private var latestValue: T? = null
    private var isFinished = false
    private val countDownLatch = CountDownLatch(1)

    override fun onNext(element: T) {
        latestValue = element
    }

    override fun onComplete() {
        isFinished = true
        countDownLatch.countDown()
    }

    override fun onCancel() {
        isFinished = true
        countDownLatch.countDown()
    }

    fun subscribeTo(publisher: Publisher<T>): T {
        publisher.subscribe(this)

        if (isFinished.not()) {
            countDownLatch.await()
        }

        return latestValue ?: throw IllegalStateException("Publisher failed to publish any values")
    }
}

class TestSubscriber<T> internal constructor(publisher: Publisher<T>) {

    private val emittedValues = mutableListOf<T>()
    private val subscription: Subscription

    init {
        subscription = publisher.subscribe { this.emittedValues += it }
    }

    fun assertValuesOnly(vararg elements: T): TestSubscriber<T> {
        if (elements.toList() != emittedValues) {
            // FIXME: Should have a better error message
            throw AssertionError("Values are wrong, expected [$elements] but was [$emittedValues]")
        }

        return this
    }

    fun cancel(): TestSubscriber<T> {
        subscription.cancel()
        return this
    }

    // FIXME: Find out how to handle errors
    fun assertNoErrors(): TestSubscriber<T> {
        return this
    }
}
