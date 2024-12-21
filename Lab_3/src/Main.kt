import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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
            val encryptedBytes = tripleDesEncrypt(paddedBytes, key1, key2, key3)
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
    val cipher = Cipher.getInstance("DES/ECB/NoPadding")
    // Первый этап: шифрование с key1
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key1, "DES"))
    var result = cipher.doFinal(data)

    // Второй этап: расшифрование с key2
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key2, "DES"))
    result = cipher.doFinal(result)

    // Третий этап: шифрование с key3
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key3, "DES"))
    return cipher.doFinal(result)
}

// Реализация 3DES расшифрования
fun tripleDesDecrypt(data: ByteArray, key1: ByteArray, key2: ByteArray, key3: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("DES/ECB/NoPadding")

    // Первый этап: расшифрование с key3
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key3, "DES"))
    var result = cipher.doFinal(data)

    // Второй этап: шифрование с key2
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key2, "DES"))
    result = cipher.doFinal(result)

    // Третий этап: расшифрование с key1
    cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key1, "DES"))
    return cipher.doFinal(result)
}

// Дополнение ANSI X.923
fun padWithANSIX923(data: ByteArray, blockSize: Int): ByteArray {
    val paddingSize = blockSize - (data.size % blockSize)
    return data + ByteArray(paddingSize - 1) { 0 } + paddingSize.toByte()
}

// Удаление дополнения ANSI X.923
fun removeANSIX923Padding(data: ByteArray): ByteArray {
    val paddingSize = data.last().toInt()
    return data.copyOf(data.size - paddingSize)
}
