package util.model

import model.Commit
import model.GitObject.Companion.readTypedFromObjectFile

data class Parents(val primary: Hash?, val secondary: Hash?) {
    val primaryCommit: Commit?
        get() {
            return readTypedFromObjectFile<Commit>(
                primary ?: return null
            )
        }

    val secondaryCommit: Commit?
        get() {
            return readTypedFromObjectFile<Commit>(
                secondary ?: return null
            )
        }
}