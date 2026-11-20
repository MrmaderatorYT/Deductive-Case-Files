package com.ccs.deductive_case_files.game;

/** Encodes the plain text of a cipher task so that the stored data never contains mistakes. */
public final class Cipher {
    private static final String UK = "АБВГҐДЕЄЖЗИІЇЙКЛМНОПРСТУФХЦЧШЩЬЮЯ";
    private static final String EN = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private Cipher() {
    }

    public static String alphabet(String lang) {
        return "uk".equals(lang) ? UK : EN;
    }

    /** Spec: CAESAR:k (shift forward), ATBASH (mirrored alphabet), REVERSE, NUMBERS. */
    public static String encode(String plain, String spec, String lang) {
        String abc = alphabet(lang);
        String text = plain.toUpperCase();
        String[] p = spec.split(":");
        StringBuilder sb = new StringBuilder();
        switch (p[0]) {
            case "REVERSE":
                return new StringBuilder(text).reverse().toString();
            case "NUMBERS":
                for (int i = 0; i < text.length(); i++) {
                    int k = abc.indexOf(text.charAt(i));
                    if (k < 0) {
                        sb.append(text.charAt(i) == ' ' ? "  /  " : String.valueOf(text.charAt(i)));
                    } else {
                        if (sb.length() > 0 && Character.isDigit(sb.charAt(sb.length() - 1))) sb.append('-');
                        sb.append(k + 1);
                    }
                }
                return sb.toString();
            default: {
                boolean atbash = "ATBASH".equals(p[0]);
                int shift = atbash || p.length < 2 ? 0 : Integer.parseInt(p[1]);
                int n = abc.length();
                for (int i = 0; i < text.length(); i++) {
                    int k = abc.indexOf(text.charAt(i));
                    if (k < 0) sb.append(text.charAt(i));
                    else sb.append(abc.charAt(atbash ? n - 1 - k : ((k + shift) % n + n) % n));
                }
                return sb.toString();
            }
        }
    }
}
