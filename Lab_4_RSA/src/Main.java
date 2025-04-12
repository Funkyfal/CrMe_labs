import mathUtils.ExtendedEuclid;
import mathUtils.ModPowSimple;

import java.math.BigInteger;

public class Main {

    public static void main(String[] args) {
        System.out.println(ExtendedEuclid
                .extendedEuclid(BigInteger.valueOf(34), BigInteger.valueOf(78)));

        System.out.println(
                ModPowSimple
                        .modPowRightToLeft
                                (BigInteger.valueOf(4)
                                        , BigInteger.valueOf(121)
                                        , BigInteger.valueOf(47)));
    }
}