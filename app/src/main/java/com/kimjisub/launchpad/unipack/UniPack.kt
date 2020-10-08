package com.kimjisub.launchpad.unipack

import android.content.Context
import com.kimjisub.launchpad.R.string
import com.kimjisub.launchpad.manager.LaunchpadColor.ARGB
import com.kimjisub.launchpad.unipack.struct.AutoPlay
import com.kimjisub.launchpad.unipack.struct.LedAnimation
import com.kimjisub.launchpad.unipack.struct.Sound
import com.kimjisub.manager.FileManager.byteToMB
import com.kimjisub.manager.FileManager.getFolderSize
import com.kimjisub.manager.FileManager.sortByName
import com.kimjisub.manager.Log.err
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


class UniPack(val F_project: File, val loadDetail: Boolean) {
	var errorDetail: String? = null
	var criticalError = false

	var title: String? = null
	var producerName: String? = null
	var buttonX = 0
	var buttonY = 0
	var chain = 0
	var squareButton = true
	var website: String? = null

	var soundCount = 0
	var ledTableCount = 0

	var soundTable: Array<Array<Array<ArrayList<Sound>?>>>? = null
	var ledAnimationTable: Array<Array<Array<ArrayList<LedAnimation>?>>>? = null
	var autoPlayTable: AutoPlay? = null

	var F_info: File? = null
	var F_sounds: File? = null
	var F_keySound: File? = null
	var F_keyLed: File? = null
	var F_autoPlay: File? = null

	val infoExist
		get() = F_info != null
	val soundExist
		get() = F_sounds != null
	val keySoundExist
		get() = F_keySound != null
	val keyLedExist
		get() = F_keyLed != null
	val autoPlayExist
		get() = F_autoPlay != null

	init {
		checkFile()

		if (!criticalError) {
			info()

			if (loadDetail) {
				keySound()
				keyLed()
				autoPlay()
			}
		}
	}

	private fun checkFile() {
		for (item in F_project.listFiles()) {
			when (item.name.toLowerCase()) {
				"info" -> F_info = if (item.isFile) item else null
				"sounds" -> F_sounds = if (item.isDirectory) item else null
				"keysound" -> F_keySound = if (item.isFile) item else null
				"keyled" -> F_keyLed = if (item.isDirectory) item else null
				"autoplay" -> F_autoPlay = if (item.isFile) item else null
			}
		}

		if (F_info == null) addErr("info doesn't exist")
		if (F_keySound == null) addErr("keySound doesn't exist")
		if (F_info == null && F_keySound == null) addErr("It does not seem to be UniPack.")

		if (F_info == null || F_keySound == null)
			criticalError = true
	}

	private fun info() {
		if (F_info != null) {
			val reader = BufferedReader(InputStreamReader(FileInputStream(F_info)))
			while (true) {
				val s = reader.readLine() ?: break
				if (s.isEmpty()) continue
				try {
					val split = s.split("=", limit = 2)
					val key = split[0]
					val value = split[1]
					when (key) {
						"title" -> title = value
						"producerName" -> producerName = value
						"buttonX" -> buttonX = Integer.parseInt(value)
						"buttonY" -> buttonY = Integer.parseInt(value)
						"chain" -> chain = Integer.parseInt(value)
						"squareButton" -> squareButton = value == "true"
						"website" -> website = value
					}
				} catch (e: IndexOutOfBoundsException) {
					addErr("info : [$s] format is not found")
				}
			}
			if (title == null) addErr("info : title was missing")
			if (producerName == null) addErr("info : producerName was missing")
			if (buttonX == 0) addErr("info : buttonX was missing")
			if (buttonY == 0) addErr("info : buttonY was missing")
			if (chain == 0) addErr("info : chain was missing")
			if (chain !in 1..24) {
				addErr("info : chain out of range")
				criticalError = true
			}
			reader.close()
		}
	}

	private fun keySound() {
		if (F_keySound != null) {
			soundTable = Array(chain) {
				Array(buttonX) {
					arrayOfNulls<ArrayList<Sound>>(buttonY)
				}
			}
			soundCount = 0
			val reader = BufferedReader(InputStreamReader(FileInputStream(F_keySound)))
			while (true) {
				val s = reader.readLine() ?: break
				val split = s.split(" ").toTypedArray()
				var c: Int
				var x: Int
				var y: Int
				var soundURL: String
				var loop = 0
				var wormhole = -1
				try {
					if (split.size <= 2) continue
					c = Integer.parseInt(split[0]) - 1
					x = Integer.parseInt(split[1]) - 1
					y = Integer.parseInt(split[2]) - 1
					soundURL = split[3]
					if (split.size >= 5) loop = Integer.parseInt(split[4]) - 1
					if (split.size >= 6) {
						loop = Integer.parseInt(split[4]) - 1
						wormhole = Integer.parseInt(split[5]) - 1
					}
				} catch (e: NumberFormatException) {
					addErr("keySound : [$s] format is incorrect")
					continue
				} catch (e: IndexOutOfBoundsException) {
					addErr("keySound : [$s] format is incorrect")
					continue
				}
				if (c < 0 || c >= chain)
					addErr("keySound : [$s] chain is incorrect")
				else if (x < 0 || x >= buttonX)
					addErr("keySound : [$s] x is incorrect")
				else if (y < 0 || y >= buttonY) addErr(
					"keySound : [$s] y is incorrect"
				) else {
					val sound = Sound(File(F_sounds!!.absolutePath + "/" + soundURL), loop, wormhole)
					if (!sound.file.isFile) {
						addErr("keySound : [$s] sound was not found")
						continue
					}
					if (soundTable!![c][x][y] == null)
						soundTable!![c][x][y] = ArrayList()
					sound.num = soundTable!![c][x][y]!!.size
					soundTable!![c][x][y]!!.add(sound)
					soundCount++
				}
			}
			reader.close()
		}
	}

	private fun keyLed() {
		if (F_keyLed != null) {
			ledAnimationTable = Array(chain) {
				Array(buttonX) {
					arrayOfNulls<ArrayList<LedAnimation>?>(buttonY)
				}
			}
			ledTableCount = 0
			val fileList = sortByName(F_keyLed!!.listFiles())
			for (file in fileList) {
				if (file.isFile) {
					val fileName: String = file.name
					val split1 = fileName.split(" ").toTypedArray()
					var c: Int
					var x: Int
					var y: Int
					var loop = 1
					try {
						if (split1.size <= 2) continue
						c = Integer.parseInt(split1[0]) - 1
						x = Integer.parseInt(split1[1]) - 1
						y = Integer.parseInt(split1[2]) - 1
						if (split1.size >= 4) loop = Integer.parseInt(split1[3])
						if (c < 0 || c >= chain) {
							addErr("keyLed : [$fileName] chain is incorrect")
							continue
						} else if (x < 0 || x >= buttonX) {
							addErr("keyLed : [$fileName] x is incorrect")
							continue
						} else if (y < 0 || y >= buttonY) {
							addErr("keyLed : [$fileName] y is incorrect")
							continue
						} else if (loop < 0) {
							addErr("keyLed : [$fileName] loop is incorrect")
							continue
						}
					} catch (e: NumberFormatException) {
						addErr("keyLed : [$fileName] format is incorrect")
						continue
					} catch (e: IndexOutOfBoundsException) {
						addErr("keyLed : [$fileName] format is incorrect")
						continue
					}
					val ledList = ArrayList<LedAnimation.LedEvent>()
					val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
					loop@ while (true) {
						val s = reader.readLine() ?: break
						val split2 = s.split(" ").toTypedArray()
						var option: String
						var _x = -1
						var _y = -1
						var _color = -1
						var _velo = 4
						var _delay = -1
						try {
							if (split2[0] == "") continue
							option = split2[0]
							when (option) {
								"on", "o" -> {
									try {
										_x = Integer.parseInt(split2[1]) - 1
									} catch (ignore: NumberFormatException) {
									}
									_y = Integer.parseInt(split2[2]) - 1
									if (split2.size == 4) _color =
										Integer.parseInt(split2[3], 16) + -0x1000000 else if (split2.size == 5) {
										if (split2[3] == "auto" || split2[3] == "a") {
											_velo = Integer.parseInt(split2[4])
											_color = ARGB[_velo].toInt()
										} else {
											_velo = Integer.parseInt(split2[4])
											_color = Integer.parseInt(split2[3], 16) + -0x1000000
										}
									} else {
										addErr("keyLed : [$fileName].[$s] format is incorrect")
										continue@loop
									}
								}
								"off", "f" -> {
									try {
										_x = Integer.parseInt(split2[1]) - 1
									} catch (ignore: NumberFormatException) {
									}
									_y = Integer.parseInt(split2[2]) - 1
								}
								"delay", "d" -> _delay = Integer.parseInt(split2[1])
								else -> {
									addErr("keyLed : [$fileName].[$s] format is incorrect")
									continue@loop
								}
							}
						} catch (e: NumberFormatException) {
							addErr("keyLed : [$fileName].[$s] format is incorrect")
							continue
						} catch (e: IndexOutOfBoundsException) {
							addErr("keyLed : [$fileName].[$s] format is incorrect")
							continue
						}
						when (option) {
							"on", "o" -> ledList.add(LedAnimation.LedEvent.On(_x, _y, _color, _velo))
							"off", "f" -> ledList.add(LedAnimation.LedEvent.Off(_x, _y))
							"delay", "d" -> ledList.add(LedAnimation.LedEvent.Delay(_delay))
						}
					}
					if (ledAnimationTable!![c][x][y] == null)
						ledAnimationTable!![c][x][y] = ArrayList()
					ledAnimationTable!![c][x][y]!!.add(LedAnimation(ledList, loop, ledAnimationTable!![c][x][y]!!.size))
					ledTableCount++
					reader.close()
				} else addErr("keyLed : " + file.name + " is not file")
			}
		}
	}

	private inline fun autoPlay() {
		if (F_autoPlay != null) {
			autoPlayTable = AutoPlay(ArrayList())
			val map = Array(buttonX) { IntArray(buttonY) }
			var currChain = 0
			val reader = BufferedReader(InputStreamReader(FileInputStream(F_autoPlay)))
			loop@ while (true) {
				val s = reader.readLine() ?: break
				val split = s.split(" ").toTypedArray()
				var option: String
				var x = -1
				var y = -1
				var chain = -1
				var delay = -1
				try {
					if (split[0] == "") continue
					option = split[0]
					when (option) {
						"on", "o" -> {
							x = Integer.parseInt(split[1]) - 1
							y = Integer.parseInt(split[2]) - 1
							if (x < 0 || x >= buttonX) {
								addErr("autoPlay : [$s] x is incorrect")
								continue@loop
							}
							if (y < 0 || y >= buttonY) {
								addErr("autoPlay : [$s] y is incorrect")
								continue@loop
							}
						}
						"off", "f" -> {
							x = Integer.parseInt(split[1]) - 1
							y = Integer.parseInt(split[2]) - 1
							if (x < 0 || x >= buttonX) {
								addErr("autoPlay : [$s] x is incorrect")
								continue@loop
							} else if (y < 0 || y >= buttonY) {
								addErr("autoPlay : [$s] y is incorrect")
								continue@loop
							}
						}
						"touch", "t" -> {
							x = Integer.parseInt(split[1]) - 1
							y = Integer.parseInt(split[2]) - 1
							if (x < 0 || x >= buttonX) {
								addErr("autoPlay : [$s] x is incorrect")
								continue@loop
							} else if (y < 0 || y >= buttonY) {
								addErr("autoPlay : [$s] y is incorrect")
								continue@loop
							}
						}
						"chain", "c" -> {
							chain = Integer.parseInt(split[1]) - 1
							if (chain < 0 || chain >= this.chain) {
								addErr("autoPlay : [$s] chain is incorrect")
								continue@loop
							}
						}
						"delay", "d" -> delay = Integer.parseInt(split[1])
						else -> {
							addErr("autoPlay : [$s] format is incorrect")
							continue@loop
						}
					}
				} catch (e: NumberFormatException) {
					addErr("autoPlay : [$s] format is incorrect")
					continue
				} catch (e: IndexOutOfBoundsException) {
					addErr("autoPlay : [$s] format is incorrect")
					continue
				}
				when (option) {
					"on", "o" -> {
						autoPlayTable!!.elements.add(AutoPlay.Element.On(x, y, currChain, map[x][y]))
						val sound = Sound_get(currChain, x, y, map[x][y])
						map[x][y]++
						if (sound != null && sound.wormhole != -1) {
							autoPlayTable!!.elements.add(AutoPlay.Element.Chain(sound.wormhole.also { currChain = it }))
							var i = 0
							while (i < buttonX) {
								var j = 0
								while (j < buttonY) {
									map[i][j] = 0
									j++
								}
								i++
							}
						}
					}
					"off", "f" -> autoPlayTable!!.elements.add(AutoPlay.Element.Off(x, y, currChain))
					"touch", "t" -> {
						autoPlayTable!!.elements.add(AutoPlay.Element.On(x, y, currChain, map[x][y]))
						autoPlayTable!!.elements.add(AutoPlay.Element.Off(x, y, currChain))
						map[x][y]++
					}
					"chain", "c" -> {
						autoPlayTable!!.elements.add(AutoPlay.Element.Chain(chain.also { currChain = it }))
						var i = 0
						while (i < buttonX) {
							var j = 0
							while (j < buttonY) {
								map[i][j] = 0
								j++
							}
							i++
						}
					}
					"delay", "d" -> autoPlayTable!!.elements.add(AutoPlay.Element.Delay(delay, currChain))
				}
			}
			reader.close()
		}
	}

	// Circular Queue /////////////////////////////////////////////////////////////////////////////////////////

	fun Sound_get(c: Int, x: Int, y: Int): Sound? {
		return try {
			soundTable!![c][x][y]!![0]
		} catch (ignored: NullPointerException) {
			null
		} catch (ee: IndexOutOfBoundsException) {
			err("Sound_get ($c, $x, $y)")
			null
		}
	}

	fun Sound_get(c: Int, x: Int, y: Int, num: Int): Sound? {
		return try {
			val sound = soundTable!![c][x][y]
			soundTable!![c][x][y]!![num % sound!!.size]
		} catch (ignored: NullPointerException) {
			null
		} catch (e: IndexOutOfBoundsException) {
			err("Sound_get ($c, $x, $y)")
			null
		}
	}

	fun Sound_push(c: Int, x: Int, y: Int) {
		try {
			val item = soundTable!![c][x][y]!!.removeAt(0)
			soundTable!![c][x][y]!!.add(item)
		} catch (ignored: NullPointerException) {
		} catch (ee: IndexOutOfBoundsException) {
			err("Sound_push ($c, $x, $y)")
		}
	}

	fun Sound_push(c: Int, x: Int, y: Int, num: Int) {
		try {
			val e = soundTable!![c][x][y]
			if (soundTable!![c][x][y]!![0].num != num)
				while (true) {
					val tmp = e!![0]
					e.removeAt(0)
					e.add(tmp)
					if (e[0].num == num % e.size)
						break
				}
		} catch (ignored: NullPointerException) {
		} catch (ee: IndexOutOfBoundsException) {
			err("Sound_push ($c, $x, $y, $num)")
		} catch (ee: ArithmeticException) {
			err("ArithmeticException : Sound_push ($c, $x, $y, $num)")
		}
	}


	fun led_get(c: Int, x: Int, y: Int): LedAnimation? {
		return try {
			ledAnimationTable!![c][x][y]!![0]
		} catch (ignored: NullPointerException) {
			null
		} catch (ee: IndexOutOfBoundsException) {
			err("LED_get ($c, $x, $y)")
			null
		}
	}

	fun led_push(c: Int, x: Int, y: Int) {
		try {
			val item = ledAnimationTable!![c][x][y]!!.removeAt(0)
			ledAnimationTable!![c][x][y]!!.add(item)
		} catch (ignored: NullPointerException) {
		} catch (e: IndexOutOfBoundsException) {
			err("LED_push ($c, $x, $y)")
		}
	}

	fun led_push(c: Int, x: Int, y: Int, num: Int) {
		try {
			val e: ArrayList<LedAnimation>? = ledAnimationTable!![c][x][y]
			if (e!![0].num != num)
				while (true) {
					val item = e.removeAt(0)
					e.add(item)
					if (e[0].num == num % e.size) break
				}
		} catch (ignored: NullPointerException) {
		} catch (ee: IndexOutOfBoundsException) {
			err("LED_push ($c, $x, $y, $num)")
		}
	}

	// etc /////////////////////////////////////////////////////////////////////////////////////////


	private fun addErr(content: String) {
		if (errorDetail == null)
			errorDetail = content
		else
			errorDetail += "\n" + content
	}

	fun toString(context: Context): String {
		return context.resources.getString(string.title) + " : " + title + "\n" +
				context.resources.getString(string.producerName) + " : " + producerName + "\n" +
				context.resources.getString(string.padSize) + " : " + buttonX.toString() + " x " + buttonY.toString() + "\n" +
				context.resources.getString(string.chainCount) + " : " + chain.toString() + "\n" +
				context.resources.getString(string.fileSize) + " : " + byteToMB(
			getFolderSize(
				F_project
			)
		) + " MB"
	}
}