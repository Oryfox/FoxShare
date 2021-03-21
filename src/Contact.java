import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Contact {

    final static int PORT = 15694;

    static boolean receiving = false;
    static List<String> availableHosts = new ArrayList<>();

    public static void send(String ip, File[] files) {
        for (File f : files) send(ip, f);
    }

    public static void send(String ip, File file) {
        try {
            byte[] bytes = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            //noinspection ResultOfMethodCallIgnored
            fis.read(bytes);
            fis.close();

            send(ip, file.getName(), bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(String ip, String fileName, byte[] fileData) {
        Key key = Crypto.getAESKey();

        String encryptedKey = Base64.getEncoder().encodeToString(Crypto.encryptAesKey(Crypto.getPublicKey(ip),key.getEncoded()));
        byte[] encryptedFileData = Crypto.encryptFile(key,fileData);

        try {
            Socket socket = new Socket(ip, PORT);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF("SEND_FILE<->" + encryptedKey + "<->" + Base64.getEncoder().encodeToString(Crypto.encryptFile(key,fileName.getBytes())));
            outputStream.write(encryptedFileData);
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startProvider() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(Contact.PORT);

                while (true) {
                    Socket socket = serverSocket.accept();

                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String[] utf = inputStream.readUTF().split("<->"); //Action<->AES-Key<->Filename<->EncryptedData

                    switch (utf[0]) {
                        case "GET_PUBLIC_KEY": {
                            socket.getOutputStream().write(Crypto.local.getPublic().getEncoded());
                            inputStream.close();
                            socket.close();
                        }
                        break;
                        case "SEND_FILE": {
                            if (receiving) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                                int count;
                                byte[] buffer = new byte[16 * 1024];
                                while ((count = inputStream.read(buffer)) > 0) {
                                    byteArrayOutputStream.write(buffer, 0, count);
                                }
                                inputStream.close();
                                socket.close();

                                SecretKeySpec aesKey = new SecretKeySpec(Crypto.decryptAesKey(Crypto.local.getPrivate(), Base64.getDecoder().decode(utf[1])), "AES");
                                Files.write(new File(System.getProperty("user.home") + "/Downloads/" + new String(Crypto.decryptFile(aesKey, Base64.getDecoder().decode(utf[2])))).toPath(), Crypto.decryptFile(aesKey, byteArrayOutputStream.toByteArray()));
                            } else {
                                inputStream.close();
                                socket.close();
                            }
                        }
                        break;
                        case "PING": {
                            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                            if (receiving) {
                                outputStream.writeUTF("RESPONSE");
                            } else {
                                outputStream.writeUTF("OFFLINE");
                            }
                            outputStream.close();
                            socket.close();
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void startPing() {
        for (int i = 2; i < 256; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    Socket socket = new Socket("192.168.178." + finalI, PORT);
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF("PING");
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    if (inputStream.readUTF().equals("RESPONSE")) {
                        Contact.availableHosts.add(socket.getInetAddress().getHostName());
                        {
                            MainFrame.frame.hostNames.add(new RoundedButton(socket.getInetAddress().getHostName(), e -> Contact.send(socket.getInetAddress().getHostAddress(),Sender.files), OryColors.YELLOW));
                        }
                    }
                } catch (IOException ignored) {
                }
            }).start();
        }
    }
}
