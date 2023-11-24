package com.speedride.customer.modules.login.API

import com.speedride.customer.interfaces.Clearable
import com.speedride.customer.modules.utils.BackgroundManager
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

class LoginService(private val backgroundManager: BackgroundManager) : Clearable, BackgroundManager.Listener {

    sealed class State {
        object Loading : State()
        object Loaded : State()
        data class Error(val error: Throwable) : State()
    }

    //var responseGetToken: ResponseGetToken? = null
    val stateObservable: BehaviorSubject<State> = BehaviorSubject.createDefault(State.Loading)
    private var topItemsDisposable_responseGetToken: Disposable? = null

    init {
        backgroundManager.registerListener(this)
    }

    fun GetToken(grant_type: String, username: String, password: String) {
        topItemsDisposable_responseGetToken?.dispose()
        stateObservable.onNext(State.Loading)
        /*topItemsDisposable_responseGetToken = APIManager.ServiceHelper.service()
            .GetToken(grant_type, username, password)
            .subscribeIO({
                responseGetToken = (it as ResponseGetToken)
                stateObservable.onNext(State.Loaded)
            }, {
                stateObservable.onNext(State.Error(it))
            })*/
    }

    override fun clear() {
        topItemsDisposable_responseGetToken?.dispose()
    }

    override fun willEnterForeground() {

    }
}