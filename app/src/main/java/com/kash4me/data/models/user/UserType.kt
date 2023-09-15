package com.kash4me.data.models.user

enum class UserType(val id: Int) {

    CUSTOMER(id = 1),
    MERCHANT(id = 2),
    STAFF(id = 3),
    ANONYMOUS(id = 4)

}