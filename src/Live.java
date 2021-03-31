import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Live {

    final static String serverIP = "localhost";

    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;

    public static void startOnlinePresence() {
        try {
            socket = new Socket(serverIP, Server.PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        /*new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    String rawUTF = in.readUTF();
                    String[] splittedArguments = rawUTF.split("<->");

                    switch (splittedArguments[0]) {
                        case "SEND_FILE": {
                            int accepted = JOptionPane.showConfirmDialog(null, "Do you want to accept the income file?", "Accept file", JOptionPane.YES_NO_OPTION);

                            if (accepted == JOptionPane.YES_OPTION) {
                                SecretKeySpec aesKey = new SecretKeySpec(Crypto.decryptWithRSA(Crypto.local.getPrivate(), Base64.getDecoder().decode(splittedArguments[2])), "AES");

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                byte[] bytes = new byte[16 * 1024];
                                int count;
                                while ((count = in.read(bytes)) > 0) {
                                    bos.write(bytes, 0, count);
                                }

                                Files.write(new File(System.getProperty("user.home") + "/Downloads/" + new String(Crypto.decryptWithAES(aesKey, Base64.getDecoder().decode(splittedArguments[3])))).toPath(), bos.toByteArray());
                                bos.close();
                            }
                        }
                        break;
                        case "GET_PUBLIC": {
                            out.write(Crypto.local.getPublic().getEncoded());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    public static boolean register(String name) {
        try {
            out.writeUTF("REGISTER<->" + name);
            String response = in.readUTF();
            System.out.println("nach");
            System.out.println(response);
            return response.equalsIgnoreCase("REGISTERED");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ping() {
        try {
            System.out.println("PING");
            out.writeUTF("PING");
            return in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getReceivers() throws IOException {
        out.writeUTF("GET_RECEIVERS");
        out.flush();
        System.out.println("1");
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        System.out.println("2");
        byte[] bytes = new byte[16 * 1024];
        int count;
        while ((count = in.read(bytes)) > 0) {
            byteOut.write(bytes, 0, count);
            System.out.println(".");
        }
        System.out.println("3");
        return byteOut.toString();
    }
}
