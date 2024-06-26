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
    @SerializedName("dimensionLength")
    val length: Double?,
    @SerializedName("dimensionWidth")
    val width: Double?,
    @SerializedName("dimensionHeight")
    val height: Double?
) : Serializable

