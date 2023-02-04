import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

public class DataCryptor {
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_BIT_LENGTH = 256;
    private static final int GCM_NONCE_BYTE_LENGTH = 12;
    private static final int GCM_TAG_BIT_LENGTH = 128;

    public static final byte[] generateKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unreachable: expect AES support", e);
        }
        keyGenerator.init(KEY_BIT_LENGTH);
        return keyGenerator.generateKey().getEncoded();
    }
    
    private final ThreadLocal<Cipher> cipherThreadLocal = new ThreadLocal<>() {
        protected Cipher initialValue() {
            try {
                return Cipher.getInstance(CIPHER_ALGORITHM);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("unreachable: expect AES/GCM/NoPadding support", e);
            }
        }
    };

    private final ThreadLocal<byte[]> gcmNonceBytesThreadLocal = new ThreadLocal<>() {
        protected byte[] initialValue() {
            return new byte[12];
        }
    };

    private final SecretKeySpec keySpec;
    private final SecureRandom random;

    DataCryptor(byte[] key) {
        assert key.length * 8 == KEY_BIT_LENGTH;

        this.keySpec = new SecretKeySpec(key, KEY_ALGORITHM);
        try {
            this.random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unreachable: expect SecureRandom strong algorithm support", e);
        }
    }

    public byte[] encrypt(byte[] plain) {
        final Cipher cipher = cipherThreadLocal.get();
        final byte[] nonce = gcmNonceBytesThreadLocal.get();
        this.random.nextBytes(nonce);

        final GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, nonce);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, this.keySpec, gcmParameterSpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Illegal encryption key format", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("unreachable: expect AES/GCM/NoPadding support", e);
        }

        final byte[] cryptedBody;
        try {
            cryptedBody = cipher.doFinal(plain);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException("unreachable: AES/GCM/NoPadding support any block sizes.", e);
        }

        final byte[] crypted = new byte[1 + nonce.length + cryptedBody.length];
        crypted[0] = 1;
        System.arraycopy(nonce, 0, crypted, 1, nonce.length);
        System.arraycopy(cryptedBody, 0, crypted, 1 + nonce.length, cryptedBody.length);
            
        return crypted;
    }

    public Optional<byte[]> decrypt(byte[] crypted) {
        if (crypted.length < 1 + GCM_NONCE_BYTE_LENGTH || crypted[0] != 1) {
            return Optional.empty();
        }

        final Cipher cipher = cipherThreadLocal.get();
        final byte[] nonce = gcmNonceBytesThreadLocal.get();
        System.arraycopy(crypted, 1, nonce, 0, GCM_NONCE_BYTE_LENGTH);

        final GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_BIT_LENGTH, nonce);

        try {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Illegal encryption key format", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("unreachable: expect AES/GCM/NoPadding support", e);
        }

        final int cryptedBodyLength = crypted.length - 1 - GCM_NONCE_BYTE_LENGTH;
        try {
            return Optional.of(cipher.doFinal(crypted, 1 + GCM_NONCE_BYTE_LENGTH, cryptedBodyLength));
        } catch (AEADBadTagException e) {
            return Optional.empty(); 
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("unreachable: AES/GCM/NoPadding support any block sizes.", e);
        }
    }
}
