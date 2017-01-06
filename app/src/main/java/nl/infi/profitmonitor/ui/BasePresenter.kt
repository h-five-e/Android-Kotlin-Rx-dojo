package nl.infi.profitmonitor.ui

import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class BasePresenter<V : MvpView> : Presenter<V> {

    var mvpView: V? = null
        private set

    private var viewLifeCycleSubscriptions = CompositeSubscription()

    override fun attachView(mvpView: V) {
        this.mvpView = mvpView
        viewLifeCycleSubscriptions = CompositeSubscription()
    }

    override fun detachView() {
        mvpView = null
        viewLifeCycleSubscriptions.unsubscribe()
    }

    val isAttached: Boolean
        get() = mvpView != null

    fun checkAttached() {
        if (!isAttached) {
            throw RuntimeException("Presenter not attached")
        }
    }

    protected fun addViewLifeCycleSubscription(subscription: Subscription): Subscription {
        viewLifeCycleSubscriptions.add(subscription)
        return subscription
    }

    protected fun removeViewLifeCycleSubscription(subscription: Subscription) =
            viewLifeCycleSubscriptions.remove(subscription)
}