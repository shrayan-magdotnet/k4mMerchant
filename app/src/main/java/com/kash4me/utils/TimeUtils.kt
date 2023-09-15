package com.kash4me.utils

import java.text.SimpleDateFormat
import java.util.Date

class TimeUtils {

    fun convertToYyyyMmDd(dateStr: String): String {
//        var date = "March 10, 2016 6:30:00"
        var simpleDateFormat = SimpleDateFormat("MMMMM dd, yyyy hh:mm:ss")
        val newDate: Date = simpleDateFormat.parse(dateStr)
        simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        return simpleDateFormat.format(newDate)
    }

}