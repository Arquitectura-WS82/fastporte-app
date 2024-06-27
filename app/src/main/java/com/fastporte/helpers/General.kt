package com.fastporte.helpers

import java.util.Calendar

class General {

    // Función para calcular edad
    companion object {
        fun calculateAge(birthdate: String): Int {

            // Si birthdate es diferente de un formato de fecha entonces se retorna 0
            if (!birthdate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                return 0
            }

            val birthdateArray = birthdate.split("/")
            val birthYear = birthdateArray[2].toInt()
            val birthMonth = birthdateArray[1].toInt()
            val birthDay = birthdateArray[0].toInt()

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            var age = currentYear - birthYear

            if (currentMonth < birthMonth) {
                age--
            } else if (currentMonth == birthMonth && currentDay < birthDay) {
                age--
            }

            return age
        }
    }
}
