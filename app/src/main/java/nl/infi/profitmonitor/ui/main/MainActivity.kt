package nl.infi.profitmonitor.ui.main

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.activity_main.*
import nl.infi.profitmonitor.R
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.add
import rx.subscriptions.CompositeSubscription
import java.text.DateFormat
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity(), MainMvpView {

    private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
    private val numberFormat = NumberFormat.getInstance()

    private val presenter = MainPresenter()

    private var liveCycleSubscriptions: CompositeSubscription? = null

    init {
        numberFormat.minimumIntegerDigits = 1
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)

        val startDateObservable = RxView.clicks(button_start_date).flatMap { getDate() }.share()
        val endDateObservable = RxView.clicks(button_end_date).flatMap { getDate() }.share()
        presenter.observeDates(startDateObservable, endDateObservable)

        val sub = CompositeSubscription()

        sub.add(startDateObservable.subscribe {
            textview_start_date.text = dateFormat.format(it)
        })

        sub.add(endDateObservable.subscribe {
            textview_end_date.text = dateFormat.format(it)
        })

        liveCycleSubscriptions?.unsubscribe()
        liveCycleSubscriptions = sub
    }

    override fun onDestroy() {
        liveCycleSubscriptions?.unsubscribe()
        presenter.detachView()
        super.onDestroy()
    }

    private fun getDate(): Observable<Date> {
        return Observable.create { subscriber: Subscriber<in Date> ->
            if (subscriber.isUnsubscribed) return@create

            val dialog = DatePickerDialog(this)

            dialog.setOnDateSetListener { datePicker, year, month, day ->
                if (subscriber.isUnsubscribed) return@setOnDateSetListener

                val calendar = Calendar.getInstance()
                calendar.clear()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                subscriber.onNext(calendar.time)
                subscriber.onCompleted()
            }

            dialog.setOnDismissListener {
                if (subscriber.isUnsubscribed) return@setOnDismissListener
                subscriber.onCompleted()
            }

            subscriber.add { dialog.dismiss() }
            dialog.show()
        }
    }

    override fun updateRevenue(amount: Float?) {
        if (amount == null) {
            textview_revenue.text = getString(R.string.no_data)
            return
        }
        textview_revenue.text = StringBuilder("€ ").append(numberFormat.format(amount)).toString()
    }

    override fun updateProfit(amount: Float?) {
        if (amount == null) {
            textview_profit.text = getString(R.string.no_data)
            return
        }
        textview_profit.text = StringBuilder("€ ").append(numberFormat.format(amount)).toString()
    }

    override fun showError() {
        AlertDialog.Builder(this)
                .setTitle(R.string.errordialog_title)
                .setMessage(R.string.errordialog_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }
}
