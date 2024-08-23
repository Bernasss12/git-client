package model

import model.references.Hash

data class ReferenceLine(val hash: Hash, val name: String, val capabilities: List<String>)
