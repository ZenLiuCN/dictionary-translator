@file:JvmName("ChinaCityDictionary")
package cn.zenliu.kotlin.translator.data

import cn.zenliu.kotlin.translator.*

fun chinaCityDict() = Thread
	.currentThread()
	.contextClassLoader
	.getResourceAsStream("chinaCity")?.let(StreamMapDictFactory::buildDict)
