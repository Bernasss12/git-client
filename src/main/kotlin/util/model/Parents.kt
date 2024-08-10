package util.model

import model.Commit
import model.GitObject.Companion.readFromObjectFile

data class Parents(val primary: Hash?, val secondary: Hash?) {
    val primaryCommit: Commit?
        get() {
            return readFromObjectFile<Commit>(
                primary ?: return null
            )
        }

    val secondaryCommit: Commit?
        get() {
            return readFromObjectFile<Commit>(
                secondary ?: return null
            )
        }
}