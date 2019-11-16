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

import android.util.Log
import java.util.concurrent.CountDownLatch

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

    override fun onError(error: Throwable) {
        error.printStackTrace()
    }

    fun subscribeTo(publisher: Publisher<T>): T {
        publisher.subscribe(this)

        if (isFinished.not()) {
            countDownLatch.await()
        }

        return latestValue ?: throw IllegalStateException("Publisher failed to publish any values")
    }
}

class UnhandledErrorException(cause: Throwable) : IllegalStateException(extract(cause)) {
    private companion object {
        fun extract(exception: Throwable): Throwable {
            var cause = exception

            while (cause is UnhandledErrorException) cause = cause.cause!!

            return cause
        }
    }
}

internal class EmptySubscriber<T> : Subscriber<T> {
    override fun onNext(element: T) {}
    override fun onComplete() {}
    override fun onCancel() {}
    override fun onError(error: Throwable) {
        if (TestMode.isEnabled) {
            throw UnhandledErrorException(error)
        } else {
            Log.e("Reaktive", "Unhandled error signal", error)
        }
    }
}

class TestSubscriber<T> internal constructor(publisher: Publisher<T>) {

    private val internalSubscription: Cancellable
    private var didComplete: Boolean = false
    private val emittedValues = mutableListOf<T>()

    val emittedErrors = mutableSetOf<Throwable>()

    init {
        internalSubscription = publisher
                .doOnComplete { this.didComplete = true }
                .doOnNext { this.emittedValues += it }
                .doOnError { this.emittedErrors += it }
                .subscribe()
    }

    fun cancel(): TestSubscriber<T> {
        internalSubscription.cancel()
        return this
    }

    fun assertComplete(): TestSubscriber<T> {
        if (didComplete) {
            return this
        }

        throw AssertionError("Expected subscription to have received an `onComplete` signal")
    }

    fun assertNotComplete(): TestSubscriber<T> {
        if (didComplete) {
            throw java.lang.AssertionError("Expected subscription to NOT have received an `onComplete` signal")
        }

        return this
    }

    fun assertEmittedValues(vararg elements: T): TestSubscriber<T> {
        if (elements.toList() != emittedValues) {
            throw AssertionError("Values are wrong, expected [${elements.toList()}] but was [$emittedValues]")
        }

        return this
    }

    fun assertNoValuesEmitted(): TestSubscriber<T> {
        if (emittedValues.isNotEmpty()) {
            throw java.lang.AssertionError("Expected no elements no have been emitted, but was [$emittedValues]")
        }

        return this
    }

    fun assertNoErrors(): TestSubscriber<T> {
        if (emittedErrors.isNotEmpty()) {
            throw AssertionError("Expected no errors, but there were ${emittedErrors.size} errors")
        }
        return this
    }

    inline fun <reified E : Throwable> assertError(): TestSubscriber<T> {
        when {
            emittedErrors.isEmpty() -> throw AssertionError("Expected [${E::class}] but no errors have been emitted")
            emittedErrors.size > 1 -> throw AssertionError("Expected single error [${E::class}] but multiple errors have been emitted: [$emittedErrors]")
            emittedErrors.first() !is E -> throw AssertionError("Expected single error [${E::class}] but only [${emittedErrors.first()}] has been emitted")
        }

        return this
    }
}
