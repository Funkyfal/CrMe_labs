package org.bsu;

import org.bsu.cert.CertManager;
import org.bsu.crypto.KeyManager;
import org.bsu.model.Participant;
import org.bsu.protocol.BpaceProtocol;

import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        CertManager certManager = new CertManager(Paths.get("certs"));
        KeyManager km = new KeyManager(Paths.get("keys"));
        Participant alice = new Participant("alice", km);
        Participant bob = new Participant("bob", km);

        alice.ensureKeys();
        bob.ensureKeys();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter shared password for BPACE: ");
        String pwd = sc.nextLine();

        BpaceProtocol prot = new BpaceProtocol(alice, bob, pwd, certManager);
        prot.run();

        // after prot.run()
        byte[] K0 = prot.getK0();
        org.bsu.channel.SecureChannel aliceChan = new org.bsu.channel.SecureChannel(K0);
        org.bsu.channel.SecureChannel bobChan = new org.bsu.channel.SecureChannel(K0);

// Alice -> Bob
        byte[] msg = "Hello Bob, this is Alice".getBytes();
        byte[] packet = aliceChan.protect(msg);

// Bob unprotects
        byte[] received = bobChan.unprotect(packet);

// Bob replies
        byte[] reply = "Hello Alice, received your message".getBytes();
        byte[] rPacket = bobChan.protect(reply);
        byte[] rReceived = aliceChan.unprotect(rPacket);
    }
}
