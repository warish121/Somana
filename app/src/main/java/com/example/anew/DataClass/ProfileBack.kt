package com.example.anew.DataClass

import com.example.anew.DataClass.Users

data class ProfileBack(
    val profileImg: String = ""
){
    constructor(): this("")
}

data class BannerBack(
    val bannerImg: String = ""
){
    constructor(): this("")
}
