package mathUtils.isPrime;

import java.math.BigInteger;

import static mathUtils.isPrime.FermatTest.uniformRandom;

public class MillerRabinTest {
    /**
     * Тест Миллера–Рабина для проверки вероятной простоты числа.
     *
     * @param candidate число, которое проверяем
     * @param rounds количество раундов
     * @return true, если candidate проходит все раунды теста Миллера–Рабина, иначе false.
     */
    public static boolean millerRabinTest(BigInteger candidate, int rounds) {
        // Если candidate <= 3, можно обработать отдельно.
        if (candidate.compareTo(BigInteger.valueOf(3)) <= 0) {
            return true;
        }
        // Разложение candidate-1 в виде 2^s * d, где d нечетно.
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
