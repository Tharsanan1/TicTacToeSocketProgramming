import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Server {
  private Socket clientSocket;
  private ServerSocket ticTacServer;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private Socket cachedSocket = null;
  public Server(int port) {
    try {
      ticTacServer = new ServerSocket(port);
      System.out.println("Server started");
      while (true){
        System.out.println("Waiting for first player ...");
        if(cachedSocket != null){
          clientSocket = cachedSocket;
          cachedSocket = null;
        }
        else {
          clientSocket = ticTacServer.accept();
        }
        System.out.println("first player accepted");
        TicTacThread firstPlayer = new TicTacThread(clientSocket, true);
        firstPlayer.start();
        System.out.println("Waiting for second player ...");
        clientSocket = ticTacServer.accept();
        System.out.println("second player accepted");
        TicTacThread secondPlayer = new TicTacThread(clientSocket, false);
        secondPlayer.start();
        firstPlayer.setMyPair(secondPlayer);
        secondPlayer.setMyPair(firstPlayer);

        TicTacThread.setGameOn(true);

        while (TicTacThread.isGameOn()){
          clientSocket = ticTacServer.accept();
          if(TicTacThread.isGameOn()) {
            dataOutputStream = new DataOutputStream(
                new BufferedOutputStream(clientSocket.getOutputStream()));
            dataOutputStream.writeUTF("Already two players are playing the game please come again latter" + "\n\r");
            dataOutputStream.flush();
            clientSocket.close();
          }
          else {
            cachedSocket = clientSocket;
          }
        }
      }

    } catch (IOException i) {
      System.out.println(i);
    }
  }

  public static void main(String[] args) {
    Server server = new Server(5000);
  }

}
