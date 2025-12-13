package org.bsu.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bsu.crypto.KeyManager;
import org.bsu.utils.HexUtils;

@Getter
@RequiredArgsConstructor
public class Participant {
    private final String name;
    private final KeyManager keyManager;
    private KeyPairData longTermKeys;

    public void ensureKeys() throws Exception {
        if (!keyManager.exists(name)) {
            System.out.println("Keys for " + name + " not found — generating...");
            longTermKeys = keyManager.generateAndSave(name);
        } else {
            longTermKeys = keyManager.load(name);
        }
        System.out.println(name + " priv: " + HexUtils.toHex(longTermKeys.getPrivateKey()));
        System.out.println(name + " pub : " + HexUtils.toHex(longTermKeys.getPublicKey()));
    }

}

