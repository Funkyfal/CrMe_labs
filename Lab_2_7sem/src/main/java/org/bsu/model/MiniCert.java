package org.bsu.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MiniCert {
    private final String name;     // например "alice"
    private final byte[] pubKey;   // raw public key bytes (как в .pub)
    private final byte[] trentSig; // подпись Трента от (name || pubKey)
}