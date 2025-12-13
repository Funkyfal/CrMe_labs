package org.bsu.tools;

import org.bsu.cert.CertManager;
import org.bsu.model.MiniCert;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;

public class CertIssuer {
    public static void main(String[] args) throws Exception {
        String name = (args.length > 0) ? args[0] : "alice";
        Path p = Paths.get("keys/trent.priv");
        if (!Files.exists(p)) {
            System.err.println("Place trent private hex at keys/trent.priv (raw hex)");
            return;
        }
        byte[] trentPriv = hexToBytes(Files.readString(p).trim());
        byte[] pub = Files.readAllBytes(Paths.get("keys", name + ".pub"));

        CertManager cm = new CertManager(Paths.get("certs"));
        MiniCert cert = cm.issueCert(name, pub, trentPriv);
        cm.saveCert(cert);
        System.out.println("Issued cert for " + name);
    }

    private static byte[] hexToBytes(String hex) {
        int l = hex.length();
        byte[] out = new byte[l/2];
        for (int i=0;i<out.length;++i) out[i] = (byte)Integer.parseInt(hex.substring(2*i,2*i+2),16);
        return out;
    }
}
