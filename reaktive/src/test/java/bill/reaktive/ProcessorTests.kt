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
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ProcessorTests {

    @Test
    fun `distinctUntilChanged doesn't emit repeating emissions`() {
        Publishers.elements(1, 1, 2)
                .distinctUntilChanged()
                .test()
                .assertNoErrors()
                .assertEmittedValues(1, 2)
    }

    @Test
    fun `map emits the value created by the mapping function`() {
        Publishers.elements(1)
                .map { it * 2 }
                .test()
                .assertNoErrors()
                .assertEmittedValues(2)
    }

    @Test
    fun `startWith emits the initial value first`() {
        Publishers.elements(1)
                .startWith(0)
                .test()
                .assertNoErrors()
                .assertEmittedValues(0, 1)
    }

    @Test
    fun `doOnNext runs its function whenever something is published`() {
        var hasBeenCalled = false
        Publishers.elements(Unit)
                .doOnNext { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    fun `doOnNext doesn't runs its function when nothing is published`() {
        var hasBeenCalled = false
        Publishers.elements<Unit>()
                .doOnNext { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(false))
    }

    @Test
    fun `doOnComplete runs its function when the Publisher completes`() {
        val open = Publishers.open<Unit>()
        val subscription = open.test()

        open.onComplete()
        subscription.assertComplete()
    }

    @Test
    fun `doOnComplete doesn't run its function when the Publisher doesn't complete`() {
        val open = Publishers.open<Unit>()
        open.test()
                .assertNotComplete()
    }

    @Test
    fun `doOnFinish runs its function when the Publisher succeeds`() {
        var hasBeenCalled = false
        Publishers.elements(Unit)
                .doOnFinish { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    fun `doOnFinish runs its function when the Publisher is cancelled`() {
        var hasBeenCalled = false
        Publishers.onSubscribe<Unit> { }
                .doOnFinish { hasBeenCalled = true }
                .subscribe()
                .cancel()

        assertThat(hasBeenCalled, `is`(true))
    }

    @Test
    fun `doOnFinish doesn't run its function when the Publisher is left open`() {
        var hasBeenCalled = false
        Publishers.onSubscribe<Unit> { }
                .doOnFinish { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(false))
    }

    @Test
    fun `doOnCancel runs its functions when the Publisher is cancelled`() {
        var hasBeenCalled = false
        Publishers.onSubscribe<Unit> { }
                .doOnCancel { hasBeenCalled = true }
                .map { Unit }
                .subscribe()
                .cancel()

        assertThat(hasBeenCalled, `is`(true))
    }


    @Test
    fun `doOnCancel doesn't run its function when the Publisher is left open`() {
        var hasBeenCalled = false
        Publishers.onSubscribe<Unit> { }
                .doOnCancel { hasBeenCalled = true }
                .subscribe()

        assertThat(hasBeenCalled, `is`(false))
    }

    @Test
    fun `filter should filter out non-matching signals`() {
        val results = mutableListOf<Int>()

        Publishers.elements(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .filter { it.rem(3) == 0 }
                .doOnNext { results += it }
                .subscribe()

        assertThat(results, `is`(equalTo(listOf(3, 6, 9))))
    }

    @Test
    fun `branch should create two separate publishers`() {
        val (smaller, bigger) = Publishers.elements(1, 2, 3, 4)
                .branch { it < 3 }

        smaller.test()
                .assertNoErrors()
                .assertEmittedValues(1, 2)
                .assertComplete()

        bigger.test()
                .assertNoErrors()
                .assertEmittedValues(3, 4)
                .assertComplete()
    }

    @Test
    fun `branch should accept simultaneous subscriptions`() {
        val open = Publishers.open<Int>()
        val (smaller, bigger) = open.branch { it < 3 }
        val smallSubscription = smaller.test()
        val bigSubscription = bigger.test()

        open.onNext(1)
        open.onNext(2)
        open.onNext(3)
        open.onNext(4)
        open.onComplete()

        smallSubscription
                .assertNoErrors()
                .assertEmittedValues(1, 2)
                .assertComplete()

        bigSubscription
                .assertNoErrors()
                .assertEmittedValues(3, 4)
                .assertComplete()
    }
}
