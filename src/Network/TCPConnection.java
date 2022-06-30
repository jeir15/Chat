package Network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {

    private final Socket socket;

    private final Thread rxthread;

    private final TCPConnectionListener tcpConnectionListener;

    private final BufferedReader in;

    private final BufferedWriter out;


    public TCPConnection(TCPConnectionListener tcpConnectionListener, String ipAddr, int port) throws  IOException{
        this(tcpConnectionListener, new Socket(ipAddr, port));
    }

    public TCPConnection(TCPConnectionListener tcpConnectionListener, Socket socket ) throws IOException {
        this.tcpConnectionListener =tcpConnectionListener;
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        rxthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    tcpConnectionListener.onConnectionReady(TCPConnection.this);
                    while(!rxthread.isInterrupted()){
                        tcpConnectionListener.onReceiveString(TCPConnection.this,in.readLine());
                    }
                }
                catch (IOException e){
                    tcpConnectionListener.onException(TCPConnection.this,e);
                }
                finally {
                    tcpConnectionListener.onDisconnect(TCPConnection.this);
                }


            }
        });
        rxthread.start();

    }


    public synchronized void sendString(String message){
        try {
            out.write(message + "\r\n");
            out.flush();
        } catch (IOException e) {
            tcpConnectionListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    public synchronized void disconnect(){
        rxthread.interrupt();

        try {
            socket.close();
        } catch (IOException e) {
            tcpConnectionListener.onException(TCPConnection.this, e);
        }
    }
    @Override
    public String toString(){
        return "TCPConnection " + socket.getInetAddress() + ": " + socket.getPort();
    }




}
