import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Server implements Runnable
{
    static List<Socket> sockets = new ArrayList<>();
    static Map<String,Server> socketMap = new HashMap<>();

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final static int PORT = 25032;

    static boolean running;

    public static void main(String [] args) throws IOException {
        running = true;
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (running) {
            new Thread(new Server(serverSocket.accept())).start();
        }
    }

    public Server(Socket socket)
    {
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
        while (!socket.isClosed() && in != null && out != null) {
            try {
                System.out.println(socket.getInetAddress().getHostAddress() + ": waiting for command");
                String rawUTF = in.readUTF();
                String[] splitArguments = rawUTF.split("<->"); //Command<->Receiver<->Encrypted AES Key<->ContentHeader/FileName --> Content as byte array
                System.out.println(rawUTF);

                switch (splitArguments[0]) {
                    case "REGISTER": {
                        if (socketMap.containsKey(splitArguments[1])) {
                            out.writeUTF("NOT_REGISTERED");
                        } else {
                            socketMap.put(splitArguments[1], this);
                            out.writeUTF("REGISTERED");
                        }
                        out.flush();
                    }
                    break;
                    case "PING": {
                        out.writeUTF("PONG");
                        out.flush();
                        System.out.println("PING REACHED");
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

                            byte[] buffer = new byte[16*1024];
                            int count;
                            while ((count = in.read(buffer)) > 0) {
                                remoteOut.write(buffer,0,count);
                            }
                        }
                    }
                    break;
                    case "GET_PUBLIC": {
                        DataOutputStream outRemote = socketMap.get(splitArguments[1]).out;
                        outRemote.writeUTF("GET_PUBLIC");
                        DataInputStream inRemote = socketMap.get(splitArguments[1]).in;
                        byte[] buffer = new byte[16*1024];
                        int count;
                        while ((count = inRemote.read(buffer)) > 0) {
                            out.write(buffer,0,count);
                        }
                        out.flush();
                    }
                    break;
                    case "GET_RECEIVERS": {
                        List<String> list = new ArrayList<>(socketMap.keySet());
                        StringBuilder allReceivers = new StringBuilder();
                        for (String s : list) allReceivers.append(s).append(";");
                        String string  = allReceivers.toString();
                        if (string.equalsIgnoreCase("")) string = "NO_RECEIVERS";
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.write(string.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                    break;
                }
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    e.printStackTrace();
                    System.out.println("Closed stream");
                }
            }
        }
    }
}
