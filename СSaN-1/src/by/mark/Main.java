package by.mark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.getHardwareAddress() != null) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            processLocalIp(inetAddress);
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static void processLocalIp(InetAddress localInetAddressPC) {
        try {
            NetworkInterface localAdapterPC = NetworkInterface.getByInetAddress(localInetAddressPC);

            byte[] mac = localAdapterPC.getHardwareAddress();
            String macPC = Main.getMACfromRawBytes(mac);

            String hostName = localInetAddressPC.getHostName();

            System.out.println("Name: " + hostName);
            System.out.println("MAC: " + macPC);

            List<InterfaceAddress> interfaceAddressesPC = localAdapterPC.getInterfaceAddresses();
            InterfaceAddress interfaceAddress = interfaceAddressesPC.get(0);
            if (interfaceAddress.getBroadcast() == null) {
                System.err.println("ERROR: Couldn't get IPv4");
                return;
            }

            int maskLen = interfaceAddress.getNetworkPrefixLength();
            byte[] mask = Main.getMaskFromMaskLen(maskLen);
            String displayMask = Main.getIpForDisplay(mask);
            System.out.println("Subnet Mask: " + displayMask);

            byte[] localIp = localInetAddressPC.getAddress();
            byte[] subnet = Main.getSubnet(localIp, mask);
            String displaySubnet = Main.getIpForDisplay(subnet);
            System.out.println("Subnet: " + displaySubnet);

            long maxHostsCount = Main.getMaxHostsCount(mask);
            System.out.println("Addresses count in subnet: " + (maxHostsCount + 1) + "\n");

            System.out.println("Pinging is starting...");
            for (long nodeNumber = 1; nodeNumber <= maxHostsCount; nodeNumber++) {
                byte[] currIp = Main.getIpFromSubnetAndNodeNumber(localIp, nodeNumber, maskLen);
                String displayIp = Main.getIpForDisplay(currIp);
                System.out.print(displayIp);
                Runtime.getRuntime().exec("ping -n 1" + displayIp);
                System.out.print("\r");
            }
            Thread.sleep(15000);
            System.out.println("Ping Ended...\n");

            Process p = Runtime.getRuntime().exec("arp -a");
            List<String> lines;
            try (
                    BufferedReader inputStream = new BufferedReader(
                            new InputStreamReader(p.getInputStream()))) {
                lines = inputStream.lines().collect(Collectors.toList());
            }
            String hostAddress = localInetAddressPC.getHostAddress();
            List<String> addresses = Main.getInterfaceAddresses(hostAddress, lines);
            addresses = addresses.stream()
                    .filter(l -> l.contains("192.168"))
                    .collect(Collectors.toList());

            for (String address : addresses) {
                Pattern pattern = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}");
                Matcher matcher = pattern.matcher(address);
                boolean ban = false;
                while (matcher.find()) {
                    String ip = address.substring(matcher.start(), matcher.end());
                    if (ip.endsWith("255")) {
                        ban = true;
                    } else {
                        System.out.println("IP: " + ip);
                        InetAddress byName = InetAddress.getByName(ip);
                        System.out.println("Name: " + byName.getHostName());
                    }
                }
                if (ban) {
                    continue;
                }
                Pattern patternMac = Pattern.compile("(([a-f0-9][a-f0-9])-){5}([a-f0-9][a-f0-9])");
                Matcher matcherMac = patternMac.matcher(address);
                while (matcherMac.find()) {
                    String addressMac = address.substring(matcherMac.start(), matcherMac.end());
                    String formattedMac = addressMac.toUpperCase().replaceAll("-", ":");
                    System.out.println("MAC: " + formattedMac);
                }
            }
            System.out.println("-------------------------");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getInterfaceAddresses(String hostAddress, List<String> lines) {
        Optional<String> targetLine = lines.stream().filter(l -> l.contains(hostAddress)).findFirst();
        if (targetLine.isPresent()) {
            int ind = lines.indexOf(targetLine.get());
            List<String> res = new LinkedList<>();
            for (int i = ind + 2; i < lines.size() && !lines.get(i).isBlank(); i++) {
                res.add(lines.get(i));
            }
            return res;
        }
        return Collections.emptyList();
    }

    private static byte[] getIpFromSubnetAndNodeNumber(byte[] localIp, long nodeNumber, int maskLen) {
        int ipNum = ((localIp[0] & 0xFF) << 24) + ((localIp[1] & 0xFF) << 16) + ((localIp[2] & 0xFF) << 8) + (localIp[3] & 0xFF);
        long ipNumVal = ipNum & 0xFFFFFFFFL;
        long normalizedIpVal = ipNumVal | ((1L << (32 - maskLen)) - 1);
        long maskVal = ((1L << maskLen) - 1) << (32 - maskLen);
        long nodeVal = maskVal | nodeNumber;
        long resultingIpNum = normalizedIpVal & nodeVal;
        return getBytes(resultingIpNum);
    }

    private static byte[] getBytes(long resultingIpNum) {
        byte firstOctant = (byte) (resultingIpNum >>> 24);
        byte secondOctant = (byte) ((resultingIpNum & 0x00FFFFFF) >> 16);
        byte thirdOctant = (byte) ((resultingIpNum & 0x0000FFFF) >> 8);
        byte fourthOctant = (byte) (resultingIpNum & 0x000000FF);
        return new byte[]{firstOctant, secondOctant, thirdOctant, fourthOctant};
    }

    private static long getMaxHostsCount(byte[] mask) {
        int maskNum = ((mask[0] & 0xFF) << 24) + ((mask[1] & 0xFF) << 16) + ((mask[2] & 0xFF) << 8) + (mask[3] & 0xFF);
        return (~maskNum) & 0xFFFFFFFFL;
    }

    private static byte[] getSubnet(byte[] localIp, byte[] mask) {
        byte firstOctant = (byte) (localIp[0] & mask[0]);
        byte secondOctant = (byte) (localIp[1] & mask[1]);
        byte thirdOctant = (byte) (localIp[2] & mask[2]);
        byte fourthOctant = (byte) (localIp[3] & mask[3]);
        return new byte[]{firstOctant, secondOctant, thirdOctant, fourthOctant};
    }

    private static String getIpForDisplay(byte[] ip) {
        int i = 4;
        StringBuilder ipBuilder = new StringBuilder();
        for (byte octant : ip) {
            ipBuilder.append(octant & 0xFF);
            if (--i > 0) {
                ipBuilder.append('.');
            }
        }
        return ipBuilder.toString();
    }

    private static byte[] getMaskFromMaskLen(int maskLen) {
        long maskVal = ((1L << maskLen) - 1) << (32 - maskLen);
        return getBytes(maskVal);
    }

    private static String getMACfromRawBytes(byte[] mac) {
        if (mac == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            String format = String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
            sb.append(format);
        }
        return sb.toString();
    }
}

