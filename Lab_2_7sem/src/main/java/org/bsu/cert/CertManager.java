package org.bsu.cert;

import org.bsu.model.MiniCert;
import org.bsu.utils.HexUtils;
import org.bsu.crypto.CryptoUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class CertManager {
    private static final String TRENT_PUB_HEX = "AF25D38589C85A6211597F55927EDF03541C925B7E7CF6EFDB6ABBE2480FED82392F3C7DB51FB876FB3588D1C5FF46425FC08E00A8B8C008A73AD851F6EA4BAE";

    private final Path certsDir;

    public CertManager(Path certsDir) {
        this.certsDir = certsDir;
    }

    public MiniCert loadCert(String name) throws Exception {
        Path f = certsDir.resolve(name + ".cert");
        if (!Files.exists(f)) return null;
        try (BufferedReader r = Files.newBufferedReader(f)) {
            String nm = r.readLine();
            String pubHex = r.readLine();
            String sigB64 = r.readLine();
            if (nm == null || pubHex == null || sigB64 == null) throw new IOException("Bad cert file");
            byte[] pub = HexToBytes(pubHex);
            byte[] sig = Base64.getDecoder().decode(sigB64);
            return new MiniCert(nm, pub, sig);
        }
    }

    public void saveCert(MiniCert cert) throws Exception {
        Files.createDirectories(certsDir);
        Path f = certsDir.resolve(cert.getName() + ".cert");
        try (BufferedWriter w = Files.newBufferedWriter(f)) {
            w.write(cert.getName()); w.newLine();
            w.write(HexUtils.toHex(cert.getPubKey())); w.newLine();
            w.write(Base64.getEncoder().encodeToString(cert.getTrentSig())); w.newLine();
        }
    }

    // Выписать сертификат: Trent подписывает (name || pub)
    public MiniCert issueCert(String name, byte[] pubKey, byte[] trentPrivBytes) throws Exception {
        byte[] data = concat(name.getBytes(), pubKey);
        byte[] sig = CryptoUtils.signWithBignPrivate(trentPrivBytes, data);
        return new MiniCert(name, pubKey, sig);
    }

    // Проверить сертификат используя публичный ключ Трента (жёстко запрограм.)
    public boolean verifyCert(MiniCert cert) throws Exception {
        byte[] trentPub = HexToBytes(TRENT_PUB_HEX);
        byte[] data = concat(cert.getName().getBytes(), cert.getPubKey());
        return CryptoUtils.verifyWithBignPublic(trentPub, data, cert.getTrentSig());
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    private static byte[] HexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) return new byte[0];
        int l = hex.length();
        byte[] out = new byte[l/2];
        for (int i = 0; i < out.length; ++i) out[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        return out;
    }
}
