package org.bsu.channel;

import lombok.RequiredArgsConstructor;
import org.bsu.crypto.CryptoUtils;
import org.bsu.utils.HexUtils;

import java.security.SecureRandom;
import java.util.Arrays;

@RequiredArgsConstructor
public class SecureChannel {
    private final byte[] K0;
    private final byte[] K1;
    private final byte[] K2;
    private static final SecureRandom RNG = new SecureRandom();

    public SecureChannel(byte[] K0) throws Exception {
        this.K0 = K0;
        byte[][] keys = CryptoUtils.splitKeys(K0);
        this.K1 = keys[0];
        this.K2 = keys[1];
        System.out.println("Derived K1: " + HexUtils.toHex(K1));
        System.out.println("Derived K2: " + HexUtils.toHex(K2));
    }

    public byte[] protect(byte[] plain) throws Exception {
        if (plain.length > 256) throw new IllegalArgumentException("Max 256 bytes");
        byte[] iv = new byte[16]; RNG.nextBytes(iv);
        byte[] cipher = CryptoUtils.encryptCFB(K2, iv, plain);
        byte[] macInput = new byte[iv.length + cipher.length];
        System.arraycopy(iv, 0, macInput, 0, iv.length);
        System.arraycopy(cipher, 0, macInput, iv.length, cipher.length);
        byte[] mac = CryptoUtils.computeMac(K1, macInput);
        byte[] out = new byte[iv.length + cipher.length + mac.length];
        System.arraycopy(iv, 0, out, 0, iv.length);
        System.arraycopy(cipher, 0, out, iv.length, cipher.length);
        System.arraycopy(mac, 0, out, iv.length + cipher.length, mac.length);
        System.out.println("Protected: IV=" + HexUtils.toHex(iv) + " C=" + HexUtils.toHex(cipher) + " MAC=" + HexUtils.toHex(mac));
        return out;
    }

    public byte[] unprotect(byte[] packet) throws Exception {
        final int ivLen = 16;
        if (packet == null || packet.length < ivLen) throw new IllegalArgumentException("Invalid packet");

        // Определяем длину MAC динамически — запросив длину у алгоритма через CryptoUtils.
        // computeMac(K1, new byte[0]) вернёт MAC от пустых данных — нам нужна только длина результата.
        byte[] macSample = CryptoUtils.computeMac(K1, new byte[0]);
        int macLen = macSample.length;

        if (packet.length < ivLen + macLen) throw new IllegalArgumentException("Invalid packet (too short for IV+MAC)");

        byte[] iv = Arrays.copyOfRange(packet, 0, ivLen);
        byte[] mac = Arrays.copyOfRange(packet, packet.length - macLen, packet.length);
        byte[] cipher = Arrays.copyOfRange(packet, ivLen, packet.length - macLen);

        // recompute MAC and compare
        byte[] macInput = new byte[iv.length + cipher.length];
        System.arraycopy(iv, 0, macInput, 0, iv.length);
        System.arraycopy(cipher, 0, macInput, iv.length, cipher.length);

        byte[] expected = CryptoUtils.computeMac(K1, macInput);
        if (!Arrays.equals(expected, mac)) throw new SecurityException("MAC failed");

        byte[] plain = CryptoUtils.decryptCFB(K2, iv, cipher);
        System.out.println("Unprotected plain (hex): " + HexUtils.toHex(plain));
        return plain;
    }

}
