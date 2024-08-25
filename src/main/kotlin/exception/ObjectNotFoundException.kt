package exception

import io.ktor.utils.io.errors.*

class ObjectNotFoundException(message: String) : IOException(message)