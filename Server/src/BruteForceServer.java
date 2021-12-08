import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class BruteForceServer extends AbstractServer {


    private final BruteForcer bruteForcer = new BruteForcer();

    /**
     * Constructs a new server.
     *
     * @param port the port number on which to listen.
     */
    public BruteForceServer(int port) throws IOException {

        super(port);
        this.listen();
    }

    private String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    /**
     * Quits the server and closes all aspects of the connection to clients.
     */
    public void quit() throws IOException {
        this.stopListening();
        this.close();
    }

    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client)
            throws NoSuchPaddingException, IllegalBlockSizeException, IOException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
            InvalidKeySpecException {       if (msg == null) {
        return;
    }
        Request request = (Request) msg;
        String hashedPass = byteArrayToHexString(request.getHashPassword());
        long length = request.getLengthFile();
        File decryptedFile = new File("decryptedsss.zip");
        File networkFile = new File("tempsss.zip");
        OutputStream writer = new FileOutputStream(networkFile);
        client.receiveFile(writer, length);
        String s = bruteForcer.crackPassword(hashedPass);
        if (s != null) {
            byte[] decodedKey = Base64.getDecoder().decode("AyADhyw7NdkLiOfrkE/oqA==");
            SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            CryptoUtils.decryptFile(key, networkFile, decryptedFile);
            InputStream decrypted = new FileInputStream(decryptedFile);
            client.sendFileLength(decryptedFile.length());
            client.sendFileToClient(decrypted);

        }
    }
}
