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

    public static Mat skalar(Mat mat, int mHeight, int mWidth, int chan, int cluster) {

        int bitshift = 0xFFFFFF00;                                                                  // Initialisierung des Bitshiftbektors
        bitshift = bitshift >> cluster;                                                             // Die Einsen werden um cluster-Stellen verschoben
        byte[] buff = new byte[mHeight * mWidth * chan];                                           // Erstellen des Byte-Arrays
        mat.get(0, 0, buff);                                                                      // Befüllen des Byte-Arrays
        int t;
        for (int i = 0; i < mWidth * mHeight; i++) {
            t = i * chan;
            buff[t] = (byte) (buff[t] & bitshift);                                    // Rot
            buff[t + 1] = (byte) (buff[t + 1] & bitshift);                                    // Gelb
            buff[t + 2] = (byte) (buff[t + 2] & bitshift);                                    // Grün
        }
        mat.put(0, 0, buff);                                                                      // Schreiben des Byte-Arrays zurück in Mat
        return mat;                                                                                 // Rückgabe von mat
    }
}
