package bill.reaktive.publishers

import bill.reaktive.BasePublisher
import bill.reaktive.Subscriber

internal abstract class SubscriberPublisher<T> : BasePublisher<T>(), Subscriber<T> {
    protected lateinit var subscriber: Subscriber<T>

    override fun onNext(element: T) = subscriber.onNext(element)
    override fun onComplete() = subscriber.onComplete()
    override fun onCancel() = subscriber.onCancel()
    override fun onError(error: Throwable) = subscriber.onError(error)
}

internal abstract class MappingSubscriberPublisher<T, V> : BasePublisher<V>(), Subscriber<T> {
    protected lateinit var subscriber: Subscriber<V>

    protected abstract fun map(element: T): V

    override fun onNext(element: T) = subscriber.onNext(map(element))
    override fun onComplete() = subscriber.onComplete()
    override fun onCancel() = subscriber.onCancel()
    override fun onError(error: Throwable) = subscriber.onError(error)
}
