import mathUtils.ExtendedEuclid;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {
        ExtendedEuclid.ExtendedEuclidResult result =
                ExtendedEuclid.extendedEuclid(BigInteger.valueOf(34), BigInteger.valueOf(78));
        System.out.println(result);
    }
}