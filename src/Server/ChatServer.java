package Server;

import Network.TCPConnection;
import Network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements TCPConnectionListener {

    public static void main(String[] args) {
        new ChatServer();
    }

    private final List<TCPConnection> connections = new ArrayList<>();


    public ChatServer(){
        System.out.println("Server running...");

      try{
          ServerSocket serverSocket = new ServerSocket(8080);
          while(true){
              try{
                  new TCPConnection(this,serverSocket.accept());
              }
              catch (IOException exception){
                  System.out.println("TCPConnection " + exception);
              }
          }
      } catch (IOException e){
          throw  new RuntimeException(e);
      }

    }

    private void sendToAllConnection(String value){
        System.out.println(value);
        final int count = connections.size();
        for(int i = 0; i<count;i++){
            connections.get(i).sendString(value);
        }
    }




    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        connections.add(tcpConnection);
        sendToAllConnection("Client connection: " + tcpConnection);
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String string) {
        sendToAllConnection(string);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(tcpConnection);
        sendToAllConnection("Client disconnect: " + tcpConnection);
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPConnection exception: " + e);
    }
}
