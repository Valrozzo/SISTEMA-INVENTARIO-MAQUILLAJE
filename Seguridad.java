package sisim;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class Seguridad {
    private static final int ITERACIONES = 120000;
    public static String generarHash(String clave) throws Exception {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(derivar(clave, sal, ITERACIONES));
    }
    private static byte[] derivar(String clave, byte[] sal, int iteraciones) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
    public static boolean verificar(String clave, String hash) throws Exception {
        String[] partes = hash.split(":");
        if (partes.length != 3) return false;
        byte[] sal = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        return MessageDigest.isEqual(esperado, derivar(clave, sal, Integer.parseInt(partes[0])));
    }
}
