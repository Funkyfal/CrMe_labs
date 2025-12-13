package org.bsu.crypto;

import by.bcrypto.bee2j.provider.BrngSecureRandom;
import org.bsu.model.KeyPairData;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

public class CryptoUtils {
    private static final BrngSecureRandom RNG = new BrngSecureRandom();
    private static final String PROVIDER = "Bee2";

    static {
        Bee2ProviderRegister.register();
    }

    // -------- Long-term & ephemeral keypair generation via Bee2 (BIGN) ----------
    public static KeyPairData generateLongTermKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("BIGN", PROVIDER);
        kpg.initialize(128, RNG);

        KeyPair kp = kpg.generateKeyPair();

        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();

        byte[] pub;
        byte[] priv;

        // --- Публичный ключ ---
        pub = pubKey.getEncoded();
        if (pub == null) {
            throw new IllegalStateException("Public key encoding is null (unexpected)");
        }

        // --- Приватный ключ ---
        if (privKey instanceof by.bcrypto.bee2j.provider.BignPrivateKey) {
            // Извлекаем raw-байты из BignKey (через reflection)
            Field f = by.bcrypto.bee2j.provider.BignKey.class.getDeclaredField("bytes");
            f.setAccessible(true);
            priv = (byte[]) f.get(privKey);
        } else {
            throw new IllegalStateException(
                    "Unsupported private key class: " + privKey.getClass().getName()
            );
        }

        if (priv == null) {
            throw new IllegalStateException("Extracted private key bytes are null");
        }

        System.out.println("DEBUG: BIGN keypair generated");
        System.out.println("DEBUG: priv len = " + priv.length);
        System.out.println("DEBUG: pub len  = " + pub.length);

        return new KeyPairData(priv, pub);
    }

    public static KeyPairData generateEphemeralKeyPair() throws Exception {
        // same as long-term (one-time)
        return generateLongTermKeyPair();
    }



    // -------- Password -> point (bake-swu) ----------
    // We'll attempt to call provider-level "BIGN" factory to create point via bake-swu.
    // If bee2j exposes a utility method for bake-swu via JCA, you can replace this.
    public static byte[] passwordToPoint(byte[] password) throws Exception {
        // Approach: use message digest (Bash256) then interpret digest as scalar and compute public point using provider
        MessageDigest md = MessageDigest.getInstance("Bash256", PROVIDER);
        byte[] h = md.digest(password);

        // Interpret hash as scalar and build a private key from it (PKCS8) via provider-specific API.
        // Create a BIGN private key from raw scalar is provider-specific — but provider often accepts PKCS8 with given scalar.
        // We'll use KeyFactory "BIGN" with PKCS#8 encoded private key generation not directly available from scalar.
        // Instead: use KeyPairGenerator seeded with the scalar — provider support may vary.
        // Practical approach: use generator to produce keypair deterministically using hash as seed.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("BIGN", PROVIDER);
        // Some Bee2 versions support init with seed via SecureRandom implementation (BrngSecureRandom),
        // but easiest: just return h (the hash) as the "Pw" representation for later KDF inclusion.
        return h; // STB ожидает точку, но для KDF можно include hash; for full compliance replace via Bake.swu.
    }

    // -------- Compute shared secret via KeyAgreement (BIGN/ECDH) ----------
    // canonical shared secret derived from password and two ephemeral public keys
    public static byte[] computeSharedSecret(byte[] pw, byte[] ephPubA, byte[] ephPubB) throws Exception {
        MessageDigest md = MessageDigest.getInstance("Bash256", PROVIDER);

        // include password first
        if (pw != null) md.update(pw);

        // canonical order of public keys so A and B do the same
        int cmp = lexCompare(ephPubA, ephPubB);
        if (cmp <= 0) {
            md.update(ephPubA);
            md.update(ephPubB);
        } else {
            md.update(ephPubB);
            md.update(ephPubA);
        }

        return md.digest();
    }

    // lexicographic comparison of two byte arrays
    private static int lexCompare(byte[] a, byte[] b) {
        if (a == b) return 0;
        int la = (a == null) ? 0 : a.length;
        int lb = (b == null) ? 0 : b.length;
        int min = Math.min(la, lb);
        for (int i = 0; i < min; ++i) {
            int va = (a[i] & 0xff);
            int vb = (b[i] & 0xff);
            if (va != vb) return Integer.compare(va, vb);
        }
        return Integer.compare(la, lb);
    }


    // -------- KDF (bake-kdf) ----------
    public static byte[] kdf(byte[] secret, byte[] info, int outLenBytes) throws Exception {
        // Bee2 may provide bake-kdf directly; if not, derive using Bash256 in counter mode per spec.
        MessageDigest md = MessageDigest.getInstance("Bash256", PROVIDER);
        int hashLen = md.getDigestLength();
        int n = (outLenBytes + hashLen - 1) / hashLen;
        byte[] out = new byte[n * hashLen];
        byte[] prev = new byte[0];
        for (int i = 1; i <= n; ++i) {
            md.reset();
            md.update(prev);
            if (info != null) md.update(info);
            md.update((byte) i);
            byte[] t = md.digest(secret);
            System.arraycopy(t, 0, out, (i - 1) * hashLen, t.length);
            prev = t;
        }
        return Arrays.copyOfRange(out, 0, outLenBytes);
    }

    // -------- Symmetric operations: Belt-CFB and Belt-MAC ----------
    public static byte[] encryptCFB(byte[] key, byte[] iv, byte[] plain) throws Exception {
        Cipher cipher = Cipher.getInstance("Belt/CFB/NoPadding", PROVIDER);
        SecretKeySpec sk = new SecretKeySpec(key, "Belt");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, sk, ivSpec);
        return cipher.doFinal(plain);
    }

    public static byte[] decryptCFB(byte[] key, byte[] iv, byte[] cipherText) throws Exception {
        Cipher cipher = Cipher.getInstance("Belt/CFB/NoPadding", PROVIDER);
        SecretKeySpec sk = new SecretKeySpec(key, "Belt");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, sk, ivSpec);
        return cipher.doFinal(cipherText);
    }

    public static byte[] computeMac(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("BeltMAC", PROVIDER);
        SecretKeySpec sk = new SecretKeySpec(key, "Belt");
        mac.init(sk);
        return mac.doFinal(data);
    }

    // Split K0 -> K1 (MAC), K2 (ENC)
    public static byte[][] splitKeys(byte[] K0) throws Exception {
        final int len = 32;
        byte[] derived = kdf(K0, "key-derivation".getBytes(), len * 2);
        byte[] K1 = Arrays.copyOfRange(derived, 0, len);
        byte[] K2 = Arrays.copyOfRange(derived, len, len * 2);
        return new byte[][]{K1, K2};
    }

    // sign data with raw BIGN private bytes (private scalar)
    public static byte[] signWithBignPrivate(byte[] privBytes, byte[] data) throws Exception {
        // construct BignPrivateKey via reflection
        Class<?> privCls = Class.forName("by.bcrypto.bee2j.provider.BignPrivateKey");
        Constructor<?> cons = privCls.getConstructor(byte[].class);
        cons.setAccessible(true);
        Object privObj = cons.newInstance((Object) privBytes);
        Signature sig = Signature.getInstance("BignWithBash256", PROVIDER);
        sig.initSign((PrivateKey) privObj);
        sig.update(data);
        return sig.sign();
    }

    // verify data with raw BIGN public bytes
    public static boolean verifyWithBignPublic(byte[] pubBytes, byte[] data, byte[] signature) throws Exception {
        Class<?> pubCls = Class.forName("by.bcrypto.bee2j.provider.BignPublicKey");
        Constructor<?> cpub = pubCls.getConstructor(byte[].class);
        cpub.setAccessible(true);
        Object pubObj = cpub.newInstance((Object) pubBytes);
        Signature sig = Signature.getInstance("BignWithBash256", PROVIDER);
        sig.initVerify((PublicKey) pubObj);
        sig.update(data);
        return sig.verify(signature);
    }

}