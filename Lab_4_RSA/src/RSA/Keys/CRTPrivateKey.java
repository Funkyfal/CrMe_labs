package RSA.Keys;

import java.math.BigInteger;

public class CRTPrivateKey extends PrivateKey {
    public final BigInteger p;
    public final BigInteger q;
    public final BigInteger dP;
    public final BigInteger dQ;
    public final BigInteger qInv;

    public CRTPrivateKey(BigInteger n, BigInteger d, BigInteger p, BigInteger q) {
        super(n, d);
        this.p = p;
        this.q = q;
        // Вычисление dP = d mod (p-1) и dQ = d mod (q-1)
        this.dP = d.mod(p.subtract(BigInteger.ONE));
        this.dQ = d.mod(q.subtract(BigInteger.ONE));
        // Вычисление обратного числа: qInv = q^(-1) mod p
        this.qInv = q.modInverse(p);
    }

    @Override
    public String toString() {
        return "CRTPrivateKey [n=" + n +
                ", d=" + d +
                ", p=" + p +
                ", q=" + q +
                ", dP=" + dP +
                ", dQ=" + dQ +
                ", qInv=" + qInv + "]";
    }
}
