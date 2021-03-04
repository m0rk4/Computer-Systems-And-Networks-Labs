package by.bsuir.m0rk4.csan.task.first.network.scanner;

import by.bsuir.m0rk4.csan.task.first.network.parser.IpParser;
import by.bsuir.m0rk4.csan.task.first.network.ping.PingTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IpScanner {

    private static final int MAX_THREADS_COUNT = 120;

    private final IpParser ipParser;
    private final Set<byte[]> usedSubnets;

    public IpScanner() {
        this.ipParser = new IpParser();
        this.usedSubnets = new HashSet<>();
    }

    public void processIp(InetAddress localInetAddressPC) {
        try {
            int maskLen = getMaskLen(localInetAddressPC);
            byte[] localIp = localInetAddressPC.getAddress();
            byte[] mask = ipParser.getMaskFromMaskLen(maskLen);
            byte[] subnet = ipParser.getSubnet(localIp, mask);
            if (usedSubnets.contains(subnet)) {
                return;
            }
            usedSubnets.add(subnet);

            NetworkInterface localAdapterPC = NetworkInterface.getByInetAddress(localInetAddressPC);

            String hostName = localInetAddressPC.getHostName();
            System.out.println("Name: " + hostName);

            byte[] mac = localAdapterPC.getHardwareAddress();
            String macPC = ipParser.getMacFromRawBytes(mac);
            System.out.println("MAC: " + macPC);

            String displaySubnet = ipParser.getIpForDisplay(subnet);
            System.out.println("Subnet: " + displaySubnet);

            long maxHostsCount = ipParser.getMaxHostsCount(mask);
            System.out.println("Addresses count in subnet: " + (maxHostsCount + 1) + "\n");

            System.out.println("Pinging is active...");
            ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);
            for (long nodeNumber = 1; nodeNumber < maxHostsCount; nodeNumber++) {
                byte[] currIp = ipParser.getIpFromSubnetAndNodeNumber(localIp, nodeNumber, maskLen);
                String displayIp = ipParser.getIpForDisplay(currIp);
                Runnable pingTask = new PingTask(displayIp);
                pool.execute(pingTask);
            }
            pool.shutdown();
            try {
                if (!pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ex) {
                pool.shutdownNow();
            }
            System.out.println("Ping Ended...\n");

            try {
                Process p = Runtime.getRuntime().exec("arp -a");
                try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    List<String> lines = inputStream.lines().collect(Collectors.toList());
                    String hostAddress = localInetAddressPC.getHostAddress();
                    List<String> addresses = ipParser.getInterfaceAddresses(hostAddress, lines);
                    addresses = addresses.stream()
                            .filter(l -> l.contains("192.168"))
                            .collect(Collectors.toList());

                    for (String address : addresses) {
                        Pattern pattern = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}");
                        Matcher matcher = pattern.matcher(address);
                        boolean ban = false;

                        byte[] broadcastIp = ipParser.getIpFromSubnetAndNodeNumber(localIp, maxHostsCount, maskLen);
                        String broadcast = ipParser.getIpForDisplay(broadcastIp);

                        while (matcher.find()) {
                            String ip = address.substring(matcher.start(), matcher.end());
                            if (ip.equals(broadcast)) {
                                ban = true;
                            } else {
                                System.out.println("IP: " + ip);
                                InetAddress byName = InetAddress.getByName(ip);
                                String host = byName.getHostName();
                                System.out.println("Name: " + host);
                            }
                        }
                        if (ban) {
                            continue;
                        }
                        Pattern patternMac = Pattern.compile("(([a-fA-F0-9][a-fA-F0-9])-){5}([a-fA-F0-9][a-fA-F0-9])");
                        Matcher matcherMac = patternMac.matcher(address);
                        while (matcherMac.find()) {
                            String addressMac = address.substring(matcherMac.start(), matcherMac.end());
                            String formattedMac = addressMac.toUpperCase().replaceAll("-", ":");
                            System.out.println("MAC: " + formattedMac);
                        }
                    }
                    System.out.println("-------------------------");
                } catch (IOException e) {
                    System.err.println("Couldn't extract arp table.");
                }
            } catch (IOException e) {
                System.err.println("Couldn't load arp table.");
            }
        } catch (SocketException e) {
            System.out.println("Couldn't load network data.");
        }
    }

    private int getMaskLen(InetAddress localInetAddressPC) throws SocketException {
        NetworkInterface localAdapterPC = NetworkInterface.getByInetAddress(localInetAddressPC);
        List<InterfaceAddress> interfaceAddressesPC = localAdapterPC.getInterfaceAddresses();
        InterfaceAddress interfaceAddress = interfaceAddressesPC.get(0);
        return interfaceAddress.getNetworkPrefixLength();
    }
}
