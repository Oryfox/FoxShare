import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.security.Key;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

public class Live {

    final static String serverIP = "localhost";

    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;

    static HashMap<String, PublicKey> availableReceivers;

    public static void startOnlinePresence() {
        try {
            socket = new Socket(serverIP, Server.PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        new Thread(() -> {
            while (socket.isConnected()) {
                try {
                    System.out.println("Waiting for Server..");
                    String rawUTF = in.readUTF();
                    String[] splittedArguments = rawUTF.split("<->");
                    System.out.println("Raw: " + rawUTF);

                    switch (splittedArguments[0]) {
                        case "SEND_FILE": {
                            SecretKeySpec aesKey = new SecretKeySpec(Crypto.decryptWithRSA(Crypto.local.getPrivate(), Base64.getDecoder().decode(splittedArguments[2])), "AES");

                            System.out.println("?");
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[16*1024];
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                System.out.println("??");
                                System.out.println(len);
                                bos.write(buffer, 0, len);
                            }
                            System.out.println("!");

                            Files.write(new File(System.getProperty("user.home") + "/Downloads/" + new String(Crypto.decryptWithAES(aesKey, Base64.getDecoder().decode(splittedArguments[3])))).toPath(), bos.toByteArray());
                        }
                        break;
                        case "GET_PUBLIC": {
                            out.write(Crypto.local.getPublic().getEncoded());
                            out.flush();
                        }
                        break;
                        case "REGISTERED": {
                            System.out.println("Registered successful");
                        }
                        break;
                        case "NOT_REGISTERED": {
                            System.out.println("Register failed. Trying random");
                            register(Integer.toString(new Random().nextInt(999999)));
                        }
                        break;
                        case "PONG": {
                            System.out.println("PONG");
                        }
                        break;
                        case "RECEIVERS": {
                            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                            availableReceivers = (HashMap<String, PublicKey>) objectIn.readObject();
                            for (String s : availableReceivers.keySet())
                                System.out.println("Receiver: " + s + " --> " + availableReceivers.get(s));
                        }
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void register(String name) {
        try {
            out.writeUTF("REGISTER<->" + name);
            out.write(Crypto.local.getPublic().getEncoded());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ping() {
        try {
            out.writeUTF("PING");
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getReceivers() throws IOException {
        out.writeUTF("GET_RECEIVERS");
        out.flush();
    }

    public static void sendFile(File file, String receiver, PublicKey receiversKey) {
        try {
            Key aesKey = Crypto.genAesKey();

            byte[] fileBytes = Files.readAllBytes(file.toPath());

            byte[] encryptedFile = Crypto.encryptWithAES(aesKey, fileBytes);
            String encryptedFilename = Base64.getEncoder().encodeToString(Crypto.encryptWithAES(aesKey, file.getName().getBytes()));
            String encryptedAesKey = Base64.getEncoder().encodeToString(Crypto.encryptWithRSA(receiversKey, aesKey.getEncoded()));

            out.writeUTF("SEND_FILE<->" + receiver + "<->" + encryptedAesKey + "<->" + encryptedFilename);
            out.write(encryptedFile);
            System.out.println(".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

