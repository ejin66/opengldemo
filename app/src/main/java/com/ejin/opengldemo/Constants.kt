package com.ejin.opengldemo

val TYPE_DEFAULT = 0
val TYPE_TRIANGLE = 0
val TYPE_TRIANGLE_1 = 1
val TYPE_SQUARE = 2
val TYPE_OVAL = 3
val TYPE_CUBE = 4
val TYPE_CONE = 5
val TYPE_CYLINDER = 6
val TYPE_IMAGE_TEXTURE = 7
val TYPE_IMAGE_TEXTURE_GARY = 8
val TYPE_IMAGE_TEXTURE_BIG = 9
val TYPE_IMAGE_TEXTURE_DIM = 10
val TYPE_MOVE = 11

inline fun tryCatch(action: () -> Unit, err: (Exception) -> Unit) {
    try {
        action()
    } catch (e: Exception) {
       err(e)
    }
}