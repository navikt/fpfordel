package no.nav.foreldrepenger.fordel.web.app.util;

public class FoedselsnummerUtil {

    private FoedselsnummerUtil() {}

    public static boolean gyldigFoedselsnummer(String foedselsnummer) {
        if(foedselsnummer.length() != 11) {
            return false;
        }
        int[] faktors;
        faktors = new int[]{3, 7, 6, 1, 8, 9, 4, 5, 2};
        int checksumEn = 11 - (sum(foedselsnummer, faktors) % 11);
        if (checksumEn == 11) {
            checksumEn = 0;
        }
        faktors = new int[]{5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int checksumTo = 11 - (sum(foedselsnummer, faktors) % 11);
        if (checksumTo == 11) {
            checksumTo = 0;
        }
        return checksumEn == Integer.parseInt(String.valueOf(foedselsnummer.charAt(9)), 10)
                && checksumTo == Integer.parseInt(String.valueOf(foedselsnummer.charAt(10)), 10);
    }

    private static int sum(String foedselsnummer, int... faktors) {
        int sum = 0;
        for(int i = 0, l = faktors.length; i < l; ++i){
            sum += Integer.parseInt(String.valueOf(foedselsnummer.charAt(i)),10) * faktors[i];
        }
        return sum;
    }
}
