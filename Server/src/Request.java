public class Request {

    private final byte[] hashPwd;
    private final int pwdLength;
    private final long fileLength;

    public Request(byte[] hashPwd, int pwdLength, long fileLength) {
        this.hashPwd = hashPwd;
        this.pwdLength = pwdLength;
        this.fileLength = fileLength;
    }

    public byte[] getHashPassword() {
        return hashPwd;
    }


    public int getLengthPwd() {
        return pwdLength;
    }

    public long getLengthFile() {
        return fileLength;
    }
}
