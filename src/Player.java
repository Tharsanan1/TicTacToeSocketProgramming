import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Player {
  private Socket socket;
  private DataInputStream input;
  private DataOutputStream out;

  public Player(String address, int port) {
    try {
      socket = new Socket(address, port);
      input = new DataInputStream(
          new BufferedInputStream(socket.getInputStream()));
      out = new DataOutputStream(
          new BufferedOutputStream(socket.getOutputStream()));
    } catch (UnknownHostException u) {
      System.out.println(u);
    } catch (IOException i) {
      System.out.println(i);
    }
    Scanner scanner = new Scanner(System.in);
    String line = "";
    while (true) {
      try {
        line = input.readLine();
        if(line.contains("split")){
          line = line.substring(2);
          String[] matrix = line.split("split");
          for (String s: matrix) {
            System.out.println(s);
          }
        }
        else{
          if(!(line.equals("\n\r") || line.equals("\n"))){
            System.out.println(line);
          }
        }
        if(line != null && line.contains("your input:")){
          System.out.print(":=> ");
          out.writeUTF(scanner.nextLine() + " \n\r");
          out.flush();
          System.out.print("\nposition sent ");
        }
      } catch (IOException | NullPointerException i) {
        System.out.println("Game finished");
        break;
      }
    }
  }

  public static void main(String[] args) {
    Player client = new Player("127.0.0.1", 5000);
  }
}
