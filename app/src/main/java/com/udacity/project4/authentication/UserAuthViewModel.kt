package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class UserAuthViewModel :ViewModel(){
    enum class AuthenticationState {
        LOGIN, LOGOUT
    }

    val authenticationState = UserAuthLiveData().map { user ->
        when (user) {
            null -> AuthenticationState.LOGOUT
            else -> AuthenticationState.LOGIN
        }
    }

}