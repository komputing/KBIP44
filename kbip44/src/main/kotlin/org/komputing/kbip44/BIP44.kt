package org.komputing.kbip44

/*
BIP44 as in https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki
 */

const val BIP44_HARDENING_FLAG = 0x80000000.toInt()
const val BIP44_PREFIX = "m"

private fun getEnsuredCleanPath(path: String): String {
    if (!path.trim().startsWith(BIP44_PREFIX)) {
        throw (IllegalArgumentException("Must start with $BIP44_PREFIX"))
    }
    return path.removePrefix(BIP44_PREFIX).removePrefix("/").replace(" ", "")
}

data class BIP44Element(val hardened: Boolean, val number: Int) {
    val numberWithHardeningFlag = if (hardened) number or BIP44_HARDENING_FLAG else number
}

data class BIP44(val path: List<BIP44Element>) {
    constructor(path: String) : this(getEnsuredCleanPath(path).let { cleanPath ->
        if (cleanPath.isEmpty()) {
            emptyList()
        } else {
            cleanPath.split("/")
                .asSequence()
                .map {
                    BIP44Element(
                        hardened = it.endsWith("'"),
                        number = it.removeSuffix("'").toIntOrNull()
                            ?: throw IllegalArgumentException("not a number '$it' in path $path")
                    )
                }
                .toList()
        }
    })

    override fun equals(other: Any?) = (other as? BIP44)?.path == path
    override fun hashCode() = path.hashCode()
    override fun toString() = ("$BIP44_PREFIX/" + path.joinToString("/") {
        if (it.hardened) "${it.number}'" else "${it.number}"
    }).removeSuffix("/")

    fun increment() = BIP44(path.subList(0, path.size - 1) +
            path.last().let { BIP44Element(it.hardened, it.number + 1) })
}
