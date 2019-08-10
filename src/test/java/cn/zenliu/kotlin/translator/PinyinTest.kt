package cn.zenliu.kotlin.translator

import cn.zenliu.kotlin.translator.py.*

internal class PinyinTest {

	@org.junit.jupiter.api.Test
	fun toPinyin() {
		assert(Translator.translate("举措")=="JU CUO")
		assert(Translator.translate("觊觎")=="JI YU")
	}

	@org.junit.jupiter.api.Test
	fun toPinyin1() {
		assert(Translator.translate('中')=="ZHONG")
		assert(Translator.translate('龃')=="JU")
	}

	@org.junit.jupiter.api.Test
	fun isChinese() {
		assert(Translator.isChinese('中'))
		assert(!Translator.isChinese('1'))
	}
}
