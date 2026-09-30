package infra;

import service.PasswordHasher;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasherBCrypt implements PasswordHasher {
    private int workFactor = 12;

    @Override
    public String hash(String passwordText) {
        return  BCrypt.hashpw(passwordText, BCrypt.gensalt(workFactor));
    }

    @Override
    public boolean matches(String passwordText, String passwordHash) {
        return BCrypt.checkpw(passwordText, passwordHash);
    }
}
