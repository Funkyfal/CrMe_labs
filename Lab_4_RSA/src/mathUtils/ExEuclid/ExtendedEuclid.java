package mathUtils.ExEuclid;

import java.math.BigInteger;

public class ExtendedEuclid {

    public static class ExtendedEuclidResult {
        public final BigInteger x;
        private final BigInteger y;
        private final BigInteger gcd;

        public ExtendedEuclidResult(BigInteger gcd, BigInteger x, BigInteger y) {
            this.gcd = gcd;
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString(){
            return "x = " + x + "\ny = " + y + "\ngcd = " + gcd;
        }
    }

    public static ExtendedEuclidResult extendedEuclid(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return new ExtendedEuclidResult(a, BigInteger.ONE, BigInteger.ZERO);
        } else {
            ExtendedEuclidResult result = extendedEuclid(b, a.mod(b));
            //b * x' + (a − ⌊a/b⌋ * b) * y' = a * y' + b * (x' − ⌊a/b⌋ * y')
            BigInteger x = result.y;
            BigInteger y = result.x.subtract(a.divide(b).multiply(result.y));
            return new ExtendedEuclidResult(result.gcd, x, y);
        }
    }
}
