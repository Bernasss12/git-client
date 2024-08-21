package model.references

class PartialHash(hash: String) : Reference(hash) {
    init {
        require(hash.length in 7..40) {
            "[PartialHash.Constructor] Partial Hash string length should be 7 or more characters long, but less than 40:\n[$hash] (${hash.length})"
        }
    }
}