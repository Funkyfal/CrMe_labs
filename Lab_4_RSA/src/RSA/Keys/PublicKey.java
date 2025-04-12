package RSA.Keys;

import java.math.BigInteger;

public class PublicKey {
    public final BigInteger n; // Модуль
    public final BigInteger e; // Открытая экспонента

    public PublicKey(BigInteger n, BigInteger e) {
        this.n = n;
        this.e = e;
    }

    @Override
    public String toString() {
        return "PublicKey [n=" + n + ", e=" + e + "]";
    }
}
