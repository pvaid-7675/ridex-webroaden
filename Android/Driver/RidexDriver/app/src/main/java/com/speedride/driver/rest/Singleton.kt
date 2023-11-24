package com.speedride.driver.rest



class Singleton {


    companion object {
        private val TAG = Singleton::class.java.simpleName
        private var mInstance: Singleton? = null
        private  var mRestGenerator: RetrofitBuilder = RetrofitBuilder()
        val instance: Singleton?
            get() {
                if (mInstance == null) {
                    mInstance = Singleton()
                }
                return mInstance
            }
        val restClient: RestClient
            get() = mRestGenerator.restClient
    }
}