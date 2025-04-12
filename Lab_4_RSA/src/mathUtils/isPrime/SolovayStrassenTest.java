package mathUtils.isPrime;

import java.math.BigInteger;

import static mathUtils.isPrime.FermatTest.uniformRandom;

public class SolovayStrassenTest {
    /**
     * Тест Соловея–Штрассена для проверки вероятной простоты числа.
     * Для кандидата, если он простое, тогда для любого a, взаимно простого с кандидатом:
     * a^((candidate-1)/2) mod candidate должно быть эквивалентно символу Якоби (a/candidate) mod candidate.
     *
     * @param candidate число, которое проверяем
     * @param rounds количество раундов
     * @return true, если кандидат проходит тест во всех раундах, иначе false.
     */
    public static boolean solovayStrassenTest(BigInteger candidate, int rounds) {
        if (candidate.compareTo(BigInteger.valueOf(3)) <= 0) {
            return true;
        }
        BigInteger exponent = candidate.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        for (int i = 0; i < rounds; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, candidate.subtract(BigInteger.ONE));
            // Если gcd(a, candidate) != 1, то кандидат составное.
            if (!a.gcd(candidate).equals(BigInteger.ONE)) {
                return false;
            }
            BigInteger modExp = a.modPow(exponent, candidate);
            int jacobi = jacobi(a, candidate);
            // Приводим jacobi к значению в модульной арифметике: если jacobi == -1, то сравниваем с candidate-1.
            BigInteger jacobiMod = (jacobi == -1) ? candidate.subtract(BigInteger.ONE) : BigInteger.valueOf(jacobi);
            if (!modExp.equals(jacobiMod.mod(candidate))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Вычисляет символ Якоби (a/n) для a >= 0, где n – положительное нечётное число.
     *
     * @param a число a
     * @param n модуль n (нечётное, положительное)
     * @return символ Якоби, одно из значений -1, 0, 1.
     */
    public static int jacobi(BigInteger a, BigInteger n) {
        if (n.signum() <= 0 || !n.testBit(0)) {
            throw new IllegalArgumentException("n должно быть положительным нечётным числом.");
        }
        a = a.mod(n);
        int result = 1;
        while (a.compareTo(BigInteger.ZERO) != 0) {
            while (!a.testBit(0)) { // a четное
                a = a.shiftRight(1);
                BigInteger nMod8 = n.mod(BigInteger.valueOf(8));
                if (nMod8.equals(BigInteger.valueOf(3)) || nMod8.equals(BigInteger.valueOf(5))) {
                    result = -result;
                }
            }
            // Обмен a и n
            BigInteger temp = a;
            a = n;
            n = temp;
            if (a.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3)) &&
                    n.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
                result = -result;
            }
            a = a.mod(n);
        }
        return n.equals(BigInteger.ONE) ? result : 0;
    }
}
