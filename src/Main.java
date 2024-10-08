import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

//Each thread created by the main thread is used for serving each client that connects to the server
public class Main extends Thread{
    //Each thread's socket to communicate with client
    public Socket c;
    //Thread safe map that can be accessed by all threads to find to whom they need to send the message
    private static ConcurrentHashMap<Long, Socket> activeUsers = new ConcurrentHashMap<>();

    Main(Socket c){
        this.c=c;
    }

    //Responsible for creating threads for each client that connects
    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(1234);
        while(true){
            Socket c = s.accept();
            Main thread = new Main(c);
            thread.start();
        }
    }

    //method that is run individually by each thread
    @Override
    public void run() {
        Long currentUserId = null;
        try {
            System.out.println("Client connected!");
            DataInputStream socketIn = new DataInputStream(c.getInputStream());
            BufferedReader socketInMessage = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));

            //Get the sender user and put it in the map along with the socket
            currentUserId = socketIn.readLong();
            activeUsers.put(currentUserId, c);

            while (true){
                //Get the receiver user and the message
                Long userId = socketIn.readLong();
                String message = socketInMessage.readLine();
                System.out.printf("User: %d\nMessage:%s\n", userId, message);

                //Get the socket connected to the user
                Socket userSocket = activeUsers.get(userId);
                //Sends the message to the receiver user
                if(userSocket != null) {
                    DataOutputStream userSocketOut = new DataOutputStream(userSocket.getOutputStream());
                    BufferedWriter userSocketOutMessage = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream(), StandardCharsets.UTF_8));

                    userSocketOut.writeLong(currentUserId);
                    userSocketOutMessage.write(message);
                    userSocketOutMessage.newLine();
                    userSocketOutMessage.flush();
                }
            }
        }
        catch (IOException e){
            if(currentUserId != null)
                activeUsers.remove(currentUserId);
        }
        System.out.println(activeUsers.size());
    }
}