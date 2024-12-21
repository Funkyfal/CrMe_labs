import java.io.File
import java.io.FileInputStream
import java.util.BitSet

fun main(args: Array<String>) {
    if (args.size < 5) {
        println("Использование: <in.bin> <key.bin> <sync.bin> <encrypt/decrypt> <padding>")
        return
    }

    val inputFilePath = args[0]
    val keyFilePath = args[1]
    val syncFilePath = args[2] // Для режима ECB синхропосылка не используется
    val operation = args[3] // "encrypt" или "decrypt"
    val paddingMode = args[4] // "ANSI"

    val outputFilePath = "out.bin"

    // Считываем ключ из файла
    val key = File(keyFilePath).readBytes()
    if (key.size != 24) {
        println("Ключ должен быть длиной 24 байта (8 байт для каждого из 3 ключей).")
        return
    }

    val key1 = key.sliceArray(0 until 8)
    val key2 = key.sliceArray(8 until 16)
    val key3 = key.sliceArray(16 until 24)

    when (operation) {
        "encrypt" -> {
            val inputBytes = File(inputFilePath).readBytes()
            val paddedBytes = if (paddingMode == "ANSI") padWithANSIX923(inputBytes, 8) else inputBytes

            println("paddedbytes content in hex:")
            for (byte in paddedBytes) {
                print("%02X ".format(byte))
            }
            println()

            val encryptedBytes = tripleDesEncrypt(paddedBytes, key1, key2, key3)

            println("encrypted content in hex:")
            for (byte in encryptedBytes) {
                print("%02X ".format(byte))
            }
            println()

            File(outputFilePath).writeBytes(encryptedBytes)
            println("Файл успешно зашифрован и сохранен в $outputFilePath")
        }
        "decrypt" -> {
            val encryptedBytes = File(inputFilePath).readBytes()
            val decryptedBytes = tripleDesDecrypt(encryptedBytes, key1, key2, key3)
            val unpaddedBytes = if (paddingMode == "ANSI") removeANSIX923Padding(decryptedBytes) else decryptedBytes
            File(outputFilePath).writeBytes(unpaddedBytes)
            println("Файл успешно расшифрован и сохранен в $outputFilePath")
        }
        else -> println("Некорректная операция. Укажите 'encrypt' или 'decrypt'.")
    }
}

// Реализация 3DES шифрования
fun tripleDesEncrypt(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
    var result = desEncrypt(data, key1)
    result = desDecrypt(result, key2)
    return desEncrypt(result, key3)
}

// Реализация 3DES расшифрования
fun tripleDesDecrypt(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
    var result = desDecrypt(data, key3)
    result = desEncrypt(result, key2)
    return desDecrypt(result, key1)
}

fun desEncrypt(data: ByteArray, key: ByteArray): ByteArray {
    // Преобразуем данные и ключ в битовые массивы
    val dataBits = bytesToBits(data)
    val keyBits = bytesToBits(key)

    // Генерация 16 раундовых ключей
    val subKeys = generateSubKeys(keyBits)

    // Начальная перестановка
    val permutedData = permute(dataBits, IP)

    // Разделение на левую и правую половины
    var left = permutedData.copyOfRange(0, 32)
    var right = permutedData.copyOfRange(32, 64)

    // 16 раундов сети Фейстеля
    for (i in 0 until 16) {
        val temp = right
        right = xor(left, feistel(right, subKeys[i]))
        left = temp
    }

    // Объединяем правую и левую части в обратном порядке
    val preOutput = right + left

    // Обратная перестановка
    val outputBits = permute(preOutput, IP_INV)

    // Преобразуем биты обратно в байты
    return bitsToBytes(outputBits)
}


// Реализация DES расшифрования
fun desDecrypt(data: ByteArray, key: ByteArray): ByteArray {
    val dataBits = bytesToBits(data)
    val keyBits = bytesToBits(key)
    val subKeys = generateSubKeys(keyBits)

    // Перестановка IP
    val permutedData = permute(dataBits, IP)

    // Разделение данных
    var left = permutedData.copyOfRange(0, 32)
    var right = permutedData.copyOfRange(32, 64)

    // 16 раундов в обратном порядке
    for (i in 15 downTo 0) {
        val temp = right
        right = xor(left, feistel(right, subKeys[i]))
        left = temp
    }

    val preOutput = right + left
    val outputBits = permute(preOutput, IP_INV)

    return bitsToBytes(outputBits)
}


// Дополнение ANSI X.923
fun padWithANSIX923(data: ByteArray, blockSize: Int): ByteArray {
    val paddingSize = if (data.size % blockSize == 0) 1 else blockSize - (data.size % blockSize)
    return data + ByteArray(paddingSize - 1) { 0 } + paddingSize.toByte()
}


// Удаление дополнения ANSI X.923
fun removeANSIX923Padding(data: ByteArray): ByteArray {
    val paddingLength = data[data.size - 1].toInt()  // последний байт указывает количество добавленных байтов
    if (paddingLength <= 0 || paddingLength > 8) {
        throw IllegalArgumentException("Неверное дополнение")
    }
    return data.copyOfRange(0, data.size - paddingLength)  // удаление дополнения
}



fun permute(input: BooleanArray, table: IntArray): BooleanArray {
    val output = BooleanArray(table.size)
    for (i in table.indices) {
        output[i] = input[table[i] - 1] // Таблицы используют индексацию с 1
    }
    return output
}

fun xor(a: BooleanArray, b: BooleanArray): BooleanArray {
    return BooleanArray(a.size) { i -> a[i] xor b[i] }
}

//fun xorBlocks(block1: ByteArray, block2: ByteArray): ByteArray {
//    return ByteArray(block1.size) { i -> (block1[i].toInt() xor block2[i].toInt()).toByte() }
//}
//
//fun tripleDesEncryptCBC(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray, iv: ByteArray): ByteArray {
//    val blockSize = 8
//    val encrypted = mutableListOf<Byte>()
//    var previousBlock = iv
//
//    for (i in data.indices step blockSize) {
//        val block = data.copyOfRange(i, i + blockSize)
//        val xoredBlock = xorBlocks(block, previousBlock)
//        val encryptedBlock = tripleDesEncrypt(xoredBlock, key1, key2, key3)
//        encrypted.addAll(encryptedBlock.toList())
//        previousBlock = encryptedBlock
//    }
//    return encrypted.toByteArray()
//}

fun feistel(input: BooleanArray, subKey: BooleanArray): BooleanArray {
    // Расширение блока с 32 до 48 бит
    val expanded = permute(input, E)

    // XOR с подключом
    val xored = xor(expanded, subKey)

    // Преобразование с помощью S-блоков
    val sBoxOutput = sBoxSubstitution(xored)

    // Перестановка P
    return permute(sBoxOutput, P)
}

fun sBoxSubstitution(input: BooleanArray): BooleanArray {
    fun Boolean.toInt() = if (this) 1 else 0 // Преобразование Boolean в Int

    val output = BooleanArray(32)
    var index = 0
    for (i in 0 until 8) {
        val row = (input[i * 6].toInt() shl 1) or input[i * 6 + 5].toInt()
        val col = (input[i * 6 + 1].toInt() shl 3) or
                (input[i * 6 + 2].toInt() shl 2) or
                (input[i * 6 + 3].toInt() shl 1) or
                input[i * 6 + 4].toInt()
        val value = S_BOXES[i][row][col]
        for (j in 0 until 4) {
            output[index++] = ((value shr (3 - j)) and 1) != 0
        }
    }
    return output
}


fun generateSubKeys(keyBits: BooleanArray): Array<BooleanArray> {
    val subKeys = Array(16) { BooleanArray(48) }

    // Применяем перестановку PC1
    val permutedKey = permute(keyBits, PC1)

    // Разделяем ключ на две половины
    var left = permutedKey.copyOfRange(0, 28)
    var right = permutedKey.copyOfRange(28, 56)

    for (i in 0 until 16) {
        // Сдвиг
        left = leftShift(left, SHIFT_TABLE[i])
        right = leftShift(right, SHIFT_TABLE[i])

        // Объединяем и применяем PC2
        val combined = left + right
        subKeys[i] = permute(combined, PC2)
    }

    return subKeys
}

fun leftShift(bits: BooleanArray, shifts: Int): BooleanArray {
    val size = bits.size
    return BooleanArray(size) { i -> bits[(i + shifts) % size] }
}


fun bitsToBytes(bits: BooleanArray): ByteArray {
    val bytes = ByteArray(bits.size / 8)
    for (i in bytes.indices) {
        for (j in 0 until 8) {
            if (bits[i * 8 + j]) {
                bytes[i] = (bytes[i].toInt() or (1 shl (7 - j))).toByte()
            }
        }
    }
    return bytes
}


fun bytesToBits(data: ByteArray): BooleanArray {
    val bits = BooleanArray(data.size * 8)
    for (i in data.indices) {
        for (j in 0 until 8) {
            bits[i * 8 + j] = ((data[i].toInt() shr (7 - j)) and 1) == 1
        }
    }
    return bits
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