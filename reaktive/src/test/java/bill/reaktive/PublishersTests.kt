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

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class PublishersTests {

    @Test
    fun `Cancelling subscribers should not affect other subscribers of the same Publisher`() {
        val open = Publishers.open<Int>()

        val subscriber1 = open.subscribe()
        val subscriber2 = open.test()

        subscriber1.cancel()

        open.onNext(1)
        open.onNext(2)
        open.onComplete()

        subscriber2
                .assertEmittedValues(1, 2)
                .assertComplete()
    }

    @Test
    fun `Cancelled subscribers should not receive any more signals`() {
        val open = Publishers.open<Unit>()

        val subscriber1 = open.test()
        open.subscribe()

        subscriber1.cancel()

        open.onNext(Unit)
        open.onComplete()

        subscriber1
                .assertNoValuesEmitted()
                .assertNotComplete()
    }

    @Test
    fun `Mid-chain operators should not keep their subscribers`() {
        val open = Publishers.open<Unit>()
        val origin = open.doOnNext { }

        val test = origin.doOnNext { fail() }.test()
        test.cancel()

        val subscription = origin.subscribe()
        open.onNext(Unit)

        subscription.cancel()

        test.assertNoErrors()
    }
}

class OnSubscribeTests {

    @Test
    fun `onSubscribe creates a Publisher you can subscribe to`() {
        Publishers
                .onSubscribe<Unit> { }
                .subscribe()
    }

    @Test
    fun `onSubscribe creates a Publisher that runs the setup function when you subscribe to it`() {
        var hasBeenCalled = false
        Publishers
                .onSubscribe<Unit> { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(true))
    }
}

class OpenPublisherTests {

    @Test
    fun `Open Publishers should forward error signals to the subscriber`() {
        val publisher = Publishers.open<Any>()
        val subscriber = publisher.test()

        publisher.onError(UnsupportedOperationException("Testing"))
        subscriber.assertError<UnsupportedOperationException>()
    }

    @Test(expected = IllegalStateException::class)
    fun `Open Publishers should fail when signaling an onNext after it's already completed`() {
        val publisher = Publishers.open<Any>()
        publisher.onComplete()
        publisher.onNext(Unit)
    }
}
