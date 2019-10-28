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

package bill.reaktive.test

import bill.reaktive.BackgroundThreadWorker
import bill.reaktive.Publisher
import bill.reaktive.publishers.SubscriberPublisher

fun <T> Publisher<T>.signalOnSleepingThread(vararg sleepTimers: Long): Publisher<T> =
        SignalOnSleepingThreadProcessor(this, sleepTimers)

internal class SignalOnSleepingThreadProcessor<T>(origin: Publisher<T>, sleepTimers: LongArray) : SubscriberPublisher<T, T>(origin) {

    private val sleepTimers = sleepTimers.iterator()
    private val threadWorker = BackgroundThreadWorker()

    override fun safeOnNext(element: T) {
        threadWorker.run {
            Thread.sleep(sleepTimers.next())
            super.safeOnNext(element)
        }
    }

    override fun onCancel() {
        threadWorker.run { super.onCancel() }
    }

    override fun onComplete() {
        threadWorker.run { super.onComplete() }
    }

    override fun onError(error: Throwable) {
        threadWorker.run { super.onError(error) }
    }
}
