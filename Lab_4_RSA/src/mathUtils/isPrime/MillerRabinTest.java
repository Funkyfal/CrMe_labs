package mathUtils.isPrime;

import java.math.BigInteger;

import static mathUtils.isPrime.FermatTest.uniformRandom;

public class MillerRabinTest {

    public static boolean millerRabinTest(BigInteger candidate, int rounds) {
        if (candidate.compareTo(BigInteger.valueOf(3)) <= 0) {
            return true;
        }
        BigInteger d = candidate.subtract(BigInteger.ONE);
        int s = 0;
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            s++;
        }

        for (int i = 0; i < rounds; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, candidate.subtract(BigInteger.TWO));
            BigInteger x = a.modPow(d, candidate);
            if (x.equals(BigInteger.ONE) || x.equals(candidate.subtract(BigInteger.ONE))) {
                continue;
            }
            boolean passed = false;
            for (int r = 0; r < s - 1; r++) {
                x = x.modPow(BigInteger.TWO, candidate);
                if (x.equals(candidate.subtract(BigInteger.ONE))) {
                    passed = true;
                    break;
                }
            }
            if (!passed) {
                return false;
            }
        }
        return true;
    }
}
