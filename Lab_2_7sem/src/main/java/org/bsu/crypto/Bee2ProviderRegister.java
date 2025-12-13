package org.bsu.crypto;

import by.bcrypto.bee2j.provider.Bee2SecurityProvider;
import lombok.RequiredArgsConstructor;

import java.security.Security;

@RequiredArgsConstructor
public final class Bee2ProviderRegister {

    public static void register() {
        for (java.security.Provider p : Security.getProviders()) {
            if ("Bee2".equals(p.getName())) return;
        }
        Security.addProvider(new Bee2SecurityProvider());
        System.out.println("Bee2 provider registered.");
    }
}