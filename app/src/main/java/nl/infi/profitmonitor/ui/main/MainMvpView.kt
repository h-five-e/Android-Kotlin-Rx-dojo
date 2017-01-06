package nl.infi.profitmonitor.ui.main

import nl.infi.profitmonitor.ui.MvpView

interface MainMvpView : MvpView {
    fun updateRevenue(amount: Float?)
    fun updateProfit(amount: Float?)
}