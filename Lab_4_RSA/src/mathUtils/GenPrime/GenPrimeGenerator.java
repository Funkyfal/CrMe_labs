package mathUtils.GenPrime;

import java.math.BigInteger;
import java.security.SecureRandom;

import static mathUtils.isPrime.FermatTest.fermatTest;
import static mathUtils.isPrime.MillerRabinTest.millerRabinTest;
import static mathUtils.isPrime.SolovayStrassenTest.solovayStrassenTest;

public class GenPrimeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

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
