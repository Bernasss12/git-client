package util.model

data class Identity(
    val name: String,
    val email: String,
) {
    companion object {
        val DEFAULT = Identity(
            "name surname",
            "email@email.email"
        )
    }

    fun toIdentityString() = buildString {
        append(name)
        append(" <")
        append(email)
        append(">")
    }
}