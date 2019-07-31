package com.multicraft.game;

class Transliteration {

    private static final String[] CHAR_TABLE = new String[81];
    private static final char START_CHAR = 'Ё';


    static {
        CHAR_TABLE['А' - START_CHAR] = "A";
        CHAR_TABLE['Б' - START_CHAR] = "B";
        CHAR_TABLE['В' - START_CHAR] = "V";
        CHAR_TABLE['Г' - START_CHAR] = "G";
        CHAR_TABLE['Д' - START_CHAR] = "D";
        CHAR_TABLE['Е' - START_CHAR] = "E";
        CHAR_TABLE['Ё' - START_CHAR] = "E";
        CHAR_TABLE['Ж' - START_CHAR] = "ZH";
        CHAR_TABLE['З' - START_CHAR] = "Z";
        CHAR_TABLE['И' - START_CHAR] = "I";
        CHAR_TABLE['Й' - START_CHAR] = "J";
        CHAR_TABLE['К' - START_CHAR] = "K";
        CHAR_TABLE['Л' - START_CHAR] = "L";
        CHAR_TABLE['М' - START_CHAR] = "M";
        CHAR_TABLE['Н' - START_CHAR] = "N";
        CHAR_TABLE['О' - START_CHAR] = "O";
        CHAR_TABLE['П' - START_CHAR] = "P";
        CHAR_TABLE['Р' - START_CHAR] = "R";
        CHAR_TABLE['С' - START_CHAR] = "TS";
        CHAR_TABLE['Т' - START_CHAR] = "T";
        CHAR_TABLE['У' - START_CHAR] = "U";
        CHAR_TABLE['Ф' - START_CHAR] = "F";
        CHAR_TABLE['Х' - START_CHAR] = "H";
        CHAR_TABLE['Ц' - START_CHAR] = "C";
        CHAR_TABLE['Ч' - START_CHAR] = "CH";
        CHAR_TABLE['Ш' - START_CHAR] = "SH";
        CHAR_TABLE['Щ' - START_CHAR] = "SHCH";
        CHAR_TABLE['Ъ' - START_CHAR] = "";
        CHAR_TABLE['Ы' - START_CHAR] = "Y";
        CHAR_TABLE['Ь' - START_CHAR] = "";
        CHAR_TABLE['Э' - START_CHAR] = "E";
        CHAR_TABLE['Ю' - START_CHAR] = "U";
        CHAR_TABLE['Я' - START_CHAR] = "YA";

        for (int i = 0; i < CHAR_TABLE.length; i++) {
            char idx = (char) ((char) i + START_CHAR);
            char lower = new String(new char[]{idx}).toLowerCase().charAt(0);
            if (CHAR_TABLE[i] != null) {
                CHAR_TABLE[lower - START_CHAR] = CHAR_TABLE[i].toLowerCase();
            }
        }
    }

    /**
     * Переводит русский текст в транслит. В результирующей строке
     * каждая русская буква будет заменена на соответствующую английскую.
     * Не русские символы останутся прежними.
     */
    static String toLatin(String text) {
        char[] charBuffer = text.toCharArray();
        StringBuilder sb = new StringBuilder(text.length());
        for (char symbol : charBuffer) {
            int i = symbol - START_CHAR;
            if (i >= 0 && i < CHAR_TABLE.length) {
                String replace = CHAR_TABLE[i];
                sb.append(replace == null ? symbol : replace);
            } else {
                sb.append(symbol);
            }
        }
        return sb.toString();
    }
}
