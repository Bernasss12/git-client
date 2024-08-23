package model.git.commit

import model.references.Hash
import model.git.Commit

data class Parents(val primary: Hash?, val secondary: Hash?) {
    val primaryCommit: Commit? by lazy {
        Local.readTypedObjectFromDiskByReference<Commit>(
            primary ?: return@lazy null
        )
    }

    val secondaryCommit: Commit? by lazy {
        Local.readTypedObjectFromDiskByReference<Commit>(
            secondary ?: return@lazy null
        )
    }
}