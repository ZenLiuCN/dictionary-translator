package cn.zenliu.kotlin.translator

abstract class MapDictionary : Dictionary {
	internal abstract val mapping: Map<String, Array<String>>
	override fun words() = mapping.keys
	override fun find(word: String) = mapping.get(word)
}
