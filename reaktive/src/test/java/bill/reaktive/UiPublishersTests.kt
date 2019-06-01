/*
 * Copyright (c) 2019 Emanuel Machado da Silva <emanuel.mch@gmail.com>
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

import android.view.View
import bill.reaktive.test.MockView
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class UiPublishersTests {

    private lateinit var view: View

    @Before
    fun before() {
        view = MockView()
    }

    @Test
    fun `clicks should emit when clicked`() {
        val testSubscriber = UiPublishers.clicks(view).test()

        view.callOnClick()

        testSubscriber
                .assertEmittedValues(Unit)
                .assertNotComplete()
                .assertNoErrors()
    }

    @Test
    fun `clicks should not emit until clicked`() {
        UiPublishers.clicks(view)
                .test()
                .assertNoValuesEmitted()
                .assertNotComplete()
                .assertNoErrors()
    }

    @Test
    fun `clicks should clean up when unsubscribed to`() {
        UiPublishers.clicks(view).subscribe().cancel()

        assertThat(view.hasOnClickListeners(), `is`(false))
    }
}
