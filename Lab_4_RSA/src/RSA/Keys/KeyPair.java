package RSA.Keys;

public class KeyPair {
    public final PublicKey publicKey;
    public final PrivateKey privateKey;

    public KeyPair(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
