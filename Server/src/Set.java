public class Set {

    private final String password;
    private final String encryptedPassword;

    public Set(String password, String encryptedPassword) {
        if (password == null || encryptedPassword == null) {
            throw new NullPointerException("parameters is null.");
        }
        this.password = password;
        this.encryptedPassword = encryptedPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}
