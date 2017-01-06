package nl.infi.profitmonitor.ui.main

import nl.infi.profitmonitor.service.WebService
import nl.infi.profitmonitor.ui.BasePresenter
import rx.Observable
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.util.*

class MainPresenter : BasePresenter<MainMvpView>() {

    private val webService = WebService.instance
    private var revenueAndProfitSubs: CompositeSubscription? = null

    fun observeDates(startDate: Observable<Date>, endDate: Observable<Date>) {
        val dates = Observable.combineLatest(startDate, endDate, { startDate, endDate -> Pair(startDate, endDate) }).share()

        val subs = CompositeSubscription(
                dates.observeOn(Schedulers.io()).map { webService.getRevenue(it.first, it.second) }.subscribe { Timber.i("Revenue: %f", it)},
                dates.observeOn(Schedulers.io()).map { webService.getProfit(it.first, it.second) }.subscribe { Timber.i("Profit: %f", it)}
        )
        revenueAndProfitSubs?.unsubscribe()
        revenueAndProfitSubs = subs
        addViewLifeCycleSubscription(subs)
    }

}
