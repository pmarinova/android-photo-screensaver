package pm.android.photoscreensaver;

import android.net.MacAddress;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

public class WakeOnLan {

    private static final String TAG = WakeOnLan.class.getName();

    private static final int WOL_PORT = 9;

    public static void sendWolPacket(MacAddress macAddress) {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress broadcastAddress = getBroadcastAddress(networkInterfaces);

            if (broadcastAddress == null) {
                Log.d(TAG, "failed to get broadcast address");
                return;
            }

            DatagramPacket packet = buildMagicPacket(broadcastAddress, macAddress, WOL_PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

        } catch (IOException e) {
            Log.d(TAG, "error while sending magic packet: ", e);
        }
    }

    // Adapted from https://github.com/Florianisme/WakeOnLan/blob/master/app/src/main/java/de/florianisme/wakeonlan/ui/modify/BroadcastHelper.java
    private static final InetAddress getBroadcastAddress(Enumeration<NetworkInterface> networkInterfaces) {
        return Collections.list(networkInterfaces).stream()
                .filter(networkInterface -> networkInterface.getName().matches("^(wlan|eth).*"))
                .map(NetworkInterface::getInterfaceAddresses)
                .flatMap(Collection::stream)
                .map(InterfaceAddress::getBroadcast)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    // Adapted from https://github.com/Florianisme/WakeOnLan/blob/master/app/src/main/java/de/florianisme/wakeonlan/wol/PacketBuilder.java
    private static DatagramPacket buildMagicPacket(InetAddress broadcastAddress, MacAddress macAddress, int port) {

        byte[] macBytes = macAddress.toByteArray();

        // Packet is 6 times 0xff, 16 times MAC Address of target
        byte[] bytes = new byte[6 + (16 * macBytes.length)];

        // Append 6 times 0xff
        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }

        // Append MAC address 16 times
        for (int i = 0; i < 16; i++) {
            System.arraycopy(macBytes, 0, bytes, (i+1) * 6, macBytes.length);
        }

        return new DatagramPacket(bytes, bytes.length, broadcastAddress, port);
    }

}
