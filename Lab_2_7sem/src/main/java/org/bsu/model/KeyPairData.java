package org.bsu.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KeyPairData {
    private final byte[] privateKey;
    private final byte[] publicKey;

}
