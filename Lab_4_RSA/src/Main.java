import RSA.Keys.CRTPrivateKey;
import RSA.Keys.KeyPair;

import java.math.BigInteger;
import java.util.Scanner;

import static RSA.RSA.*;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Выберите способ генерации ключей:");
        System.out.println("1: С использованием функции Эйлера");
        System.out.println("2: С использованием функции Кармайкла");
        System.out.println("3: С использованием оптимизации CRT (с сохранением p и q)");
        int keyOption = scanner.nextInt();

        System.out.println("Введите количество раундов для тестов на простоту (например, 100):");
        int primeTestRounds = scanner.nextInt();

        System.out.println("Введите битовую длину модуля (например, 1024 или 2048):");
        int modulusBitLength = scanner.nextInt();
        // Чтобы считывание строки шло корректно, вычитываем оставшийся перевод строки.
        scanner.nextLine();

        System.out.println("Введите сообщение для шифрования:");
        String messageStr = scanner.nextLine();

        // Вариант exponentiationOption определяет метод возведения в степень:
        // для вариантов 1 и 2 – выбор между простым методом (1) и Монтгомери (2).
        // Для CRT-ключей также выбирается метод шифрования (расшифрование при CRT выполняется через decryptWithCRT).
        int expOption = 0;
        KeyPair keys = null;
        if (keyOption == 1) {
            System.out.println("Способ генерации ключей: функция Эйлера");
            System.out.println("Выберите метод возведения в степень:");
            System.out.println("1: Простой метод");
            System.out.println("2: Метод Монтгомери");
            expOption = scanner.nextInt();
            keys = generateKeyPairEuler(modulusBitLength, primeTestRounds);
        } else if (keyOption == 2) {
            System.out.println("Способ генерации ключей: функция Кармайкла");
            System.out.println("Выберите метод возведения в степень:");
            System.out.println("1: Простой метод");
            System.out.println("2: Метод Монтгомери");
            expOption = scanner.nextInt();
            keys = generateKeyPairCarmichael(modulusBitLength, primeTestRounds);
        } else if (keyOption == 3) {
            System.out.println("Способ генерации ключей: оптимизация CRT (с сохранением p и q)");
            System.out.println("Выберите метод возведения в степень для шифрования:");
            System.out.println("1: Простой метод");
            System.out.println("2: Метод Монтгомери");
            expOption = scanner.nextInt();
            keys = generateKeyPairCRT(modulusBitLength, primeTestRounds);
        } else {
            System.out.println("Некорректный выбор. Завершаем работу.");
            scanner.close();
            return;
        }

        // Вывод сгенерированных ключей.
        System.out.println("\nПубличный ключ:");
        System.out.println(keys.publicKey);
        System.out.println("\nПриватный ключ:");
        System.out.println(keys.privateKey);

        // Преобразуем сообщение в число.
        BigInteger message = new BigInteger(messageStr.getBytes());
        System.out.println("\nИсходное сообщение: " + messageStr);

        // Шифрование. Для первых двух вариантов выбор метода зависит от expOption.
        BigInteger ciphertext;
        if (keyOption == 1 || keyOption == 2) {
            if (expOption == 1) {
                ciphertext = encryptWithModPowSimple(message, keys.publicKey);
            } else {
                ciphertext = encryptWithMontgomery(message, keys.publicKey);
            }
        } else { // для CRT ключей – выбор метода шифрования тоже делается
            if (expOption == 1) {
                ciphertext = encryptWithModPowSimple(message, keys.publicKey);
            } else {
                ciphertext = encryptWithMontgomery(message, keys.publicKey);
            }
        }
        System.out.println("Зашифрованное сообщение: " + ciphertext);

        // Расшифрование.
        BigInteger decrypted;
        if (keyOption == 3) {
            // При использовании CRT оптимизации, расшифрование всегда через decryptWithCRT.
            CRTPrivateKey privCRT = (CRTPrivateKey) keys.privateKey;  // Приведение к CRT-версии.
            decrypted = decryptWithCRT(ciphertext, privCRT);
        } else {
            if (expOption == 1) {
                decrypted = decryptWithModPowSimple(ciphertext, keys.privateKey);
            } else {
                decrypted = decryptWithMontgomery(ciphertext, keys.privateKey);
            }
        }
        String decryptedStr = new String(decrypted.toByteArray());
        System.out.println("Расшифрованное сообщение: " + decryptedStr);

        if (messageStr.equals(decryptedStr)) {
            System.out.println("Шифрование/расшифрование прошло успешно!");
        } else {
            System.out.println("Ошибка шифрования/расшифрования!");
        }
        scanner.close();
    }
}