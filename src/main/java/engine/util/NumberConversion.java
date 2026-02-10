package engine.util;

import engine.state.MyByte;

/**
 * Diese Klasse beihnhaltet alle benoetigten Umrechnungen
 */
public class NumberConversion {

    /**
     * Addiert zwei Arrays of MyByte, wenn Sie die gleiche Länge haben
     *
     * @param op1 Array1
     * @param op2 Array2
     * @return Ergebnis wieder als Array of MyByte
     */
    public static MyByte[] add(MyByte[] op1, MyByte[] op2) {
        if (op1.length != op2.length) {
            return null;
        }
        boolean carry = false;
        MyByte[] ret = new MyByte[op1.length];
        for (int i = op1.length - 1; i >= 0; i--) {
            ret[i] = carry ?
                    new MyByte(op1[i].getContent() + op2[i].getContent() + 1) :
                    new MyByte(op1[i].getContent() + op2[i].getContent());
            carry = ret[i].getCarry();
        }
        return ret;
    }

    /**
     * Konvertiert einen Binaerwert zu einem MyByte-Array
     *
     * @param bin    Binaerwert
     * @param length Laenge des Arrays
     * @return Ergebnis als MyByte-Array
     */
    public static MyByte[] binToByte(String bin, int length) {
        int zahl = Integer.valueOf(bin, 2);
        return intToByte(zahl, length);
    }

    /**
     * Bildet das Komplement eines MyByte-Array
     *
     * @param in MyByte-Array
     * @return Komplementdarstellung als MyByte-Array
     */
    public static MyByte[] complement(MyByte[] in) {
        MyByte[] ret = new MyByte[in.length];
        for (int i = 0; i < in.length; i++) {
            ret[i] = in[i].negate();
        }
        return ret;
    }

    /**
     * Konvertiert einen Hexwert zu einem MyByteArray
     *
     * @param hex    Hexwert
     * @param length Laenge des Arrays
     * @return Ergebnis als MyByte-Array
     */
    public static MyByte[] hexToByte(String hex, int length) {
        int zahl = Long.valueOf(hex, 16).intValue();
        return intToByte(zahl, length);
    }

    /**
     * Konvertiert einen Integerwert zu einem MyByteArray
     *
     * @param in     Integerwert
     * @param length Laenge des Arrays
     * @return Ergebnis als MyByte-Array
     */
    public static MyByte[] intToByte(int in, int length) {

        MyByte[] ret = new MyByte[length];
        boolean neg = false;
        if (in < 0) {
            in = -in;
            neg = true;
        }

        if (neg) {
            String neu = negateBinary(Integer.toBinaryString(in), 8 * length);
            for (int i = 0; i < length; i++) {
                ret[i] = new MyByte(
                        Integer.parseInt(neu.substring(i * 8, i * 8 + 8), 2));
            }
        } else {
            for (int i = 0; i < length; i++) {
                ret[length - i - 1] = new MyByte(in % 256);
                in /= 256;
            }
        }
        return ret;
    }

    /**
     * Konvertiert einen Longwert zu einem MyByteArray
     *
     * @param in     Longwert
     * @param length Laenge des Arrays
     * @return Ergebnis als MyByte-Array
     */
    public static MyByte[] longToByte(long in, int length) {
        MyByte[] ret = new MyByte[length];
        boolean neg = false;

        String neu = Long.toBinaryString(in);
        int merke = length * 8 - neu.length();
        for (int i = 0; i < merke; i++)
            neu = "0" + neu;
        for (int i = 0; i < length; i++) {
            ret[i] = new MyByte(Integer.parseInt(neu.substring(i * 8, i * 8 + 8), 2));
        }

        return ret;
    }

    /**
     * Vergleicht zwei MyByte-Arrays
     *
     * @param one Array 1
     * @param two Array 2
     * @return true, if successful
     */
    public static boolean myByteEqual(MyByte[] one, MyByte[] two) {
        if (one.length != two.length) {
            return false;
        }

        for (int i = 0; i < one.length; i++) {
            if (!one[i].hasSameContent(two[i])) {
                return false;
            }

        }
        return true;
    }

    /**
     * Konvertiert ein MyByte-Array in einen Binärstring
     *
     * @param in MyByte-Array
     * @return Binärstring
     */
    public static String myBytetoBin(MyByte[] in) {
        String ret = "";
        for (MyByte by : in) {
            String bin = Integer.toBinaryString((by.getContent()));
            ret += "00000000".substring(0, 8 - bin.length()) + bin;
        }
        ret = ret.indexOf("1") == -1 ? "" : ret.substring(ret.indexOf("1"));
        return ret.isEmpty() ? "0" : ret;
    }

    /**
     * Konvertiert ein MyByte-Array in einen Hexstring
     *
     * @param in MyByte-Array
     * @return Hexstring
     */
    public static String myBytetoHex(MyByte[] in) {
        String ret = "";
        for (MyByte by : in) {
            String bin = Integer.toHexString(by.getContent()).toUpperCase();
            ret += "00".substring(0, 2 - bin.length()) + bin;
        }
        return ret;
    }

    /**
     * Konvertiert ein MyBytearray zu einem Integer ohne Vorzeichen
     *
     * @param in MyBytearray
     * @return Ergebnis als Integer
     */
    public static int myBytetoIntWithoutSign(MyByte[] in) {
        int ret = 0;
        for (MyByte element : in) {
            ret = ret * 256 + element.getContent();

        }
        return ret;
    }

    /**
     * Konvertiert ein MyBytearray zu einem Integer mit Vorzeichen
     *
     * @param in MyBytearray
     * @return Ergebnis als Integer
     */
    public static int myBytetoIntWithSign(MyByte[] in) {
        MyByte[] ret = new MyByte[in.length];
        boolean carry = true;
        boolean neg = false;
        for (int i = in.length - 1; i >= 0; i--) {
            ret[i] = new MyByte(in[i].getContent());
        }

        if (in[0].getContent() > 127) {
            neg = true;
            for (int i = in.length - 1; i >= 0; i--) {
                ret[i] = carry ?
                        new MyByte(in[i].negate().getContent() + 1) :
                        new MyByte(in[i].negate().getContent());
                carry = ret[i].getCarry();

            }
        }
        return neg ? (-1 * myBytetoIntWithoutSign(ret)) : myBytetoIntWithoutSign(ret);
    }

    /**
     * Konvertiert ein MyBytearray zu einem Long ohne Vorzeichen
     *
     * @param in MyBytearray
     * @return Ergebnis als Long
     */
    public static long myBytetoLongWithoutSign(MyByte[] in) {
        long ret = 0L;
        for (MyByte element : in) {
            ret = ret * 256 + new Long(element.getContent());

        }
        return ret;
    }

    /**
     * Konvertiert ein MyBytearray zu einem Long mit Vorzeichen
     *
     * @param in MyBytearray
     * @return Ergebnis als Long
     */
    public static long myBytetoLongWithSign(MyByte[] in) {
        MyByte[] ret = new MyByte[in.length];
        boolean carry = true;
        boolean neg = false;
        for (int i = in.length - 1; i >= 0; i--) {
            ret[i] = new MyByte(in[i].getContent());
        }

        if (in[0].getContent() > 127) {
            neg = true;
            for (int i = in.length - 1; i >= 0; i--) {
                ret[i] = carry ?
                        new MyByte(in[i].negate().getContent() + 1) :
                        new MyByte(in[i].negate().getContent());
                carry = ret[i].getCarry();

            }
        }
        return neg ? (-1 * myBytetoLongWithoutSign(ret)) : myBytetoLongWithoutSign(ret);
    }

    /**
     * Hegiert einen Binaerstring
     *
     * @param in     Binaerstring
     * @param length Länge des Ergebnis Strings
     * @return Binaerstring
     */
    public static String negateBinary(String in, int length) {
        String neu = "";
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
                case '0':
                    neu += "1";
                    break;
                case '1':
                    neu += "0";
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid binary character: '" + c + "' in string: " + in);
            }

        }
        String pre = "";
        for (int i = 0; i < length - in.length(); i++) {
            pre += "1";
        }
        neu = pre + neu;
        boolean carry = true;
        String erg = "";
        for (int i = (neu.length() - 1); i >= 0; i--) {
            char ch = neu.charAt(i);
            switch (ch) {
                case '0':
                    if (carry) {
                        erg = "1" + erg;
                    } else {
                        erg = "0" + erg;
                    }
                    carry = false;
                    break;
                case '1':
                    if (carry) {
                        erg = "0" + erg;
                        carry = true;
                    } else {
                        erg = "1" + erg;
                        carry = false;
                    }

                    break;
                default:
                    throw new IllegalArgumentException(
                            "Invalid binary character: '" + ch + "' in string: " + neu);
            }
        }

        return erg;
    }

    /**
     * Prueft ob sich eine Zhl im gültigen Zahlenraum befindet
     *
     * @param number zu pruefende Zahl
     * @param length Zahlenraum (0: byte, 2; short, 4: integer)
     * @return gueltig
     */
    public static boolean valid_number(long number, int length) {
        switch (length) {
            case 1:
                if (number < -128 || number > 255)
                    return false;
                break;
            case 2:
                if (number < -32768 || number > 65535)
                    return false;
                break;
            case 4:
                if (number < -2147483648 || number > 4294967295L)
                    return false;
                break;
            default:
                return false;
        }
        return true;

    }

    /**
     * Prüft, ob ein Wert im gültigen vorzeichenbehafteten Bereich liegt.
     *
     * @param number der Wert
     * @param length die Länge in Bytes (1, 2, 4)
     * @return true, wenn der Wert im Bereich liegt
     */
    public static boolean isValidSigned(long number, int length) {
        switch (length) {
            case 1:
                return number >= -128 && number <= 127;
            case 2:
                return number >= -32768 && number <= 32767;
            case 4:
                return number >= -2147483648 && number <= 2147483647;
            default:
                return false;
        }
    }

}
