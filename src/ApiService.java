import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

public class ApiService {
    private final DataCryptor dataCryptor;
    private final HashMap<String, byte[]> store = new HashMap<>();

    ApiService(DataCryptor dataCryptor) {
        this.dataCryptor = dataCryptor;
    }

    public void registerData(String id, String data) {
        final byte[] encryptedData = dataCryptor.encrypt(data.getBytes(StandardCharsets.UTF_8));
        store.put(id, encryptedData);
    }

    public void readData(String id) {
        final Optional<byte[]> encryptedDataOpt = Optional.ofNullable(store.get(id));
        final Optional<String> decryptedDataOpt = encryptedDataOpt
            .flatMap(encryptedData -> {
                return dataCryptor.decrypt(encryptedData);
            })
            .map(decryptedDataBytes -> {
                return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(decryptedDataBytes)).toString();
            })
            ;

        if (decryptedDataOpt.isPresent()) {
            System.out.println(id + ": " + decryptedDataOpt.get());
        } else {
            System.out.println("Not found for " + id + ".");
        }
    }
}
