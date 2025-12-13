package org.bsu.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bsu.cert.CertManager;
import org.bsu.crypto.CryptoUtils;
import org.bsu.model.KeyPairData;
import org.bsu.model.MiniCert;
import org.bsu.model.Participant;
import org.bsu.utils.HexUtils;

import java.util.Arrays;

import static org.bsu.cert.CertManager.concat;

@RequiredArgsConstructor
@Getter
public class BpaceProtocol {
    private final Participant alice;
    private final Participant bob;
    private final String password; // текстовый пароль, введённый вручную
    private final CertManager certManager;

    private byte[] K0;

    public void run() throws Exception {
        System.out.println("=== BPACE (bee2) start ===");

        // Step 0: load long-term keys
        KeyPairData aLT = alice.getLongTermKeys();
        KeyPairData bLT = bob.getLongTermKeys();

        MiniCert aCert = certManager.loadCert(alice.getName());
        MiniCert bCert = certManager.loadCert(bob.getName());

// if certs absent, optionally issue them locally using TrentPriv (only for testing)
// but typically certs should be created by Trent and files placed in certs dir.

        if (aCert == null || bCert == null) {
            throw new IllegalStateException("Missing mini-certificate(s). Place [name].cert in certs dir or run cert issuance.");
        }
        if (!certManager.verifyCert(aCert)) throw new SecurityException("Alice cert failed verification by Trent");
        if (!certManager.verifyCert(bCert)) throw new SecurityException("Bob cert failed verification by Trent");

// check that cert public keys match local stored public keys
        if (!java.util.Arrays.equals(aCert.getPubKey(), alice.getLongTermKeys().getPublicKey()))
            throw new SecurityException("Alice cert public key does not match local public key");
        if (!java.util.Arrays.equals(bCert.getPubKey(), bob.getLongTermKeys().getPublicKey()))
            throw new SecurityException("Bob cert public key does not match local public key");
        System.out.println("Mini-certificates verified by Trent.");

        // Step 1: password -> Pw (use CryptoUtils.passwordToPoint)
        byte[] pwA = CryptoUtils.passwordToPoint(password.getBytes());
        byte[] pwB = CryptoUtils.passwordToPoint(password.getBytes());
        System.out.println("Pw A: " + HexUtils.toHex(pwA));
        System.out.println("Pw B: " + HexUtils.toHex(pwB));

        // Step 2: generate ephemeral keys
        KeyPairData eA = CryptoUtils.generateEphemeralKeyPair();
        KeyPairData eB = CryptoUtils.generateEphemeralKeyPair();
        System.out.println("Ephemeral A priv: " + HexUtils.toHex(eA.getPrivateKey()));
        System.out.println("Ephemeral A pub : " + HexUtils.toHex(eA.getPublicKey()));
        System.out.println("Ephemeral B priv: " + HexUtils.toHex(eB.getPrivateKey()));
        System.out.println("Ephemeral B pub : " + HexUtils.toHex(eB.getPublicKey()));

        // sign ephemeral data: data = ephPub || pw
        byte[] dataA = concat(eA.getPublicKey(), pwA);
        byte[] dataB = concat(eB.getPublicKey(), pwB);

// Alice signs with her long-term private bytes (from file)
        byte[] aLongPriv = alice.getLongTermKeys().getPrivateKey();
        byte[] sigA = CryptoUtils.signWithBignPrivate(aLongPriv, dataA);

// Bob signs with his long-term private
        byte[] bLongPriv = bob.getLongTermKeys().getPrivateKey();
        byte[] sigB = CryptoUtils.signWithBignPrivate(bLongPriv, dataB);

// Exchange and verify using certs' public keys
        if (!CryptoUtils.verifyWithBignPublic(bCert.getPubKey(), dataB, sigB)) throw new SecurityException("Bob ephemeral signature invalid");
        if (!CryptoUtils.verifyWithBignPublic(aCert.getPubKey(), dataA, sigA)) throw new SecurityException("Alice ephemeral signature invalid");

        System.out.println("Mutual authentication by long-term signatures OK.");

        // Step 3: compute shared secret Z (ECDH)
        byte[] zA = CryptoUtils.computeSharedSecret(pwA, eA.getPublicKey(), eB.getPublicKey());
        byte[] zB = CryptoUtils.computeSharedSecret(pwB, eB.getPublicKey(), eA.getPublicKey());
        System.out.println("zA: " + HexUtils.toHex(zA));
        System.out.println("zB: " + HexUtils.toHex(zB));
        if (!Arrays.equals(zA, zB)) throw new IllegalStateException("ECDH mismatch");

        // Step 4: KDF to K0 (we include Pw in info)
        byte[] k0a = CryptoUtils.kdf(zA, pwA, 32); // 256-bit
        byte[] k0b = CryptoUtils.kdf(zB, pwB, 32);
        System.out.println("K0 A: " + HexUtils.toHex(k0a));
        System.out.println("K0 B: " + HexUtils.toHex(k0b));
        if (!Arrays.equals(k0a, k0b)) throw new IllegalStateException("K0 mismatch");
        this.K0 = k0a;

        // Step 5: confirmation (compute MAC over transcript)
        byte[] transcript = buildTranscript(eA.getPublicKey(), eB.getPublicKey(), pwA);
        byte[] confirmKey = CryptoUtils.kdf(K0, "confirm".getBytes(), 32);
        byte[] macA = CryptoUtils.computeMac(confirmKey, transcript);
        byte[] macB = CryptoUtils.computeMac(confirmKey, transcript);
        System.out.println("Confirm MAC A: " + HexUtils.toHex(macA));
        System.out.println("Confirm MAC B: " + HexUtils.toHex(macB));
        if (!Arrays.equals(macA, macB)) throw new IllegalStateException("Confirm MAC mismatch");

        System.out.println("=== BPACE completed, K0 established ===");
    }

    private byte[] buildTranscript(byte[] pubA, byte[] pubB, byte[] pw) {
        byte[] out = new byte[pubA.length + pubB.length + pw.length];
        int pos = 0;
        System.arraycopy(pubA, 0, out, pos, pubA.length); pos += pubA.length;
        System.arraycopy(pubB, 0, out, pos, pubB.length); pos += pubB.length;
        System.arraycopy(pw, 0, out, pos, pw.length);
        return out;
    }

}

