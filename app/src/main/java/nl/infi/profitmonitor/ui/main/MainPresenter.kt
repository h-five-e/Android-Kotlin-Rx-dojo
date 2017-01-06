package nl.infi.profitmonitor.ui.main

import nl.infi.profitmonitor.service.WebService
import nl.infi.profitmonitor.ui.BasePresenter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.util.*

class MainPresenter : BasePresenter<MainMvpView>() {

    private val webService = WebService.instance
    private var revenueAndProfitSubs: CompositeSubscription? = null

    fun observeDates(startDate: Observable<Date>, endDate: Observable<Date>) {
        val dates = Observable.combineLatest(startDate, endDate, { startDate, endDate -> Pair(startDate, endDate) }).share()

        val subs = CompositeSubscription(
                dates
                        .observeOn(Schedulers.io())
                        .map { webService.getRevenue(it.first, it.second) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { mvpView?.updateRevenue(it) },
                dates
                        .observeOn(Schedulers.io())
                        .map { webService.getProfit(it.first, it.second) }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { mvpView?.updateProfit(it) }
        )
        revenueAndProfitSubs?.unsubscribe()
        revenueAndProfitSubs = subs
        addViewLifeCycleSubscription(subs)
    }

}
