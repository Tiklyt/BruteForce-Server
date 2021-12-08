import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * @author braro
 */
public class ConnectionToClient extends Thread {

    /**
     * A reference to the Server that created this instance.
     */
    private final AbstractServer server;
    /**
     * Sockets are used in the operating system as channels of communication
     * between two processes.
     *
     * @see java.net.Socket
     */
    private Socket clientSocket;


    private InputStream inputStream;

    private OutputStream outputStream;

    /**
     * Stream used to read from the client.
     */
    private final DataInputStream dataInputStream;

    /**
     * Stream used to write to the client.
     */
    private final DataOutputStream dataOutputStream;


    /**
     * Indicates if the thread is ready to stop. Set to true when closing of the
     * connection is initiated.
     */
    private boolean readyToStop;

    /**
     * Map to save information about the client such as its login ID. The
     * initial size of the map is small since it is not expected that concrete
     * servers will want to store many types of information about each
     * client. Used by the setInfo and getInfo methods.
     */
    private HashMap<String, Object> savedInfo = new HashMap<>(10);

    /**
     * Constructs a new connection to a client.
     *
     * @param clientSocket contains the client's socket.
     * @param server       a reference to the server that created this instance
     * @throws IOException if an I/O error occur when creating the
     *                     connection.
     */
    protected ConnectionToClient(Socket clientSocket,
                                 AbstractServer server) throws IOException {
        this.clientSocket = clientSocket;
        this.server = server;
        clientSocket.setSoTimeout(0); // make sure timeout is infinite
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
        readyToStop = false;
        start(); // Start the thread waits for data from the socket
    }

    public static Request readRequest(DataInputStream in) throws IOException {
        byte[] hashPwd = new byte[20];
        int count = in.read(hashPwd, 0, 20);
        if (count < 0) {
            throw new IOException("Server could not read from the stream");
        }
        int pwdLength = in.readInt();
        long fileLength = in.readLong();

        return new Request(hashPwd, pwdLength, fileLength);
    }

    /**
     * Closes all connection to the server.
     *
     * @throws IOException if an I/O error occur when closing the connection.
     */
    private void closeAll() throws IOException {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } finally {
            outputStream = null;
            inputStream = null;
            clientSocket = null;
        }
    }

    /**
     * Returns the address of the client.
     *
     * @return the client's Internet address.
     */
    final public InetAddress getInetAddress() {
        return clientSocket == null ? null : clientSocket.getInetAddress();
    }

    /**
     * Saves arbitrary information about this client. Designed to be used by
     * concrete subclasses of AbstractServer. Based on a hash map.
     *
     * @param infoType identifies the type of information
     * @param info     the information itself.
     */
    public void setInfo(String infoType, Object info) {
        savedInfo.put(infoType, info);
    }

    /**
     * Returns information about the client saved using setInfo. Based on a hash
     * map.
     *
     * @param infoType identifies the type of information
     */
    public Object getInfo(String infoType) {
        return savedInfo.get(infoType);
    }

    /**
     * Closes the client. If the connection is already closed, this call has no
     * effect.
     *
     * @throws IOException if an error occurs when closing the socket.
     */
    final public void close() throws IOException {
        readyToStop = true; // Set the flag that tells the thread to stop
        closeAll();
    }

    /**
     * Hook method called each time a new message is received by this client. If
     * this method return true, then the method
     * <code>handleMessageFromClient()</code> of <code>AbstractServer</code>
     * will also be called after. The default implementation simply returns
     * true.
     *
     * @param message the message sent.
     */
    protected boolean handleMessageFromClient(Object message) {
        return true;
    }

    /**
     * Sends an object to the client. This method can be overriden, but if so it
     * should still perform the general function of sending to client, by
     * calling the <code>super.sendToClient()</code> method perhaps after some
     * kind of filtering is done.
     *
     * @param fileToSend the message to be sent.
     * @throws IOException if an I/O error occur when sending the message.
     */
    public void sendFileToClient(InputStream fileToSend) throws IOException {
        int readCount;
        byte[] buffer = new byte[64];
        //read from the file and send it in the socket
        while ((readCount = fileToSend.read(buffer)) > 0) {
            dataOutputStream.write(buffer, 0, readCount);
        }
    }

    public void sendFileLength(long length) throws IOException {
        dataOutputStream.writeLong(length);
    }

    public void receiveFile(OutputStream fileOut, long fileLength) throws IOException {
        int readFromFile = 0;
        int bytesRead;
        byte[] readBuffer = new byte[64];
        while ((readFromFile < fileLength)) {
            bytesRead = inputStream.read(readBuffer);
            readFromFile += bytesRead;
            fileOut.write(readBuffer, 0, bytesRead);
        }
    }

    /**
     * Return true if the client is connected.
     *
     * @return true if the client is connected.
     */
    final public boolean isConnected() {
        return clientSocket != null && outputStream != null;
    }

    /**
     * Constantly reads the client's input stream. Sends all objects that are
     * read to the server. Not to be called.
     */
    @Override
    final public void run() {
        this.server.clientConnected(this);

        try {
            while (!this.readyToStop) {
                try {
                    Request request = readRequest(dataInputStream);
                    if (!this.readyToStop && this.handleMessageFromClient(request)) {
                        this.server.handleMessageFromClient(request, this);
                    }
                } catch (RuntimeException var11) {
                    this.server.clientException(this, var11);
                }
            }
        } catch (Exception var12) {
            if (!this.readyToStop) {
                try {
                    this.closeAll();
                } catch (Exception ignored) {
                }

                this.server.clientException(this, var12);
            }
        } finally {
            this.server.clientDisconnected(this);
        }
    }

    /**
     * Returns a string representation of the client.
     *
     * @return the client's description.
     */
    @Override
    public String toString() {
        return clientSocket == null ? null
                : clientSocket.getInetAddress().getHostName()
                + " (" + clientSocket.getInetAddress().getHostAddress() + ")";
    }

    /**
     * This method is called by garbage collection.
     */
    @Override
    protected void finalize() {
        try {
            closeAll();
        } catch (IOException ignored) {
        }
    }
}
