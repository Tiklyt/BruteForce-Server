// code issu de sources disponibles depuis ici :
// https://www.site.uottawa.ca/school/research/lloseng/supportMaterial/ocsf/ocsf.html
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observable;

/**
 * The <code> AbstractClient </code> contains all the methods necessary to set
 * up the client side of a client-server architecture. When a client is thus
 * connected to the server, the two programs can then exchange <code> Object
 * </code> instances.<p>
 * <p>
 * Method <code> handleMessageFromServer </code> must be defined by a concrete
 * subclass. Several other hook methods may also be overriden.<p>
 * <p>
 * Several public service methods are provided to application that use this
 * framework.<p>
 * <p>
 * Project Name: OCSF (Object Client-Server Framework)
 * <p>
 *
 * @author Dr. Robert Lagani&egrave;re
 * @author Dr. Timothy C. Lethbridge
 * @author Fran&ccedil;ois B&eacutel;langer
 * @author Paul Holden
 * @version December 2003 (2.31)
 */
public abstract class AbstractClient extends Observable implements Runnable {

    /**
     * Sockets are used in the operating system as channels of communication
     * between two processes.
     *
     * @see java.net.Socket
     */
    private Socket clientSocket;

    /**
     * The stream to handle data going to the server.
     */
    private DataOutputStream output;

    /**
     * The stream to handle data from the server.
     */
    private DataInputStream input;

    /**
     * The thread created to read data from the server.
     */
    private Thread clientReader;

    /**
     * Indicates if the thread is ready to stop. Needed so that the loop in the
     * run method knows when to stop waiting for incoming messages.
     */
    private boolean readyToStop = false;

    /**
     * The server's host name.
     */
    private String host;

    /**
     * The port number.
     */
    private int port;

    /**
     * Constructs the client.
     *
     * @param host the server's host name.
     * @param port the port number.
     */
    public AbstractClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Closes all aspects of the connection to the server.
     *
     * @throws IOException if an I/O error occurs when closing.
     */
    private void closeAll() throws IOException {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }

            if (output != null) {
                output.close();
            }

            if (input != null) {
                input.close();
            }
        } finally {
            output = null;
            input = null;
            clientSocket = null;
        }
    }

    /**
     * @return the port number.
     */
    final public int getPort() {
        return port;
    }

    /**
     * Sets the server port number for the next connection. The change in port
     * only takes effect at the time of the next call to openConnection().
     *
     * @param port the port number.
     */
    final public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the host name.
     */
    final public String getHost() {
        return host;
    }

    /**
     * Sets the server host for the next connection. The change in host only
     * takes effect at the time of the next call to openConnection().
     *
     * @param host the host name.
     */
    final public void setHost(String host) {
        this.host = host;
    }

    /**
     * returns the client's description.
     *
     * @return the client's Inet address.
     */
    final public InetAddress getInetAddress() {
        return clientSocket.getInetAddress();
    }

    /**
     * @return true if the client is connected.
     */
    final public boolean isConnected() {
        return clientReader != null && clientReader.isAlive();
    }

    /**
     * Closes the connection to the server.
     *
     * @throws IOException if an I/O error occurs when closing.
     */
    final public void closeConnection() throws IOException {
        readyToStop = true;
        closeAll();
    }

    /**
     * Hook method called after the connection has been closed. The default
     * implementation does nothing. The method may be overridden by subclasses to
     * perform special processing such as cleaning up and terminating, or
     * attempting to reconnect.
     */
    protected void connectionClosed() {
    }

    /**
     * Hook method called each time an exception is thrown by the client's
     * thread that is reading messages from the server. The method may be
     * overridden by subclasses. Most exceptions will cause the end of the
     * reading thread except for <code>ClassNotFoundException</code>s received
     * when an object of unknown class is received and for the
     * <code>RuntimeException</code>s that can be thrown by the message handling
     * method implemented by the user.
     *
     * @param exception the exception raised.
     */
    protected void connectionException(Exception exception) {
    }

    /**
     * Hook method called after a connection has been established. The default
     * implementation does nothing. It may be overridden by subclasses to do
     * anything they wish.
     */
    protected void connectionEstablished() {
    }

    /**
     * Handles a message sent from the server to this client. This MUST be
     * implemented by subclasses, who should respond to message.
     *
     * @param length the message sent.
     */
    protected abstract void handleMessageFromServer(long length) throws IOException;

    /**
     * Opens the connection with the server. If the connection is already
     * opened, this call has no effect.
     *
     * @throws IOException if an I/O error occurs when opening.
     */
    final public void openConnection() throws IOException {
        // Do not do anything if the connection is already open
        if (isConnected()) {
            return;
        }
        try {
            clientSocket = new Socket(host, port);
            output = new DataOutputStream(clientSocket.getOutputStream());
            input = new DataInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            try {
                closeAll();
            } catch (Exception ignored) {
            }
            throw ex;
        }

        clientReader = new Thread(this);  //Create the data reader thread
        readyToStop = false;
        clientReader.start();
    }

    public void sendRequest(byte[] hashPwd, int pwdLength,
                            long fileLength) throws IOException {
        output.write(hashPwd, 0, 20);
        output.writeInt(pwdLength);
        output.writeLong(fileLength);
        output.flush();
    }

    public void sendFileToServer(InputStream fileInputStream) throws IOException {
        int readCount;
        byte[] buffer = new byte[64];
        //read from the file and send it in the socket
        while ((readCount = fileInputStream.read(buffer)) > 0) {
            output.write(buffer, 0, readCount);
        }
    }

    public void receiveFile(OutputStream fileOut, long fileLength) throws IOException {
        int readFromFile = 0;
        int bytesRead;
        byte[] readBuffer = new byte[64];
        while ((readFromFile < fileLength)) {
            bytesRead = input.read(readBuffer);
            readFromFile += bytesRead;
            fileOut.write(readBuffer, 0, bytesRead);
        }
        fileOut.close();
    }

    /**
     * Waits for messages from the server. When each arrives, a call is made to
     * <code>handleMessageFromServer()</code>. Not to be explicitly called.
     */
    @Override
    final public void run() {
        connectionEstablished();

        long msg;
        // Loop waiting for data
        try {
            while (!readyToStop) {
                // Get data from Server and send it to the handler
                // The thread waits indefinitely at the following
                // statement until something is received from the server
                try {
                    msg = input.readLong();
                    // Concrete subclasses do what they want with the
                    // msg by implementing the following method
                    if (!readyToStop) {
                        handleMessageFromServer(msg);
                    }

                } catch (RuntimeException ex) { // when an unknown class is received
                    connectionException(ex);
                } // thrown by handleMessageFromServer

            }
        } catch (Exception exception) {
            if (!readyToStop) {
                try {
                    closeAll();
                } catch (Exception ignored) {
                }
                clientReader = null;
                connectionException(exception);
            }
        } finally {
            clientReader = null;
            connectionClosed();
        }
    }

}
