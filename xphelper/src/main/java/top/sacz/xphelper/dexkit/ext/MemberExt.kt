package top.sacz.xphelper.dexkit.ext

import top.sacz.xphelper.dexkit.FieldFinder
import top.sacz.xphelper.dexkit.MethodFinder
import top.sacz.xphelper.util.DexFieldDescriptor
import top.sacz.xphelper.util.DexMethodDescriptor
import java.lang.reflect.Field
import java.lang.reflect.Method

fun Method.toMethodFinder(): MethodFinder = MethodFinder.from(this)

inline val Method.descriptor: String get() = DexMethodDescriptor(this).descriptor

fun Field.toFieldFinder(): FieldFinder = FieldFinder.from(this)

inline val Field.descriptor: String get() = DexFieldDescriptor(this).descriptor

