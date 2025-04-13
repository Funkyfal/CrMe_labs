package mathUtils.isPrime;

import java.math.BigInteger;
import java.security.SecureRandom;

public class FermatTest {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static boolean fermatTest(BigInteger candidate, int rounds) {
        if (candidate.compareTo(BigInteger.valueOf(4)) < 0) {
            return true;
        }

        for (int i = 0; i < rounds; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, candidate.subtract(BigInteger.TWO));
            if (!a.gcd(candidate).equals(BigInteger.ONE)) {
                return false;
            }
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
