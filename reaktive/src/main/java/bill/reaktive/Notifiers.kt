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

object Notifiers {
    fun onSubscribe(setup: (NotifierSubscriber) -> Unit): Notifier = HotNotifier(setup)
}

interface NotifierSubscriber {
    fun onNext()
    fun onCancel()
}

interface Notifier {
    fun subscribe(subscriber: NotifierSubscriber? = null): Subscription
    fun doOnFinish(action: () -> Unit): Notifier
    fun <T> toPublisher(factory: () -> T): Publisher<T>
}

internal open class HotNotifier(private val setup: (NotifierSubscriber) -> Unit) : Notifier, NotifierSubscriber, Subscription {

    private var subscriber: NotifierSubscriber? = null
    private var finishAction: (() -> Unit)? = null

    override fun subscribe(subscriber: NotifierSubscriber?): Subscription {
        this.subscriber = subscriber
        setup(this)
        return this
    }

    override fun onCancel() {
        finishAction?.invoke()
        subscriber?.onCancel()
    }

    override fun doOnFinish(action: () -> Unit): Notifier {
        this.finishAction = action
        return this
    }

    override fun <T> toPublisher(factory: () -> T): Publisher<T> {
        return Publishers.onSubscribe<T> {
            this@HotNotifier.subscribe(object : NotifierSubscriber {
                override fun onNext() {
                    it.onNext(factory())
                }

                override fun onCancel() {
                }
            })
        }.doOnCancel {
            this@HotNotifier.onCancel()
        }
    }

    override fun onNext() {
        subscriber?.onNext()
    }

    override fun cancel() = onCancel()
}
