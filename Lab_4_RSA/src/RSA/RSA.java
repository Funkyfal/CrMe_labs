package RSA;

import RSA.Keys.CRTPrivateKey;
import RSA.Keys.KeyPair;
import RSA.Keys.PrivateKey;
import RSA.Keys.PublicKey;
import mathUtils.ExEuclid.ExtendedEuclid;
import mathUtils.GenPrime.GenPrimeGenerator;
import mathUtils.modPow.ModPowMontgomery;

import java.math.BigInteger;

import static mathUtils.modPow.ModPowSimple.modPowRightToLeft;

/**
 * Класс RSA реализует генерацию ключей, зашифрование и расшифрование.
 * Для генерации простых чисел используется GenPrimeGenerator (с тестами Ферма, Миллера–Рабина и Соловея–Штрассена),
 * а для вычисления обратного элемента – ExtendedEuclid.
 */
public class RSA {

    /**
     * Генерация пары ключей RSA.
     *
     * @param modulusBitLength требуемая битовая длина модуля n (например, 2048)
     * @param primeTestRounds  число раундов для тестов на простоту (например, 100)
     * @return пара ключей (публичный и приватный)
     */
    public static KeyPair generateKeyPairEuler(int modulusBitLength, int primeTestRounds) {
        // Разбиваем длину модуля примерно пополам для генерации простых чисел p и q.
        int primeBitLength = modulusBitLength / 2;

        // Генерация простых чисел p и q с использованием GenPrimeGenerator
        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q)); // Требуем, чтобы p и q были различны.

        BigInteger n = p.multiply(q);
        // Вычисляем функцию Эйлера: φ(n) = (p - 1) * (q - 1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        // Выбираем стандартную открытую экспоненту e
        BigInteger e = BigInteger.valueOf(65537);
        // Если gcd(e, φ(n)) ≠ 1, можно повторно генерировать p и q
        while (!e.gcd(phi).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        }

        // Вычисляем закрытую экспоненту d: d = e^(-1) mod φ(n)
        ExtendedEuclid.ExtendedEuclidResult result = ExtendedEuclid.extendedEuclid(e, phi);
        BigInteger d = result.x;
        // Если d отрицательно, приводим его к положительному значению
        if (d.compareTo(BigInteger.ZERO) < 0) {
            d = d.mod(phi);
        }

        PublicKey pub = new PublicKey(n, e);
        PrivateKey priv = new PrivateKey(n, d);

        return new KeyPair(pub, priv);
    }

    /**
     * Генерация пары ключей RSA с использованием функции Кармайкла λ(n).
     * Для двух простых чисел p и q λ(n) = lcm(p - 1, q - 1) = (p - 1)*(q - 1) / gcd(p - 1, q - 1).
     *
     * @param modulusBitLength требуемая битовая длина модуля n (например, 2048)
     * @param primeTestRounds  число раундов для тестов на простоту (например, 100)
     * @return пара ключей (публичный и приватный)
     */
    public static KeyPair generateKeyPairCarmichael(int modulusBitLength, int primeTestRounds) {
        int primeBitLength = modulusBitLength / 2;

        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q));

        BigInteger n = p.multiply(q);
        // Вычисляем λ(n) = lcm(p - 1, q - 1)
        BigInteger lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

        // Выбираем стандартную открытую экспоненту e (обычно 65537)
        BigInteger e = BigInteger.valueOf(65537);
        // Подбираем p и q так, чтобы gcd(e, λ(n)) = 1
        while (!e.gcd(lambda).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        }

        // Вычисляем d как обратное к e по модулю λ(n)
        // Здесь можно воспользоваться встроенным методом modInverse
        BigInteger d = e.modInverse(lambda);

        PublicKey pub = new PublicKey(n, e);
        PrivateKey priv = new PrivateKey(n, d);
        return new KeyPair(pub, priv);
    }

    /**
     * Вычисляет наименьшее общее кратное двух чисел: lcm(a, b) = (a * b) / gcd(a, b).
     *
     * @param a первое число
     * @param b второе число
     * @return lcm(a, b)
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b));
    }

    /**
     * Генерация пары ключей RSA с сохранением p и q для оптимизированного расшифрования (CRT).
     * Здесь в приватном ключе сохраняются дополнительные параметры: p, q, dP, dQ и qInv.
     *
     * @param modulusBitLength требуемая битовая длина модуля n (например, 2048)
     * @param primeTestRounds число раундов для тестов на простоту (например, 100)
     * @return пара ключей (публичный и CRT-приватный)
     */
    public static KeyPair generateKeyPairCRT(int modulusBitLength, int primeTestRounds) {
        int primeBitLength = modulusBitLength / 2;
        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q));
        BigInteger n = p.multiply(q);
        // Функция Эйлера: φ(n) = (p-1)*(q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        while (!e.gcd(phi).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        }
        // Вычисление d по модулю φ(n)
        ExtendedEuclid.ExtendedEuclidResult result = ExtendedEuclid.extendedEuclid(e, phi);
        BigInteger d = result.x;
        if (d.compareTo(BigInteger.ZERO) < 0) {
            d = d.mod(phi);
        }
        PublicKey pub = new PublicKey(n, e);
        CRTPrivateKey privCRT = new CRTPrivateKey(n, d, p, q);
        return new KeyPair(pub, privCRT);
    }

    /**
     * Алгоритм зашифрования RSA.
     *
     * @param message сообщение в виде числа m, где 0 ≤ m < n.
     * @param pub     публичный ключ RSA.
     * @return шифротекст c = m^e mod n.
     */
    public static BigInteger encrypt(BigInteger message, PublicKey pub) {
        return message.modPow(pub.e, pub.n);
    }

    public static BigInteger encryptWithMontgomery(BigInteger message, PublicKey pub) {
        ModPowMontgomery modPowMontgomery = new ModPowMontgomery(pub.n);
        return modPowMontgomery.modPow(message, pub.e);
    }

    public static BigInteger encryptWithModPowSimple(BigInteger message, PublicKey pub) {
        return modPowRightToLeft(message, pub.e, pub.n);
    }

    /**
     * Алгоритм расшифрования RSA.
     *
     * @param ciphertext шифротекст c.
     * @param priv       приватный ключ RSA.
     * @return восстановленное сообщение m = c^d mod n.
     */
    public static BigInteger decrypt(BigInteger ciphertext, PrivateKey priv) {
        return ciphertext.modPow(priv.d, priv.n);
    }

    public static BigInteger decryptWithMontgomery(BigInteger ciphertext, PrivateKey priv) {
        ModPowMontgomery modPowMontgomery = new ModPowMontgomery(priv.n);
        return modPowMontgomery.modPow(ciphertext, priv.d);
    }

    public static BigInteger decryptWithModPowSimple(BigInteger ciphertext, PrivateKey priv) {
        return modPowRightToLeft(ciphertext, priv.d, priv.n);
    }

    /**
     * Оптимизированное расшифрование RSA с использованием сохранённых p и q (CRT).
     *
     * @param ciphertext шифротекст c.
     * @param privCRT    приватный ключ с сохранёнными p и q.
     * @return восстановленное сообщение m = c^d mod n.
     */
    public static BigInteger decryptWithCRT(BigInteger ciphertext, CRTPrivateKey privCRT) {
        // Вычисляем m1 = c^(dP) mod p и m2 = c^(dQ) mod q.
        BigInteger m1 = ciphertext.modPow(privCRT.dP, privCRT.p);
        BigInteger m2 = ciphertext.modPow(privCRT.dQ, privCRT.q);
        // h = (qInv * (m1 - m2)) mod p (при необходимости корректируем отрицательный остаток)
        BigInteger h = (privCRT.qInv.multiply(m1.subtract(m2))).mod(privCRT.p);
        // Восстанавливаем m = m2 + h * q
        return m2.add(h.multiply(privCRT.q));
    }
}

