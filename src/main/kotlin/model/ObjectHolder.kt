package model

import model.git.ObjectType
import model.references.Hash

sealed class ObjectHolder

class GitObjectHolder(val type: ObjectType, val bytes: ByteArray) : ObjectHolder()
class DeltaObjectHolder(val type: ObjectType, val hash: Hash, val bytes: ByteArray) : ObjectHolder()