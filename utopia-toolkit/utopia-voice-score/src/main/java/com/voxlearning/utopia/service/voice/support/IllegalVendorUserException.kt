package com.voxlearning.utopia.service.voice.support

class IllegalVendorUserException(val code: String, message: String): IllegalArgumentException(message)