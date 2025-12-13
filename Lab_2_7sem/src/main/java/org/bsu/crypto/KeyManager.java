package org.bsu.crypto;

import lombok.RequiredArgsConstructor;
import org.bsu.model.KeyPairData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@RequiredArgsConstructor
public class KeyManager {
    private final Path keysDir;

    public boolean exists(String name) {
        return Files.exists(keysDir.resolve(name + ".priv")) && Files.exists(keysDir.resolve(name + ".pub"));
    }

    public KeyPairData load(String name) throws IOException {
        byte[] priv = Files.readAllBytes(keysDir.resolve(name + ".priv"));
        byte[] pub = Files.readAllBytes(keysDir.resolve(name + ".pub"));
        return new KeyPairData(priv, pub);
    }

    public void save(String name, KeyPairData kp) throws IOException {
        Files.createDirectories(keysDir);
        Files.write(keysDir.resolve(name + ".priv"), kp.getPrivateKey(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(keysDir.resolve(name + ".pub"), kp.getPublicKey(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public KeyPairData generateAndSave(String name) throws Exception {
        KeyPairData kp;
        try {
            kp = CryptoUtils.generateLongTermKeyPair();
        } catch (Throwable t) {
            // логируем причину сбоя генерации ключей
            System.err.println("Error while generating long-term key pair: " + t.getMessage());
            t.printStackTrace();
            throw new Exception("Key pair generation failed for " + name, t);
        }

        if (kp.getPrivateKey() == null || kp.getPublicKey() == null) {
            System.err.println("DEBUG: Generated KeyPairData has null part(s)."
                    + " priv==null? " + (kp.getPrivateKey() == null)
                    + ", pub==null? " + (kp.getPublicKey() == null));
            // Дополнительно — выводим объекты (если есть toString) — или бросаем понятную ошибку
            throw new IllegalStateException("Generated KeyPairData contains null byte[] - cannot save to files. " +
                    "Ensure CryptoUtils.generateLongTermKeyPair() returns non-null encoded keys.");
        }

        // если всё ок — сохраняем
        save(name, kp);
        return kp;
    }

}
