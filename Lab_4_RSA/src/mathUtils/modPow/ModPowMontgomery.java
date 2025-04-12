package mathUtils.modPow;
import java.math.BigInteger;

public class ModPowMontgomery {
    private final BigInteger modulus;
    private final BigInteger r;
    private final BigInteger rInv;
    private final BigInteger nPrime;

    public ModPowMontgomery(BigInteger modulus) {
        this.modulus = modulus;
        int k = modulus.bitLength();
        this.r = BigInteger.ONE.shiftLeft(k);
        this.rInv = r.modInverse(modulus);
        this.nPrime = r.subtract(modulus.modInverse(r));
    }

    /**
     * Функция редукции Монтгомери.
     *
     * @param t число, которое нужно редуцировать.
     * @return число в редуцированной форме.
     */
    private BigInteger montReduce(BigInteger t) {
        BigInteger m = t.multiply(nPrime).mod(r);
        BigInteger tPrime = t.add(m.multiply(modulus)).divide(r);
        if (tPrime.compareTo(modulus) >= 0) {
            tPrime = tPrime.subtract(modulus);
        }
        return tPrime;
    }

    /**
     * Перемножение двух чисел в монтгомери-форме.
     *
     * @param a первое число в монтгомери-форме
     * @param b второе число в монтгомери-форме
     * @return произведение (a*b) в монтгомери-форме.
     */
    public BigInteger montgomeryMultiply(BigInteger a, BigInteger b) {
        BigInteger t = a.multiply(b);
        return montReduce(t);
    }

    /**
     * Перевод числа в монтгомери-форму.
     *
     * @param a исходное число
     * @return число в монтгомери-форме.
     */
    public BigInteger convertToMontgomery(BigInteger a) {
        return a.multiply(r).mod(modulus);
    }

    /**
     * Перевод числа из монтгомери-формы в стандартное представление.
     *
     * @param aMont число в монтгомери-форме
     * @return число в стандартном представлении.
     */
    public BigInteger convertFromMontgomery(BigInteger aMont) {
        return montReduce(aMont);
    }

    /**
     * Возведение в степень с использованием алгоритма Монтгомери.
     *
     * @param base     основание (стандартное представление)
     * @param exponent показатель степени
     * @return base^exponent mod modulus.
     */
    public BigInteger modPow(BigInteger base, BigInteger exponent) {
        BigInteger baseMont = convertToMontgomery(base);
        BigInteger resultMont = convertToMontgomery(BigInteger.ONE);

        while (exponent.compareTo(BigInteger.ZERO) > 0) {
            if (exponent.testBit(0)) {
                resultMont = montgomeryMultiply(resultMont, baseMont);
            }
            baseMont = montgomeryMultiply(baseMont, baseMont);
            exponent = exponent.shiftRight(1);
        }
        return convertFromMontgomery(resultMont);
    }
}