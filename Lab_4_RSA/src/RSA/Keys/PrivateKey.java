package RSA.Keys;

import java.math.BigInteger;

public class PrivateKey {
    public final BigInteger n; // Модуль (тот же, что и у публичного ключа)
    public final BigInteger d; // Закрытая экспонента

    public PrivateKey(BigInteger n, BigInteger d) {
        this.n = n;
        this.d = d;
    }

    @Override
    public String toString() {
        return "PrivateKey [n=" + n + ", d=" + d + "]";
    }
}
