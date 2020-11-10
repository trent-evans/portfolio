import java.math.BigInteger;
import java.security.SecureRandom;

public class SecretGenerator {

    public static BigInteger generateNewSecret(){
        return new BigInteger(2048,1,new SecureRandom());
    }
}
