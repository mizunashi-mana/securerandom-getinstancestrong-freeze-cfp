import javax.crypto.SecretKey;

public class Main {
    public static void main(String args[]) {
        final SecretKey key = DataCryptor.generateKey();
        final DataCryptor dataCryptor = new DataCryptor(key);
        final ApiService service = new ApiService(dataCryptor);

        final Thread registerThread = new Thread(() -> {
            service.registerData("test", "something");
            service.readData("test");
            service.readData("unknown");
        });

        registerThread.start();
    }
}
