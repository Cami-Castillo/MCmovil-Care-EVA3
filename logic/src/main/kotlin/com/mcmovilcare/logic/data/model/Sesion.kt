package com.mcmovilcare.logic.data.model

import com.mcmovilcare.logic.data.model.*
import com.mcmovilcare.logic.data.remote.*
import com.mcmovilcare.logic.repository.*
import com.mcmovilcare.logic.domain.*

data class EntradaSesion(val usuario: String, val clave: String)

data class Sesion(val token: String, val usuario: String, val rol: String)

