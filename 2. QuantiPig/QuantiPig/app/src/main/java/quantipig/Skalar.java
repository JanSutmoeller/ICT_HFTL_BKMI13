package quantipig;

import org.opencv.core.Mat;

/*
 * Alle Werte werden mittels Shift-Funktionen bitweise manipuliert
 * @param buff
 * @param mHeight
 * @param mWidth
 * @param chan
 * @param cluster
 * @return modifizierte Bildmatrix
 */
public class Skalar {

    public static Mat skalar(Mat mat, int mHeight, int mWidth, int channels, int intervall) {
        byte[] buff = new byte[mHeight * mWidth * channels];                                           // Erstellen des Byte-Arrays
        mat.get(0, 0, buff);                                                                      // Befüllen des Byte-Arrays
        int bitshift = 0xFFFFFFFF;                                                                  // Initialisierung des Bitshiftbektors
        bitshift = bitshift << intervall;                                                             // Die Einsen werden um cluster-Stellen verschoben
        int t;
        for (int i = 0; i < mWidth * mHeight; i++) {
            t = i * channels;
            buff[t] = (byte) (buff[t] & bitshift);                                             // Rot
            buff[t + 1] = (byte) (buff[t + 1] & bitshift);                                    // Gelb
            buff[t + 2] = (byte) (buff[t + 2] & bitshift);                                    // Blau
        }
        mat.put(0, 0, buff);                                                                      // Schreiben des Byte-Arrays zurück in Mat
        return mat;                                                                                 // Rückgabe von mat
    }
}
