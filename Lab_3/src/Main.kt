import java.io.File
import java.util.BitSet

fun main() {
    println("Введите режим работы (1 для шифрования, 2 для дешифрования):")
    val mode = readLine()?.toIntOrNull() ?: error("Некорректный ввод")

    val inputFileName = "in.bin"
    val keyFileName = "key.bin"
    val outputFileName = "out.bin"

    // Загрузка данных из файлов
    val inputFile = File(inputFileName)
    val keyFile = File(keyFileName)

    val inputBlocks = inputFile.readBytes().toList().chunked(8).map { BitSet.valueOf(it.toByteArray()) }
    val keyBytes = keyFile.readBytes()

    if (keyBytes.size != 24) error("Ключ должен содержать 24 байта!")

    // Разделение ключа на три 8-байтных ключа
    val keys = listOf(
        BitSet.valueOf(keyBytes.sliceArray(0..7)),
        BitSet.valueOf(keyBytes.sliceArray(8..15)),
        BitSet.valueOf(keyBytes.sliceArray(16..23))
    )

    // Генерация подключей для каждого ключа
    val subkeys = keys.map { key ->
        mutableListOf<BitSet>().apply { generateKeys(key, this) }
    }

    // Обработка блоков данных по схеме 3DES (EDE или DED)
    val processedBlocks = when (mode) {
        1 -> { // Шифрование
            println("Шифрование...")
            inputBlocks.map { block ->
                tripleDesEncrypt(block, subkeys, encrypt = true)
            }
        }
        2 -> { // Дешифрование
            println("Дешифрование...")
            inputBlocks.map { block ->
                tripleDesEncrypt(block, subkeys, encrypt = false)
            }
        }
        else -> error("Неверный режим работы")
    }

    // Запись результата в файл
    val outputFile = File(outputFileName)
    outputFile.writeBytes(processedBlocks.flatMap { it.toByteArray().toList() }.toByteArray())

    println("Обработка завершена. Результат сохранён в $outputFileName")
}

// Реализация 3DES по схеме EDE или DED
fun tripleDesEncrypt(block: BitSet, subkeys: List<List<BitSet>>, encrypt: Boolean): BitSet {
    return if (encrypt) {
        // Шифрование: E -> D -> E
        val encrypted = desEncryptBlock(block, subkeys[0], decrypt = false)
        val decrypted = desEncryptBlock(encrypted, subkeys[1], decrypt = true)
        desEncryptBlock(decrypted, subkeys[2], decrypt = false)
    } else {
        // Дешифрование: D -> E -> D
        val decrypted = desEncryptBlock(block, subkeys[2], decrypt = true)
        val encrypted = desEncryptBlock(decrypted, subkeys[1], decrypt = false)
        desEncryptBlock(encrypted, subkeys[0], decrypt = true)
    }
}

// Функция для перестановки
fun permute(input: BitSet, output: BitSet, table: IntArray, n: Int) {
    for (i in 0 until n) {
        output.set(n - i - 1, input.get(input.size() - table[i]))
    }
}

// Генерация подключей
fun generateKeys(key: BitSet, subkeys: MutableList<BitSet>) {
    val key56 = BitSet(56)
    permute(key, key56, PC1, 56)

    val left = BitSet(28)
    val right = BitSet(28)

    for (i in 0 until 28) {
        left.set(i, key56[i])
        right.set(i, key56[28 + i])
    }

    for (round in 0 until 16) {
        // Сдвиг влево
        val shiftCount = SHIFT_TABLE[round]
        left.leftShift(shiftCount)
        right.leftShift(shiftCount)

        // Перестановка PC2 для получения подключа
        val combined = BitSet(56)
        for (i in 0 until 28) {
            combined.set(i, left[i])
            combined.set(28 + i, right[i])
        }
        val subkey = BitSet(48)
        permute(combined, subkey, PC2, 48)

        subkeys.add(subkey)
    }
}

// Сдвиг бит влево
fun BitSet.leftShift(shiftCount: Int) {
    for (i in (size() - 1 downTo shiftCount)) {
        set(i, get(i - shiftCount))
    }
    for (i in 0 until shiftCount) {
        set(i, false)
    }
}

// Функция DES-шифрования одного блока
fun desEncryptBlock(block: BitSet, subkeys: List<BitSet>, decrypt: Boolean = false): BitSet {
    val permutedBlock = BitSet(64)
    permute(block, permutedBlock, IP, 64)

    val L = BitSet(32)
    val R = BitSet(32)
    for (i in 0 until 32) {
        L.set(31 - i, permutedBlock[63 - i])
        R.set(31 - i, permutedBlock[31 - i])
    }

    for (round in 0 until 16) {
        val temp = R.clone() as BitSet
        val subkeyIndex = if (decrypt) 15 - round else round
        R.xor(fFunction(R, subkeys[subkeyIndex]))
        L.xor(temp)
    }

    val combined = BitSet(64)
    for (i in 0 until 32) {
        combined.set(63 - i, R[31 - i])
        combined.set(31 - i, L[31 - i])
    }

    val output = BitSet(64)
    permute(combined, output, IP_INV, 64)
    return output
}
fun fFunction(R: BitSet, subkey: BitSet): BitSet {
    val expandedR = BitSet(48)
    permute(R, expandedR, E, 48)

    expandedR.xor(subkey)

    val output = BitSet(32)
    for (i in 0 until 8) {
        val row = (expandedR[47 - 6 * i].toInt() shl 1) or expandedR[47 - 6 * i - 5].toInt()
        val col = (expandedR[47 - 6 * i - 1].toInt() shl 3) or
                (expandedR[47 - 6 * i - 2].toInt() shl 2) or
                (expandedR[47 - 6 * i - 3].toInt() shl 1) or
                expandedR[47 - 6 * i - 4].toInt()
        val value = S_BOXES[i][row][col]

        for (j in 0 until 4) {
            output.set(31 - 4 * i - j, (value shr j) and 1 == 1)
        }
    }

    val permutedOutput = BitSet(32)
    permute(output, permutedOutput, P, 32)
    return permutedOutput
}
private fun Boolean.toInt(): Int {
    return if(this) 1 else 0
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