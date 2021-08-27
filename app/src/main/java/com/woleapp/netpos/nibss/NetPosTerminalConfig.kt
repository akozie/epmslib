package com.woleapp.netpos.nibss

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.netpluspay.netpossdk.NetPosSdk
import com.netpluspay.netpossdk.NetPosSdk.writeTpkKey
import com.netpluspay.netpossdk.utils.DeviceConfig
import com.netpluspay.nibssclient.exception.NibssClientException
import com.netpluspay.nibssclient.models.ConfigurationParams
import com.netpluspay.nibssclient.models.KeyHolder
import com.netpluspay.nibssclient.service.NibssApiWrapper
import com.netpluspay.nibssclient.util.app.NibssClient
import com.pixplicity.easyprefs.library.Prefs
import com.woleapp.netpos.model.*
import com.woleapp.netpos.util.*
import com.woleapp.netpos.util.Singletons.getSavedConfigurationData
import com.woleapp.netpos.util.Singletons.gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

const val CONFIGURATION_STATUS = "terminal_configuration_status"
const val CONFIGURATION_ACTION = "com.woleapp.netpos.TERMINAL_CONFIGURATION"
const val DEFAULT_TERMINAL_ID = "2057H63U"

object NetPosTerminalConfig {
    private var configurationData: ConfigurationData = getSavedConfigurationData()
    private val disposables = CompositeDisposable()
    private var terminalId: String? = null
    var isConfigurationInProcess = false
    var configurationStatus = -1
    private val mutableLiveData = MutableLiveData(Event(-99))
    val liveData: LiveData<Event<Int>>
        get() = mutableLiveData
    private val sendIntent = Intent(CONFIGURATION_ACTION)

    fun getTerminalId() = terminalId ?: ""
    private fun setTerminalId(configurationData: ConfigurationData) {
        Timber.e("use storm TID ${useStormTerminalId()}")
        terminalId =
            if (useStormTerminalId()) Singletons.getCurrentlyLoggedInUser()?.terminal_id else configurationData.terminalId
    }

    private var keyHolder: KeyHolder? = null


    fun init(
        context: Context,
        newConfigurationData: ConfigurationData? = null,
        configureSilently: Boolean = false
    ) {
        Timber.e("configure silently: $configureSilently")
        newConfigurationData?.let { it ->
            configurationData = it
        }
        Timber.e(configurationData.toString())
        setTerminalId(configurationData)
        val terminalID = getTerminalId()
        NibssClient.init(
            "netpos.cert.pem",
            "private.key.pem",
            terminalID,
            NetPosSdk.getDeviceSerial()
        )
        NibssClient.useSSL(true)
        //NibssClient.useTestEnvironment(true)
        Timber.e("Terminal ID: $terminalId")
        keyHolder = Singletons.getKeyHolder()
        if (keyHolder != null) {
            configurationStatus = 1
            Timber.e("done here")
            return
        }
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        if (isConfigurationInProcess)
            return
        configurationStatus = 0
        sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
        localBroadcastManager.sendBroadcast(sendIntent)
        if (configureSilently.not()) {
            mutableLiveData.value = Event(configurationStatus)
            mutableLiveData.value = Event(-99)
        }
        NibssApiWrapper.configureTerminal(context, ConfigurationParams())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t1, t2 ->
                t1?.let {
                    Timber.e(it.toString())
                    Prefs.putString(PREF_KEYHOLDER, gson.toJson(it))
                    configurationStatus = 1
                    val tpkResult = writeTpkKey(DeviceConfig.TPKIndex, it.clearPinKey!!)
                    if (tpkResult != 0) {
                        Timber.e("write tpk failed")
                        Toast.makeText(context, "write tpk failed", Toast.LENGTH_SHORT).show()
                    } else {
                        //Toast.makeText(context, "write tpk success", Toast.LENGTH_SHORT).show()
                        Timber.e("write tpk success")
                    }
                    sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                    localBroadcastManager.sendBroadcast(sendIntent)
                    if (configureSilently.not()) {
                        mutableLiveData.value = Event(configurationStatus)
                        mutableLiveData.value = Event(-99)
                    }
                    Timber.e("Config data set")
                    disposeDisposables()
                }
                t2?.let {
                    if (it is NibssClientException) {
                        it.nibssError?.let { nibssError -> Timber.e(nibssError.toString()) }
                    }
                    Toast.makeText(
                        context,
                        it.localizedMessage ?: "Configuration Error",
                        Toast.LENGTH_SHORT
                    ).show()
                    configurationStatus = -1
                    if (configureSilently.not()) {
                        mutableLiveData.value = Event(configurationStatus)
                        mutableLiveData.value = Event(-99)
                    }
                    sendIntent.putExtra(CONFIGURATION_STATUS, configurationStatus)
                    localBroadcastManager.sendBroadcast(sendIntent)
                    Timber.e(it)
                }
            }.disposeWith(disposables)
    }


    private fun disposeDisposables() {
        disposables.clear()
    }
}
