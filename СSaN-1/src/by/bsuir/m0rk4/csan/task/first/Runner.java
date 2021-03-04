package by.bsuir.m0rk4.csan.task.first;

import by.bsuir.m0rk4.csan.task.first.network.scanner.IpScanner;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;

public class Runner {

    private final IpScanner ipScanner;

    public Runner() {
        ipScanner = new IpScanner();
    }

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        new Runner().run();
        long t2 = System.currentTimeMillis();

        System.out.println("TOTAL TIME: " + (t2 - t1) / 1000.0);
    }

    private void run() {
        try {
            NetworkInterface.networkInterfaces()
                    .filter(networkInterface -> {
                        try { return networkInterface.getHardwareAddress() != null; }
                        catch (SocketException e) { return false; } })
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(inetAddress -> inetAddress instanceof Inet4Address)
                    .forEach(ipScanner::processIp);
        } catch (SocketException e) {
            System.err.println("Couldn't get list of network interfaces.");
        }
    }

}

