import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BruteForceClient extends AbstractClient {


    public BruteForceClient(String host, int port) throws IOException {

        super(host, port);
        openConnection();
    }

    @Override
    protected void handleMessageFromServer(long length) throws IOException {
        File decryptedClient = new File("result.zip");
        OutputStream outFile = new FileOutputStream(decryptedClient);
        super.receiveFile(outFile, length);
        System.out.println("File transferred");
    }
}