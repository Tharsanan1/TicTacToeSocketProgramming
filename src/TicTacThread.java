import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TicTacThread extends Thread {
  private static volatile boolean isGameOn = false;
  private static volatile boolean firstOneChance = true;
  private static volatile int[][] matrix = new int[6][6];
  private static volatile boolean opponentWon = false;
  private Socket socket;
  private boolean iAmFirst;
  private TicTacThread myPair = null;
  private DataInputStream dataInputStream;
  private DataOutputStream dataOutputStream;
  private static volatile boolean[] lastDied = new boolean[]{false, false};

  public TicTacThread(Socket socket, boolean iAmFirst) {
    this.socket = socket;
    this.iAmFirst = iAmFirst;
  }

  public static boolean isGameOn() {
    return isGameOn;
  }

  public static void setGameOn(boolean gameOn) {
    isGameOn = gameOn;
  }

  public TicTacThread getMyPair() {
    return myPair;
  }

  public void setMyPair(TicTacThread myPair) {
    this.myPair = myPair;
  }

  @Override
  public void run() {
    try {
      dataInputStream = new DataInputStream(
          new BufferedInputStream(socket.getInputStream()));
      dataOutputStream = new DataOutputStream(
          new BufferedOutputStream(socket.getOutputStream()));
      if (!isGameOn) {
        System.out.println("notifying player to wait");
        dataOutputStream.writeUTF("Please wait until next player to come " + "\n\r");
        dataOutputStream.flush();
      }
      while (!isGameOn()) {
        Thread.sleep(200);
      }
      dataOutputStream.writeUTF("Your mark is: " + (iAmFirst ? "X" : "O") + "\n\r");
      dataOutputStream.flush();
      System.out.println("players are notified to play");
      String line;
      boolean notifiedToWait = false;
      while (true) {
        Thread.sleep(200);
        if (firstOneChance && iAmFirst) {
          displayCurrentMatrix();
          if (opponentWon) {
            closeEverything();
            break;
          }
          notifiedToWait = false;
          dataOutputStream.writeUTF("Player X provide your input: (ex : '2,4')" + "\n\r");
          dataOutputStream.flush();
          System.out.println("waiting for Player X response");
          line = dataInputStream.readUTF();
          System.out.println("Player X gave : " + line);
          if (processReturnedLine(line, false)) {
            closeEverything();
            break;
          }
        } else if (!firstOneChance && !iAmFirst) {
          displayCurrentMatrix();
          if (opponentWon) {
            closeEverything();
            break;
          }
          notifiedToWait = false;
          dataOutputStream.writeUTF("Player O provide your input: (ex : '2,4')" + "\n\r");
          dataOutputStream.flush();
          System.out.println("waiting for Player O response");
          line = dataInputStream.readUTF();
          System.out.println("Player O gave : " + line);
          if (processReturnedLine(line, true)) {
            closeEverything();
            break;
          }
        } else {
          if (!notifiedToWait) {
            dataOutputStream.writeUTF("Please wait until other player make his/her move" + "\n\r");
            dataOutputStream.flush();
            notifiedToWait = true;
          }
        }

      }
    } catch (IOException | InterruptedException e) {
      try {
        if (myPair != null) {
          myPair.interrupt();
        }
        closeEverything();
      } catch (IOException ex) {
      }

    } finally {
      if(iAmFirst){
        lastDied[0] = true;
      }
      else{
        lastDied[1] = true;
      }
      if(lastDied[0] && lastDied[1]){
        matrix = new int[6][6];
        firstOneChance = true;
        opponentWon = false;
        isGameOn = false;
        lastDied = new boolean[]{false, false};
      }
      if (iAmFirst) {
        System.out.println("Player X quit");
      } else {
        System.out.println("Player O quit");
      }
    }

  }

  private boolean paintX(int x, int y) {
    if(matrix[x][y] != 0){
      throw new IllegalStateException();
    }
    matrix[x][y] = 1;
    return checkTheWinner() != 0;
  }

  private boolean paintO(int x, int y) {
    if(matrix[x][y] != 0){
      throw new IllegalStateException();
    }
    matrix[x][y] = 2;
    return checkTheWinner() != 0;
  }

  private int checkTheWinner() {
    for (int i = 0; i < 6; i++) {
      for (int j = 0; j < 6; j++) {
        try {
          if (matrix[i][j] == matrix[i][j + 1] && matrix[i][j + 1] == matrix[i][j + 2]) {
            if (matrix[i][j] != 0) {
              return matrix[i][j];
            }
          }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
          if (matrix[i][j] == matrix[i + 1][j + 1] && matrix[i + 1][j + 1] == matrix[i + 2][j + 2]) {
            if (matrix[i][j] != 0) {
              return matrix[i][j];
            }
          }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
          if (matrix[i][j] == matrix[i + 1][j] && matrix[i + 1][j] == matrix[i + 2][j]) {
            if (matrix[i][j] != 0) {
              return matrix[i][j];
            }
          }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        try {
          if (matrix[i][j] == matrix[i - 1][j + 1] && matrix[i - 1][j + 1] == matrix[i - 2][j + 2]) {
            if (matrix[i][j] != 0) {
              return matrix[i][j];
            }
          }
        } catch (ArrayIndexOutOfBoundsException e) {

        }
      }
    }
    return 0;
  }

  private void closeEverything() throws IOException {
    socket.close();
    dataInputStream.close();
    dataOutputStream.close();
    setGameOn(false);
  }

  public void iLost() throws IOException {
    dataOutputStream.writeUTF("You lost the game" + "\n\r");
    dataOutputStream.flush();
  }

  private String getMatrixRepr() {
    StringBuilder stringRepr = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      for (int j = 0; j < 6; j++) {
        stringRepr.append(matrix[i][j] == 0 ? "-" : (matrix[i][j] == 1 ? "X" : "O"));
        stringRepr.append(" ");
      }
      stringRepr.append("split");
    }
    return stringRepr.toString();
  }

  public boolean processReturnedLine(String line, boolean isFirstOneChance) throws IOException {
    String[] position = line.split(" ");
    try {
      int first = Integer.parseInt(position[0]);
      int second = Integer.parseInt(position[1]);
      if (first > 5 || second > 5) {
        throw new IllegalStateException();
      }
      boolean won;
      if (!isFirstOneChance) {
        won = paintX(first, second);
      } else {
        won = paintO(first, second);
      }
      if (won) {

        dataOutputStream.writeUTF(getMatrixRepr() + "\n\r");
        dataOutputStream.flush();
        dataOutputStream.writeUTF("You won the game" + "\n\r");
        dataOutputStream.flush();
        opponentWon = true;
        firstOneChance = isFirstOneChance;
        return true;
      } else {
        dataOutputStream.writeUTF(getMatrixRepr() + "\n\r");
        dataOutputStream.flush();
      }
      firstOneChance = isFirstOneChance;
    } catch (Exception e) {
      dataOutputStream.writeUTF("Please give valid inputs ex : '4 3' and don't try to over write the matrix" + "\n\r");
      dataOutputStream.flush();
      System.out.println("error in the response : " + e);
    }
    return false;
  }

  public void displayCurrentMatrix() throws IOException {
    dataOutputStream.writeUTF("Updated Matrix : " + "\n\r");
    dataOutputStream.writeUTF(getMatrixRepr() + "\n\r");
    dataOutputStream.flush();
    if (opponentWon) {
      iLost();
    }
  }
}
