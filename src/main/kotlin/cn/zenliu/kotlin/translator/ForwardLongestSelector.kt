package cn.zenliu.kotlin.translator

import org.ahocorasick.trie.*

internal  val EMIT_COMPARATOR = Comparator<Emit> { o1, o2 ->
	when {
		o1.getStart() == o2.getStart() -> // 起点相同时，更长的排前面
			when {
				o1.size() < o2.size() -> 1
				o1.size() == o2.size() -> 0
				else -> -1
			}
		else -> // 起点小的放前面
			when {
				o1.getStart() < o2.getStart() -> -1
				o1.getStart() == o2.getStart() -> 0
				else -> 1
			}
	}
}
internal class ForwardLongestSelector : SegmentationSelector {
	override fun select(emits: Collection<Emit>) = emits
		.sortedWith(EMIT_COMPARATOR)
		.let {
			var endValueToRemove = -1
			it.filter { emit ->
				if (emit.start > endValueToRemove && emit.end > endValueToRemove) {
					endValueToRemove = emit.end
					false
				} else {
					true
				}
			}
		}
}
