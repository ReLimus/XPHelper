package top.sacz.hook

import top.sacz.xphelper.dexkit.DexFinder

class DexkitInject {

    fun test() {
        val method = DexFinder.findMethodOrNull {

        }.firstOrNull()
    }
}