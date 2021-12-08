import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class BruteForcer {
    private final HashMap<String, Set> bruteForceTable = new HashMap<>();

    public BruteForcer() {
        readTableFromFile();
    }

    public String crackPassword(String hashedKey) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";
    }

    private void readTableFromFile() {
        File f = new File("BruteForceTable.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] tableLine = line.split(" ");
                bruteForceTable.put(tableLine[2], new Set(tableLine[0], tableLine[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
