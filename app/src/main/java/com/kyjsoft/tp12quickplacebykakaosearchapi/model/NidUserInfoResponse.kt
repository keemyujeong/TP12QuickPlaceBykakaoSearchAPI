package com.kyjsoft.tp12quickplacebykakaosearchapi.model

import android.provider.ContactsContract

data class NidUserInfoResponse(
    var resultcode: String,
    var message: String,
    var response : NaverIdUser
)
data class NaverIdUser(
    var id : String,
    var email: String,

)



