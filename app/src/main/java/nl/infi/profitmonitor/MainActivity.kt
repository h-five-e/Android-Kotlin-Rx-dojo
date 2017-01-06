package nl.infi.profitmonitor

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.activity_main.*
import rx.subscriptions.CompositeSubscription
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var liveCycleSubscriptions: CompositeSubscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sub = CompositeSubscription()

        sub.add(RxView.clicks(button_start_date).subscribe {
            Timber.i("Button start date clicked")
        })

        sub.add(RxView.clicks(button_end_date).subscribe {
            Timber.i("Button end date clicked")
        })

        liveCycleSubscriptions?.unsubscribe()
        liveCycleSubscriptions = sub
    }

    override fun onDestroy() {
        liveCycleSubscriptions?.unsubscribe()
        super.onDestroy()
    }
}
