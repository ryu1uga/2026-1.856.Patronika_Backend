package pe.edu.ulima.patronika.exception

class ConflictException(message: String = "El recurso ya existe") : RuntimeException(message)