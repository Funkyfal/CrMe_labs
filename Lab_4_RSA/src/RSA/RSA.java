package RSA;

import RSA.Keys.CRTPrivateKey;
import RSA.Keys.KeyPair;
import RSA.Keys.PrivateKey;
import RSA.Keys.PublicKey;
import mathUtils.ExEuclid.ExtendedEuclid;
import mathUtils.GenPrime.GenPrimeGenerator;
import mathUtils.modPow.ModPowMontgomery;

import java.math.BigInteger;

import static mathUtils.modPow.ModPowSimple.modPowRightToLeft;

public class RSA {

    public static KeyPair generateKeyPairEuler(int modulusBitLength, int primeTestRounds) {
        int primeBitLength = modulusBitLength / 2;

        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q));

        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);
        while (!e.gcd(phi).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        }

        ExtendedEuclid.ExtendedEuclidResult result = ExtendedEuclid.extendedEuclid(e, phi);
        BigInteger d = result.x;
        if (d.compareTo(BigInteger.ZERO) < 0) {
            d = d.mod(phi);
        }

        PublicKey pub = new PublicKey(n, e);
        PrivateKey priv = new PrivateKey(n, d);

        return new KeyPair(pub, priv);
    }

    public static KeyPair generateKeyPairCarmichael(int modulusBitLength, int primeTestRounds) {
        int primeBitLength = modulusBitLength / 2;

        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q));

        BigInteger n = p.multiply(q);
        BigInteger lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));

        BigInteger e = BigInteger.valueOf(65537);
        while (!e.gcd(lambda).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        }

        BigInteger d = e.modInverse(lambda);

        PublicKey pub = new PublicKey(n, e);
        PrivateKey priv = new PrivateKey(n, d);
        return new KeyPair(pub, priv);
    }

    public static BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b));
    }

    public static KeyPair generateKeyPairCRT(int modulusBitLength, int primeTestRounds) {
        int primeBitLength = modulusBitLength / 2;
        BigInteger p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        BigInteger q;
        do {
            q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
        } while (p.equals(q));
        BigInteger n = p.multiply(q);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger e = BigInteger.valueOf(65537);
        while (!e.gcd(phi).equals(BigInteger.ONE)) {
            p = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            do {
                q = GenPrimeGenerator.generateCandidate(primeBitLength, primeTestRounds);
            } while (p.equals(q));
            n = p.multiply(q);
            phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        }
        // Вычисление d по модулю φ(n)
        ExtendedEuclid.ExtendedEuclidResult result = ExtendedEuclid.extendedEuclid(e, phi);
        BigInteger d = result.x;
        if (d.compareTo(BigInteger.ZERO) < 0) {
            d = d.mod(phi);
        }
        PublicKey pub = new PublicKey(n, e);
        CRTPrivateKey privCRT = new CRTPrivateKey(n, d, p, q);
        return new KeyPair(pub, privCRT);
    }

    public static BigInteger encryptWithMontgomery(BigInteger message, PublicKey pub) {
        ModPowMontgomery modPowMontgomery = new ModPowMontgomery(pub.n);
        return modPowMontgomery.modPow(message, pub.e);
    }

    public static BigInteger encryptWithModPowSimple(BigInteger message, PublicKey pub) {
        return modPowRightToLeft(message, pub.e, pub.n);
    }

    public static BigInteger decryptWithMontgomery(BigInteger ciphertext, PrivateKey priv) {
        ModPowMontgomery modPowMontgomery = new ModPowMontgomery(priv.n);
        return modPowMontgomery.modPow(ciphertext, priv.d);
    }

    public static BigInteger decryptWithModPowSimple(BigInteger ciphertext, PrivateKey priv) {
        return modPowRightToLeft(ciphertext, priv.d, priv.n);
    }

    public static BigInteger decryptWithCRT(BigInteger ciphertext, CRTPrivateKey privCRT) {
        BigInteger m1 = ciphertext.modPow(privCRT.dP, privCRT.p);
        BigInteger m2 = ciphertext.modPow(privCRT.dQ, privCRT.q);
        BigInteger h = (privCRT.qInv.multiply(m1.subtract(m2))).mod(privCRT.p);
        return m2.add(h.multiply(privCRT.q));
    }
}

