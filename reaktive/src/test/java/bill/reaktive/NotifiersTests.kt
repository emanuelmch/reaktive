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

import bill.reaktive.test.MockNotifier
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class NotifiersTests {

    @Test
    fun `onSubscribe creates a Notifier that runs the setup function when you subscribe to it`() {
        var hasBeenCalled = false
        Notifiers
            .onSubscribe { hasBeenCalled = true }
            .subscribe()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    @Ignore("Feature not yet needed")
    fun `doOnFinish runs its function when the Publisher completes`() {
        Assert.fail("Test not implemented yet")
    }

    @Test
    fun `doOnFinish runs its function when the Publisher is cancelled`() {
        var hasBeenCalled = false
        Notifiers.onSubscribe { }
            .doOnFinish { hasBeenCalled = true }
            .subscribe()
            .cancel()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    fun `doOnFinish doesn't run its function when the Publisher is left open`() {
        var hasBeenCalled = false
        Notifiers.onSubscribe { }
            .doOnFinish { hasBeenCalled = true }
            .subscribe()

        assertThat(hasBeenCalled, `is`(false))
    }
}

@Suppress("ClassName")
class `Notifiers - toPublisher Tests` {

    @Test
    fun `toPublisher doesn't subscribes to the source when not subscribed to`() {
        var hasBeenCalled = false
        Notifiers.onSubscribe { hasBeenCalled = true }
            .toPublisher { }

        assertThat(hasBeenCalled, `is`(false))
    }

    @Test
    fun `toPublisher subscribes to the source when subscribed to`() {
        var hasBeenCalled = false
        Notifiers.onSubscribe { hasBeenCalled = true }
            .toPublisher { }
            .subscribe()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    fun `toPublisher cancels the source when cancelled`() {
        val notifier = MockNotifier()
        notifier.toPublisher {}
            .subscribe()
            .cancel()

        assertThat(notifier.cancelled, `is`(true))
    }

    @Test
    @Ignore("Feature not yet needed")
    fun `toPublisher is completed when the source is`() {
        Assert.fail("Test not implemented yet")
    }

    @Test
    fun `toPublisher emits the value created by the factory function`() {
        Notifiers
            .onSubscribe {
                it.onNext()
                it.onNext()
            }
            .toPublisher { 1 }
            .test()
            .assertValuesOnly(1, 1)
    }
}
