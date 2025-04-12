package mathUtils.GenPrime;

import java.math.BigInteger;
import java.security.SecureRandom;

import static mathUtils.isPrime.FermatTest.fermatTest;
import static mathUtils.isPrime.MillerRabinTest.millerRabinTest;
import static mathUtils.isPrime.SolovayStrassenTest.solovayStrassenTest;

/**
 * Класс для генерации кандидатного простого числа заданной битовой длины,
 * используя исключительно тест Ферма.
 * Алгоритм:
 * 1. Генерируется случайное число заданной битовой длины с выставлением старшего бита
 *    (для обеспечения нужного размера) и младшего (для нечётности).
 * 2. Проводится тест Ферма с заданным числом раундов.
 * 3. Если кандидат не проходит тест Ферма, генерируется новое число.
 */
public class GenPrimeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Генерирует кандидат простого числа заданной битовой длины, которое проходит тест Ферма.
     *
     * @param bitLength число бит кандидата (например, 2048)
     * @param rounds число раундов для теста Ферма (например, 100)
     * @return кандидат простого числа
     */
    public static BigInteger generateCandidate(int bitLength, int rounds) {
        BigInteger candidate = new BigInteger(bitLength, RANDOM)
                .setBit(bitLength - 1) // гарантирует нужную битовую длину
                .setBit(0);            // гарантирует нечётность

        // Пока кандидат не проходит все тесты, генерируем новое число.
        while (!(fermatTest(candidate, rounds)
                && millerRabinTest(candidate, rounds)
                && solovayStrassenTest(candidate, rounds))) {
            candidate = new BigInteger(bitLength, RANDOM)
                    .setBit(bitLength - 1)
                    .setBit(0);
        }

        return candidate;
    }
}
