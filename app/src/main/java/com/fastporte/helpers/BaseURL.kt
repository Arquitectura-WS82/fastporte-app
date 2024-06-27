package com.fastporte.helpers

enum class BaseURL {
    BASE_URL {
        override fun toString(): String {
            return "https://fastporte-web-services.azurewebsites.net/"
            //return "http://192.168.1.103:8080/"
        }
    }
}