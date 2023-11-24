package com.speedride.customer.modules.login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.speedride.customer.interfaces.Clearable
import com.speedride.customer.modules.login.API.LoginService
import com.speedride.customer.modules.login.model.ResponseGetToken
import com.speedride.customer.rest.contentHelper.Event
import com.speedride.customer.rest.subscribeIO
import io.reactivex.disposables.CompositeDisposable

class LoginViewModel(
    private val service: LoginService,
    private val clearables: List<Clearable>
) : ViewModel() {

    val errorLiveData = MutableLiveData<String?>(null)
    private val disposable = CompositeDisposable()
    val responseAuthenticateTest = MutableLiveData<Event<ResponseGetToken>>()

    init {
        service.stateObservable
            .subscribeIO {
                syncState_responseAuthenticateTest(it)
            }
            .let {
                disposable.add(it)
            }
    }

    private fun syncState_responseAuthenticateTest(state: LoginService.State) {
        /*when (state) {
            LoginService.State.Loading -> {
            }
            LoginService.State.Loaded -> {
                responseAuthenticateTest.postValue(Event(service.responseGetToken) as Event<ResponseGetToken>?)
            }
            is LoginService.State.Error -> {
                errorLiveData.postValue(convertErrorMessage(state.error))
            }
        }*/
    }

    private fun convertErrorMessage(it: Throwable): String {
        return it.message ?: it.javaClass.simpleName
    }

    override fun onCleared() {
        clearables.forEach(Clearable::clear)
        disposable.clear()
    }

    fun GetToken(grant_type: String, username: String, password: String) {
        service.GetToken(grant_type, username, password)
    }
}