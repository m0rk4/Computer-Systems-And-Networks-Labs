package by.bsuir.m0rk4.csan.task.first.network.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class IpParser {

    public byte[] getIpFromSubnetAndNodeNumber(byte[] localIp, long nodeNumber, int maskLen) {
        int ipNum = ((localIp[0] & 0xFF) << 24) + ((localIp[1] & 0xFF) << 16) + ((localIp[2] & 0xFF) << 8) + (localIp[3] & 0xFF);
        long ipNumVal = ipNum & 0xFFFFFFFFL;
        long normalizedIpVal = ipNumVal | ((1L << (32 - maskLen)) - 1);
        long maskVal = ((1L << maskLen) - 1) << (32 - maskLen);
        long nodeVal = maskVal | nodeNumber;
        long resultingIpNum = normalizedIpVal & nodeVal;
        return getBytes(resultingIpNum);
    }

    public byte[] getBytes(long resultingIpNum) {
        byte firstOctant = (byte) (resultingIpNum >>> 24);
        byte secondOctant = (byte) ((resultingIpNum & 0x00FFFFFF) >> 16);
        byte thirdOctant = (byte) ((resultingIpNum & 0x0000FFFF) >> 8);
        byte fourthOctant = (byte) (resultingIpNum & 0x000000FF);
        return new byte[]{firstOctant, secondOctant, thirdOctant, fourthOctant};
    }

    public long getMaxHostsCount(byte[] mask) {
        int maskNum = ((mask[0] & 0xFF) << 24) + ((mask[1] & 0xFF) << 16) + ((mask[2] & 0xFF) << 8) + (mask[3] & 0xFF);
        return (~maskNum) & 0xFFFFFFFFL;
    }

    public byte[] getSubnet(byte[] localIp, byte[] mask) {
        byte firstOctant = (byte) (localIp[0] & mask[0]);
        byte secondOctant = (byte) (localIp[1] & mask[1]);
        byte thirdOctant = (byte) (localIp[2] & mask[2]);
        byte fourthOctant = (byte) (localIp[3] & mask[3]);
        return new byte[]{firstOctant, secondOctant, thirdOctant, fourthOctant};
    }

    public String getIpForDisplay(byte[] ip) {
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

    public byte[] getMaskFromMaskLen(int maskLen) {
        long maskVal = ((1L << maskLen) - 1) << (32 - maskLen);
        return getBytes(maskVal);
    }

    public String getMacFromRawBytes(byte[] mac) {
        if (mac == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            String format = String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
            sb.append(format);
        }
        return sb.toString();
    }

    public List<String> getInterfaceAddresses(String hostAddress, List<String> lines) {
        Optional<String> targetLine = lines.stream()
                .filter(l -> l.contains(hostAddress))
                .findFirst();
        if (targetLine.isPresent()) {
            String target = targetLine.get();
            int ind = lines.indexOf(target);
            List<String> res = new LinkedList<>();
            for (int i = ind + 2; i < lines.size() && !lines.get(i).isBlank(); i++) {
                String line = lines.get(i);
                res.add(line);
            }
            return res;
        }
        return Collections.emptyList();
    }
}
