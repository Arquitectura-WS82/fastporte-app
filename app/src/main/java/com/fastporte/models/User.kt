package com.fastporte.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

class User(
    @SerializedName("birthdate") var birthdate: String,
    @SerializedName("description") var description: String,
    @SerializedName("email") var email: String,
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("lastname") var lastname: String,
    @SerializedName("username") var username: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("region") var region: String,
    @SerializedName("password") var password: String,
    @SerializedName("photo") var photo: String
) : Serializable {
    override fun toString(): String {
        return "User(birthdate='$birthdate', description='$description', email='$email', id=$id, name='$name', lastname='$lastname', username='$username', phone='$phone', region='$region', password='$password', photo='$photo')"
    }
}