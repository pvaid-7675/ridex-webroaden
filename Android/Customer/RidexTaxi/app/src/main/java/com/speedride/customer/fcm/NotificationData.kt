package com.speedride.customer.fcm

class NotificationData {

    var imageName: String? = null
    var id = 0
    var title: String? = null
    var textMessage: String? = null
    var sound: String? = null

    constructor() {}
    constructor(imageName: String?, id: Int, title: String?, textMessage: String?, sound: String?) {
        this.imageName = imageName
        this.id = id
        this.title = title
        this.textMessage = textMessage
        this.sound = sound
    }

    companion object {
        const val TEXT = "TEXT"
    }
}