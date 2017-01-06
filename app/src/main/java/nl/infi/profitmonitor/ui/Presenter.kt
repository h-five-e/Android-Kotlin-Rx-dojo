package nl.infi.profitmonitor.ui

interface Presenter<V : MvpView> {
    fun attachView(mvpView: V)
    fun detachView()
}