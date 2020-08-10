package com.manu.connect.model

class NotificationData {
    private var user : String? = ""
    private var icon : Int? = 0
    private var body : String? = ""
    private var title : String? = ""
    private var sentToUser : String? = ""

    constructor()

    constructor(user: String?, icon: Int?, body: String?, title: String?, sentToUser: String?) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sentToUser = sentToUser
    }

    fun getUser():String? {
        return user
    }

    fun setUser(user : String?){
        this.user = user
    }

    fun getIcon():Int? {
        return icon
    }

    fun setIcon(icon : Int?){
        this.icon = icon
    }

    fun getBody():String? {
        return body
    }

    fun setBody(body : String?){
        this.body = body
    }

    fun getTitle():String? {
        return title
    }

    fun setTitle(title : String?){
        this.title = title
    }

    fun getSentToUser():String? {
        return sentToUser
    }

    fun setSentToUser(sentToUser : String?){
        this.sentToUser = sentToUser
    }

}