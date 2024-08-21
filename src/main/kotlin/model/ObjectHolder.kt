package model

import ObjectTypes
import model.references.Hash

sealed class ObjectHolder

class GitObjectHolder(val type: ObjectTypes, val bytes: ByteArray) : ObjectHolder()
class DeltaObjectHolder(val type: ObjectTypes, val hash: Hash, val bytes: ByteArray) : ObjectHolder()