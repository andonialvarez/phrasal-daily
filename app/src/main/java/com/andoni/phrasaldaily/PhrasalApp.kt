package com.andoni.phrasaldaily

import android.app.Application
import com.andoni.phrasaldaily.notify.Notifications

class PhrasalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannel(this)
    }
}
