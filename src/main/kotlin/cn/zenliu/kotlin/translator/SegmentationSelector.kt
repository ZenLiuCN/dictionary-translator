package cn.zenliu.kotlin.translator

import org.ahocorasick.trie.*

interface SegmentationSelector {
	fun select(emits: Collection<Emit>): List<Emit>
}
