package org.bsu;

import org.bsu.crypto.KeyManager;
import org.bsu.model.Participant;
import org.bsu.protocol.BpaceProtocol;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        KeyManager km = new KeyManager(Paths.get("keys"));
        Participant alice = new Participant("alice", km);
        Participant bob = new Participant("bob", km);

        alice.ensureKeys();
        bob.ensureKeys();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter shared password for BPACE: ");
        String pwd = sc.nextLine();

        BpaceProtocol prot = new BpaceProtocol(alice, bob, pwd);
        prot.run();
    }
}
