package cn.zenliu.kotlin.translator

import java.io.*
import kotlin.streams.*

object StreamMapDictFactory {
	class StreamDict internal constructor(textInputStream: InputStream) : MapDictionary() {
		override val mapping: Map<String, Array<String>> = textInputStream
			.let { BufferedReader(InputStreamReader(it)) }
			.lines()
			.map {
				it
					.split(KEY_VALUE_SPLITER)
					.dropLastWhile { it.isEmpty() }
					.toTypedArray()
					.takeIf { it.size == 2 }
					?.let {
						it[1] to it[0].split(VALUES_SPLITER).dropLastWhile { it.isNullOrBlank() }.toTypedArray()
					}
			}
			.toList()
			.filterNotNull()
			.toMap()
	}

	var KEY_VALUE_SPLITER = ' '
	var VALUES_SPLITER = '\''
	/**
	 * build dictionary from format inputstream
	 * @param textInputStream InputStream
	 * @return StreamDict
	 */
	fun buildDict(textInputStream: InputStream) = StreamDict(textInputStream)

}
