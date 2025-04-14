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

    private BigInteger montReduce(BigInteger t) {
        BigInteger m = t.multiply(nPrime).mod(r);
        BigInteger tPrime = t.add(m.multiply(modulus)).divide(r);
        if (tPrime.compareTo(modulus) >= 0) {
            tPrime = tPrime.subtract(modulus);
        }
        return tPrime;
    }

    public BigInteger montgomeryMultiply(BigInteger a, BigInteger b) {
        BigInteger t = a.multiply(b);
        return montReduce(t);
    }

    public BigInteger convertToMontgomery(BigInteger a) {
        return a.multiply(r).mod(modulus);
    }

    public BigInteger convertFromMontgomery(BigInteger aMont) {
        return montReduce(aMont);
    }

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