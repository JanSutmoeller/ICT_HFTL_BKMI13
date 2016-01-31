package quantipig;

import android.util.Log;

import org.opencv.core.Mat;

/*
 * Tastet ein quadratisches Raster ab, ermittelt den den Durschnittlichen Wert der Kanäle und gibt diese für ddas Raster zurück
 * @param buff
 * @param mHeight
 * @param mWidth
 * @param channels
 * @param cluster
 * @return modifiziertes Array mit den geänderten Farbwerten
 */
public class Pixel {

    public static Mat quantize(Mat mat, int mHeight, int mWidth, int channels, int cluster) {
        byte[] buff = new byte[mHeight * mWidth * channels];
        mat.get(0, 0, buff);

        Log.d("Abmessung", "Höhe: " + Integer.toString(mHeight) + " | Breite: " + Integer.toString(mWidth) + " | Cluster: " + Integer.toString(cluster) + "Tiefe" + channels);
        int x = 0;																		            /* Index des abzutastenden Farbwertes aus dem Array buff[] */
        int k = 0;																		            /* Index des Bildpunktes für das Hilfsarray */
        int countY = 0;																	            /* Variable, die die abgetasteten Zeilen zählt */
        int[] frame = new int[mWidth / cluster * mHeight / cluster * channels];				        /* Hilsarray zur Berechnung des Durchschnittwerts */

        /*
         *  Befüllen des Hilsarrays frame mit:
         *
         *  k = Bildpunktindex des Hilfarrays
         *  i = RGBA-Kanal
         *  x = Index des Originals
         */

        for (int n = 0; n < mHeight; n++) {											            /* Erhöhung des Zeilenindexes */
            countY++;																	            /* Zähler für Zeilendurchläufe */

            /* Innerhalb der Zeile */
            for (int m = 0; m < mWidth / cluster; m++) {								            /* Jede Zeile wird Breite:Cluster-mal durchlaufen */
                for (int j = 0; j < cluster; j++) {									            /* j -> Zählvariable in Abhängigkeit von cluster, wieviele Werte miteinander addiert werden müssen */
                    for (int i = 0; i < channels; i++) {                                            /* i -> RGBA-Kanal (0=R, 1=G, 2=B, 3=A) */
                        if (buff[x + i + j * channels] >= 0)                                       /* Da signed Byte(-128 bis 127), muss geprüft werden ob der Wert mit negativem Vorueichen ist oder nicht */
                            frame[k + i] += ((int) buff[x + i + j * channels]);				    /* für >=0 kann der Wert einfach übernommen werden */
                        else
                            frame[k + i] += ((int) buff[x + i + j * channels] & 0xff);		        /* für < 0 wird der Wert umgerechnet, damit er positiv wird */
                    }
                }
                k += channels;																	    /* nächster Bildpunkt (+4, da RGBA) */
                x += channels * cluster;														    /* neuer abzutastender Wert, in Abhängigkeit vom Cluster */
            }
            /* Sprung zur nächsten Zeile */
            if (countY < cluster)														            /* Zeilendurchläufe müssen gezählt werden */
                k -= mWidth * channels / cluster;												    /* Wenn noch innerhalb des Clusters, muss k um eine Zeile zurückgesetzt werden */
            else
                countY = 0;																            /* neuer Zählzyklus */
        }
        /* Berechnen der Durchschnittswerte */
        for (int i = 0; i < frame.length; i++) {
            frame[i] = (byte) (frame[i] / (cluster * cluster));									    /* Summme (frame[i]) : Anzahl der Elemente (cluster*cluster) */
        }

        k = 0;																			            /* Rücksetzen der Variablen */
        countY = 0;
        x = 0;

        /*
        * Kopieren der Werte zurück in buff
        * Dabei werden die berechneten Int-Werte wieder zurück in Byte-Werte geschrieben
        */

        for (int n = 0; n < mHeight; n++) {
            countY++;

            /* Innerhalb der Zeile */
            for (int m = 0; m < mWidth / cluster; m++) {
                for (int j = 0; j < cluster; j++) {
                    for (int i = 0; i < channels; i++) {
                        buff[x + i + j * channels] = (byte) frame[k + i];
                    }
                }
                k += channels;
                x += channels * cluster;
            }
            /* Sprung zur nächste n Zeile */
            if (countY < cluster)
                k -= mWidth * channels / cluster;
            else
                countY = 0;
        }
        mat.put(0, 0, buff);
        return mat;
    }


}