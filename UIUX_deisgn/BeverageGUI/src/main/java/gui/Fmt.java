package gui;

public final class Fmt {
    private Fmt() { }
    public static String money(long v) { return String.format("%,d ₫", v); }
}
