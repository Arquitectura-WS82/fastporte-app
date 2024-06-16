package com.fastporte.models

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class Vehicle(
    @SerializedName("id")
    val id: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("photo")
    val photo: String,
    @SerializedName("driver")
    var driver: User,
    @SerializedName("length")
    val length: Double?,
    @SerializedName("width")
    val width: Double?,
    @SerializedName("height")
    val height: Double?
) : Serializable

