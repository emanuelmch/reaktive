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

import android.os.Handler
import android.os.Looper
import bill.reaktive.publishers.SubscriberPublisher
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

internal class SignalOnThreadProcessor<T>(origin: Publisher<T>, private val threadWorker: ThreadWorker) : SubscriberPublisher<T, T>(origin) {
    private val semaphore = Semaphore(1, true)

    override fun safeOnNext(element: T) {
        threadWorker.run {
            semaphore.acquire()
            super.safeOnNext(element)
            semaphore.release()
        }
    }

    override fun onCancel() {
        threadWorker.runTerminal {
            semaphore.acquire()
            super.onCancel()
            semaphore.release()
        }
    }

    override fun onComplete() {
        threadWorker.runTerminal {
            semaphore.acquire()
            super.onComplete()
            semaphore.release()
        }
    }

    override fun onError(error: Throwable) {
        threadWorker.runTerminal {
            semaphore.acquire()
            super.onError(error)
            semaphore.release()
        }
    }
}

internal abstract class ThreadWorker {
    fun run(action: () -> Unit) {
        if (TestMode.isEnabled) {
            action()
        } else {
            post(action)
        }
    }

    fun runTerminal(action: () -> Unit) {
        if (TestMode.isEnabled) {
            action()
        } else {
            post {
                action()
                shutdown()
            }
        }
    }

    abstract fun post(action: () -> Unit)
    abstract fun shutdown()
}

internal class BackgroundThreadWorker : ThreadWorker() {

    private val threadPool by lazy { Executors.newCachedThreadPool() }

    override fun post(action: () -> Unit) {
        threadPool.submit(action)
    }

    override fun shutdown() {
        threadPool.shutdown()
    }
}

internal class ForegroundThreadWorker : ThreadWorker() {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun post(action: () -> Unit) {
        mainHandler.post(action)
    }

    override fun shutdown() {}
}
