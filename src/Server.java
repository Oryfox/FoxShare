import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server implements Runnable {
    static List<Socket> sockets = new ArrayList<>();
    static Map<String, Server> socketMap = new HashMap<String, Server>() {
        @Override
        public Server remove(Object key) {
            System.out.println("Removed " + key);
            return super.remove(key);
        }
    };

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    PublicKey publicKey;

    final static int PORT = 25032;

    static boolean running;

    public static void main(String[] args) throws IOException {
        running = true;
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (running) {
            new Thread(new Server(serverSocket.accept())).start();
        }
    }

    public Server(Socket socket) {
        this.socket = socket;
        sockets.add(socket);
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        try {
            while (!socket.isClosed() && in != null && out != null) {
                System.out.println(socket.getInetAddress().getHostAddress() + ": waiting for command");
                String rawUTF = in.readUTF();
                String[] splitArguments = rawUTF.split("<->"); //Command<->Receiver<->Encrypted AES Key<->ContentHeader/FileName --> Content as byte array
                System.out.println(rawUTF);

                switch (splitArguments[0]) {
                    case "REGISTER": {
                        if (socketMap.containsKey(splitArguments[1])) {
                            out.writeUTF("NOT_REGISTERED");
                        } else {
                            byte[] bytes = new byte[1024];
                            in.read(bytes);
                            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
                            socketMap.put(splitArguments[1], this);
                            out.writeUTF("REGISTERED");
                        }
                        out.flush();
                    }
                    break;
                    case "PING": {
                        out.writeUTF("PONG");
                        out.flush();
                        System.out.println("PING FROM " + socket.getInetAddress().getHostAddress() + " REACHED");
                    }
                    break;
                    case "UNREGISTER": {
                        if (socketMap.containsKey(splitArguments[1])) {
                            out.writeUTF("UNREGISTERED");
                            socketMap.remove(splitArguments[1], this);
                        } else {
                            out.writeUTF("UNREGISTERED");
                        }
                        out.flush();
                    }
                    break;
                    case "SEND_FILE": {
                        if (socketMap.containsKey(splitArguments[1])) {
                            DataOutputStream remoteOut = socketMap.get(splitArguments[1]).out;
                            remoteOut.writeUTF(rawUTF);

                            System.out.println("?");
                            byte[] buffer = new byte[16*1024];
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                System.out.println("??");
                                System.out.println(len);
                                remoteOut.write(buffer, 0, len);
                            }
                            System.out.println("!");
                        }
                    }
                    break;
                    case "GET_PUBLIC": {
                        DataOutputStream outRemote = socketMap.get(splitArguments[1]).out;
                        outRemote.writeUTF("GET_PUBLIC");
                        DataInputStream inRemote = socketMap.get(splitArguments[1]).in;
                        byte[] buffer = new byte[16 * 1024];
                        int count;
                        while ((count = inRemote.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                        }
                        out.flush();
                    }
                    break;
                    case "GET_RECEIVERS": {
                        out.writeUTF("RECEIVERS");
                        HashMap<String, PublicKey> receivers = new HashMap<>();
                        for (String s : socketMap.keySet()) {
                            receivers.put(s,socketMap.get(s).publicKey);
                        }
                        ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
                        objectOut.writeObject(receivers);
                        out.flush();
                    }
                    break;
                }
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            if (e instanceof SocketException || e instanceof EOFException) {
                try {
                    for (String s : socketMap.keySet()) {
                        if (socketMap.get(s).socket == socket) socketMap.remove(s);
                        break;
                    }
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.out.println("Closed stream");
            }
        }
    }
}
