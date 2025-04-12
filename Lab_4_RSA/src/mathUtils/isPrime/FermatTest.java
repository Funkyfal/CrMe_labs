package mathUtils.isPrime;

import java.math.BigInteger;
import java.security.SecureRandom;

public class FermatTest {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static boolean fermatTest(BigInteger candidate, int rounds) {
        // Для очень малых кандидатов тест не имеет смысла.
        if (candidate.compareTo(BigInteger.valueOf(4)) < 0) {
            return true;
        }

        for (int i = 0; i < rounds; i++) {
            // Выбираем случайное основание a в диапазоне [2, candidate - 2]
            BigInteger a = uniformRandom(BigInteger.TWO, candidate.subtract(BigInteger.TWO));
            // Если gcd(a, candidate) != 1, то candidate составное.
            if (!a.gcd(candidate).equals(BigInteger.ONE)) {
                return false;
            }
            // Если для данного основания условие Ферма не выполняется, число составное.
            if (!a.modPow(candidate.subtract(BigInteger.ONE), candidate).equals(BigInteger.ONE)) {
                return false;
            }
        }
        return true;
    }

    static BigInteger uniformRandom(BigInteger lower, BigInteger upper) {
        BigInteger range = upper.subtract(lower).add(BigInteger.ONE);
        int bitLength = range.bitLength();
        BigInteger result;
        do {
            result = new BigInteger(bitLength, RANDOM);
        } while (result.compareTo(range) >= 0);
        return result.add(lower);
    }
}
