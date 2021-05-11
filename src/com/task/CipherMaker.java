package com.task;

import com.task.exceptions.CipherException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class CipherMaker {
    private static final byte[] SALT = {-109, 28, 86, -92, 33, -90, 88, -104};
    private static final PBEParameterSpec PARAMETER_SPEC = new PBEParameterSpec(SALT, 20);
    private static final String KEY_ALGORITHM = "PBEWithMD5AndDES";
    private static final String CIPHER_ALGORITHM = "PBEWithMD5AndDES/CBC/PKCS5Padding";

    private final SecretKey key;

    public CipherMaker(String password) throws CipherException {
        key = generateKey(password);
    }

    protected CipherInputStream inputStream(FileInputStream fileIn) throws CipherException {
        return new CipherInputStream(fileIn, cipher(DECRYPT_MODE));
    }

    protected CipherOutputStream outputStream(OutputStream out) throws CipherException {
        return new CipherOutputStream(out, cipher(ENCRYPT_MODE));
    }

    private Cipher cipher(int mode) throws CipherException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(mode, key, PARAMETER_SPEC);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            throw new CipherException(ex);
        }
        return cipher;
    }

    private SecretKey generateKey(String password) throws CipherException {
        SecretKeyFactory kf;
        try {
            kf = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            return kf.generateSecret(new PBEKeySpec(password.toCharArray()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new CipherException(ex);
        }
    }
}
