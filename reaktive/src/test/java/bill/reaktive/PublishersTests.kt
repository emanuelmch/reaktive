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
import org.junit.Test

class PublishersTests {

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

class OpenHotPublisherTests {

    @Test
    fun `Allows one subscription`() {
        Publishers
                .open<Any>()
                .subscribe()
    }

    @Test(expected = AssertionError::class)
    fun `Doesn't allow two concurrent subscriptions`() {
        val publisher = Publishers.open<Any>()
        publisher.subscribe()
        publisher.subscribe()
    }

    @Test
    fun `Allows a second subscription after first one has been canceled`() {
        val publisher = Publishers.open<Any>()
        publisher.subscribe().cancel()
        publisher.subscribe()
    }

    @Test
    fun `Open Publishers should forward error signals to the subscriber`() {
        val publisher = Publishers.open<Any>()
        val subscriber = publisher.test()

        publisher.onError(UnsupportedOperationException("Testing"))
        subscriber.assertError<UnsupportedOperationException>()
    }
}
