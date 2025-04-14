import RSA.Keys.CRTPrivateKey;
import RSA.Keys.KeyPair;
import RSA.Keys.PrivateKey;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static RSA.RSA.*;

public class Main {

    private static final Path MESSAGE_FILE = Path.of("Lab_4_RSA/src/message.txt");
    private static final Path CRYPT_FILE = Path.of("Lab_4_RSA/src/crypt.txt");
    private static final Path DECRYPT_FILE = Path.of("Lab_4_RSA/src/decrypt.txt");

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Введите номер операции:\n1 - Шифрование\n2 - Расшифрование");
            int operation = scanner.nextInt();
            scanner.nextLine();

            if (operation == 1) {
                String inputMessage = Files.readString(MESSAGE_FILE, StandardCharsets.UTF_8).trim();
                if (inputMessage.isEmpty()) {
                    System.out.println("Файл message.txt пустой. Завершаем работу.");
                    return;
                }
                System.out.println("Исходное сообщение (считанное из message.txt):");
                System.out.println(inputMessage);

                System.out.println("Выберите способ генерации ключей:");
                System.out.println("1: С использованием функции Эйлера");
                System.out.println("2: С использованием функции Кармайкла");
                System.out.println("3: С использованием оптимизации CRT (с сохранением p и q)");
                int keyOption = scanner.nextInt();

                System.out.println("Введите количество раундов для тестов на простоту (например, 100):");
                int primeTestRounds = scanner.nextInt();
                System.out.println("Введите битовую длину модуля (например, 1024 или 2048):");
                int modulusBitLength = scanner.nextInt();
                scanner.nextLine();

                int expOption;
                KeyPair keys;
                if (keyOption == 1) {
                    System.out.println("Способ генерации ключей: функция Эйлера");
                    System.out.println("Выберите метод возведения в степень:");
                    System.out.println("1: Простой метод");
                    System.out.println("2: Метод Монтгомери");
                    expOption = scanner.nextInt();
                    scanner.nextLine();
                    keys = generateKeyPairEuler(modulusBitLength, primeTestRounds);
                } else if (keyOption == 2) {
                    System.out.println("Способ генерации ключей: функция Кармайкла");
                    System.out.println("Выберите метод возведения в степень:");
                    System.out.println("1: Простой метод");
                    System.out.println("2: Метод Монтгомери");
                    expOption = scanner.nextInt();
                    scanner.nextLine();
                    keys = generateKeyPairCarmichael(modulusBitLength, primeTestRounds);
                } else if (keyOption == 3) {
                    System.out.println("Способ генерации ключей: оптимизация CRT (с сохранением p и q)");
                    System.out.println("Выберите метод возведения в степень для шифрования:");
                    System.out.println("1: Простой метод");
                    System.out.println("2: Метод Монтгомери");
                    expOption = scanner.nextInt();
                    scanner.nextLine();
                    keys = generateKeyPairCRT(modulusBitLength, primeTestRounds);
                } else {
                    System.out.println("Некорректный выбор. Завершаем работу.");
                    return;
                }

                System.out.println("\nПубличный ключ:");
                System.out.println(keys.publicKey);
                System.out.println("\nПриватный ключ:");
                System.out.println(keys.privateKey);

                StringBuilder keyData = new StringBuilder();
                keyData.append(keys.publicKey.n).append(System.lineSeparator());
                keyData.append(keys.publicKey.e).append(System.lineSeparator());
                keyData.append(keys.privateKey.d).append(System.lineSeparator());
                if (keyOption == 3) {
                    CRTPrivateKey crtKey = (CRTPrivateKey) keys.privateKey;
                    keyData.append(crtKey.p).append(System.lineSeparator());
                    keyData.append(crtKey.q).append(System.lineSeparator());
                    keyData.append(crtKey.dP).append(System.lineSeparator());
                    keyData.append(crtKey.dQ).append(System.lineSeparator());
                    keyData.append(crtKey.qInv).append(System.lineSeparator());
                }
                Files.writeString(CRYPT_FILE, keyData.toString(), StandardCharsets.UTF_8);
                System.out.println("Ключевая информация сохранена в файл crypt.txt");

                BigInteger message = new BigInteger(inputMessage.getBytes(StandardCharsets.UTF_8));

                BigInteger ciphertext;
                if (keyOption == 1 || keyOption == 2) {
                    if (expOption == 1) {
                        ciphertext = encryptWithModPowSimple(message, keys.publicKey);
                    } else {
                        ciphertext = encryptWithMontgomery(message, keys.publicKey);
                    }
                } else {
                    if (expOption == 1) {
                        ciphertext = encryptWithModPowSimple(message, keys.publicKey);
                    } else {
                        ciphertext = encryptWithMontgomery(message, keys.publicKey);
                    }
                }
                String cipherStr = ciphertext.toString();
                Files.writeString(MESSAGE_FILE, cipherStr, StandardCharsets.UTF_8);
                System.out.println("Зашифрованное сообщение (BigInteger в виде строки):");
                System.out.println(cipherStr);
            } else if (operation == 2) {
                String cipherStr = Files.readString(MESSAGE_FILE, StandardCharsets.UTF_8).trim();
                if (cipherStr.isEmpty()) {
                    System.out.println("Файл message.txt пустой. Завершаем работу.");
                    return;
                }
                BigInteger ciphertext = new BigInteger(cipherStr);
                List<String> keyLines = Files.readAllLines(CRYPT_FILE, StandardCharsets.UTF_8);
                if (keyLines.size() < 3) {
                    System.out.println("В файле crypt.txt недостаточно данных для расшифрования.");
                    return;
                }
                BigInteger n = new BigInteger(keyLines.get(0).trim());
                BigInteger e = new BigInteger(keyLines.get(1).trim());
                BigInteger d = new BigInteger(keyLines.get(2).trim());
                PrivateKey privateKey;
                if (keyLines.size() >= 8) {
                    BigInteger p = new BigInteger(keyLines.get(3).trim());
                    BigInteger q = new BigInteger(keyLines.get(4).trim());
                    privateKey = new CRTPrivateKey(n, d, p, q);
                } else {
                    privateKey = new RSA.Keys.PrivateKey(n, d);
                }
                System.out.println("Выберите метод возведения в степень для расшифрования:");
                System.out.println("1: Простой метод");
                System.out.println("2: Метод Монтгомери");
                int expOption = scanner.nextInt();
                BigInteger decrypted;
                if (privateKey instanceof CRTPrivateKey) {
                    decrypted = decryptWithCRT(ciphertext, (CRTPrivateKey) privateKey);
                } else {
                    if (expOption == 1) {
                        decrypted = decryptWithModPowSimple(ciphertext, privateKey);
                    } else {
                        decrypted = decryptWithMontgomery(ciphertext, privateKey);
                    }
                }
                String decryptedStr = new String(decrypted.toByteArray(), StandardCharsets.UTF_8);
                System.out.println("Расшифрованное сообщение:");
                System.out.println(decryptedStr);
                Files.writeString(DECRYPT_FILE, decryptedStr, StandardCharsets.UTF_8);
                System.out.println("Результат расшифрования сохранён в decrypt.txt");
            } else {
                System.out.println("Некорректный выбор операции.");
            }
        } catch (Exception ex) {
            System.out.println("Ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
