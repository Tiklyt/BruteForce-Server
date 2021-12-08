import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class ClientMain {

    public static byte[] hashSHA1(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(data.getBytes());
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException,
            InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, InvalidKeyException {
        String password = "password";
        File inputFile = new File("Files-20KB.zip");
        File encryptedFile = new File("crypted.zip");

        BruteForceClient client = new BruteForceClient("192.168.44.128", 12_345);
        SecretKey k = CryptoUtils.getKeyFromPassword(password);
        CryptoUtils.encryptFile(k, inputFile, encryptedFile);
        client.openConnection();
        client.sendRequest(hashSHA1(password), password.length(), encryptedFile.length());
        client.sendFileToServer(new FileInputStream(encryptedFile));
        System.out.println("Hello World");
        //client.closeConnection();
    }
}
