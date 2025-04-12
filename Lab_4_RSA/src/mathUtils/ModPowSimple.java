package mathUtils;

import java.math.BigInteger;

public class ModPowSimple {
    public static BigInteger modPowRightToLeft(
            BigInteger base,
            BigInteger exponent,
            BigInteger modulus) {
        BigInteger result = BigInteger.ONE;
        base = base.mod(modulus);
        while (exponent.compareTo(BigInteger.ZERO) > 0) {
            if (exponent.testBit(0)){
                result = result.multiply(base).mod(modulus);
            }
            exponent = exponent.shiftRight(1);
            base = base.multiply(base).mod(modulus);
        }

        return result;
    }
}
