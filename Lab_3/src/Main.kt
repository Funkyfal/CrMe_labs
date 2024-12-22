import java.io.File

fun main() {
    val inputFile = "in.bin"
    val keyFile = "key.bin"
    val outputFile = "out.bin"


    // Выбор операции
    println("Выберите операцию: 1 - Зашифровать, 2 - Расшифровать")
    val operation = readlnOrNull()?.toIntOrNull()

    if (operation == null || (operation != 1 && operation != 2)) {
        println("Неверный выбор операции!")
        return
    }

    // Чтение входных данных
    val inputData = File(inputFile).readBytes()
    val keyData = File(keyFile).readBytes()

    if (keyData.size != 8) {
        println("Ключ должен быть размером 8 байт!")
        return
    }

    // Выполнение операции
    val result = when (operation) {
        1 -> encrypt(inputData, keyData) // Шифрование
        2 -> decrypt(inputData, keyData) // Расшифровка
        else -> byteArrayOf()
    }

    // Запись результата
    File(outputFile).writeBytes(result)
    println("Операция завершена. Результат записан в $outputFile.")
}

// Функция шифрования
fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
    return processDES(data, key, true)
}

// Функция расшифровки
fun decrypt(data: ByteArray, key: ByteArray): ByteArray {
    return processDES(data, key, false)
}

// Основная функция DES
fun processDES(data: ByteArray, key: ByteArray, encrypt: Boolean): ByteArray {
    val blockSize = 8
    val result = mutableListOf<Byte>()

    // Разделение данных на блоки
    val blocks = data.asList().chunked(blockSize)
    for (block in blocks) {
        val paddedBlock = block.toByteArray().padToBlockSize(blockSize)
        val processedBlock = processBlock(paddedBlock, key, encrypt)
        result.addAll(processedBlock.toList())
    }

    return result.toByteArray()
}

// Обработка одного блока
fun processBlock(block: ByteArray, key: ByteArray, encrypt: Boolean): ByteArray {
    val initialPermuted = permute(block, IP)
    val (left, right) = initialPermuted.splitBlock()

    val roundKeys = generateRoundKeys(key)
    val finalKeys = if (encrypt) roundKeys else roundKeys.reversed()

    var l = left
    var r = right

    for (key in finalKeys) {
        val temp = r
        r = l xor f(r, key)
        l = temp
    }

    val combined = r + l
    return permute(combined, IP_INV)
}

// Функция F
fun f(block: ByteArray, key: ByteArray): ByteArray {
    val expanded = permute(block, E)
    val xored = expanded xor key
    val substituted = substitute(xored)
    return permute(substituted, P)
}

// Замена через S-блоки
fun substitute(block: ByteArray): ByteArray {
    val result = mutableListOf<Byte>()

    for (i in 0 until block.size step 6) {
        val chunk = block.copyOfRange(i, i + 6)
        val row = ((chunk[0].toInt() and 0b100000) shr 4) or (chunk[5].toInt() and 1)
        val col = (chunk[1].toInt() and 0b011110) shr 1

        val sBoxValue = S_BOXES[i / 6][row][col]
        result.add(sBoxValue.toByte())
    }

    return result.toByteArray()
}

// Генерация раундовых ключей
fun generateRoundKeys(key: ByteArray): List<ByteArray> {
    val permutedKey = permute(key, PC1)
    var (left, right) = permutedKey.splitBlock()

    val roundKeys = mutableListOf<ByteArray>()
    for (shift in SHIFT_TABLE) {
        left = left.circularLeftShift(shift)
        right = right.circularLeftShift(shift)
        roundKeys.add(permute(left + right, PC2))
    }

    return roundKeys
}

// Утилиты
fun ByteArray.splitBlock(): Pair<ByteArray, ByteArray> =
    this.copyOfRange(0, this.size / 2) to this.copyOfRange(this.size / 2, this.size)

fun ByteArray.padToBlockSize(size: Int): ByteArray =
    if (this.size == size) this else this + ByteArray(size - this.size) { 0 }

fun permute(input: ByteArray, table: IntArray): ByteArray {
    val output = ByteArray(table.size / 8) // Результат должен иметь размер кратный байтам
    for (i in table.indices) {
        val bit = (input[(table[i] - 1) / 8].toInt() shr (7 - (table[i] - 1) % 8)) and 1
        output[i / 8] = (output[i / 8].toInt() or (bit shl (7 - (i % 8)))).toByte()
    }
    return output
}


infix fun ByteArray.xor(other: ByteArray): ByteArray =
    this.zip(other) { a, b -> (a.toInt() xor b.toInt()).toByte() }.toByteArray()

fun ByteArray.circularLeftShift(bits: Int): ByteArray {
    val value = this.fold(0L) { acc, byte -> (acc shl 8) or (byte.toLong() and 0xFF) }
    val sizeBits = this.size * 8

    val shiftedValue = ((value shl bits) or (value shr (sizeBits - bits))) and ((1L shl sizeBits) - 1)

    return ByteArray(this.size) { i ->
        ((shiftedValue shr ((this.size - 1 - i) * 8)) and 0xFF).toByte()
    }
}


val IP = intArrayOf(58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4,
    62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
    57, 49, 41, 33, 25, 17,  9, 1, 59, 51, 43, 35, 27, 19, 11, 3,
    61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7)

val IP_INV = intArrayOf(40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
    38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
    36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
    34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41,  9, 49, 17, 57, 25)

// Таблица расширения E
val E = intArrayOf(32,  1,  2,  3,  4,  5,  4,  5,  6,  7,  8,  9,
    8,   9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17,
    16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25,
    24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32,  1)

// S-блоки (S-Boxes)
val S_BOXES = arrayOf(
    arrayOf(
        intArrayOf(14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7),
        intArrayOf(0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8),
        intArrayOf(4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0),
        intArrayOf(15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13)
    ),
    arrayOf(
        intArrayOf(15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10),
        intArrayOf(3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5),
        intArrayOf(0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15),
        intArrayOf(13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9)
    ),
    arrayOf(
        intArrayOf(10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8),
        intArrayOf(13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1),
        intArrayOf(13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7),
        intArrayOf(1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12)
    ),
    arrayOf(
        intArrayOf(7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15),
        intArrayOf(13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9),
        intArrayOf(10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4),
        intArrayOf(3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14)
    ),
    arrayOf(
        intArrayOf(2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9),
        intArrayOf(14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6),
        intArrayOf(4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14),
        intArrayOf(11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3)
    ),
    arrayOf(
        intArrayOf(12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11),
        intArrayOf(10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8),
        intArrayOf(9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6),
        intArrayOf(4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13)
    ),
    arrayOf(
        intArrayOf(4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1),
        intArrayOf(13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6),
        intArrayOf(1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2),
        intArrayOf(6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12)
    ),
    arrayOf(
        intArrayOf(13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7),
        intArrayOf(1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2),
        intArrayOf(7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8),
        intArrayOf(2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11)
    )

)

val SHIFT_TABLE = intArrayOf(1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1)

val PC2 = intArrayOf(
    14, 17, 11, 24, 1, 5,
    3, 28, 15, 6, 21, 10,
    23, 19, 12, 4, 26, 8,
    16, 7, 27, 20, 13, 2,
    41, 52, 31, 37, 47, 55,
    30, 40, 51, 45, 33, 48,
    44, 49, 39, 56, 34, 53,
    46, 42, 50, 36, 29, 32
)

val PC1 = intArrayOf(
    57, 49, 41, 33, 25, 17, 9,
    1, 58, 50, 42, 34, 26, 18,
    10, 2, 59, 51, 43, 35, 27,
    19, 11, 3, 60, 52, 44, 36,
    63, 55, 47, 39, 31, 23, 15,
    7, 62, 54, 46, 38, 30, 22,
    14, 6, 61, 53, 45, 37, 29,
    21, 13, 5, 28, 20, 12, 4
)


val P = intArrayOf(
    16, 7, 20, 21,
    29, 12, 28, 17,
    1, 15, 23, 26,
    5, 18, 31, 10,
    2, 8, 24, 14,
    32, 27, 3, 9,
    19, 13, 30, 6,
    22, 11, 4, 25
)