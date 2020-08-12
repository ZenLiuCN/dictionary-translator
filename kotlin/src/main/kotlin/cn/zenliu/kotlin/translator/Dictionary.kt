package cn.zenliu.kotlin.translator

interface Dictionary {
	fun words(): Set<String>
	fun find(word: String): Array<String>?
}
