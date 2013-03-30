package restx.jackson;

public class Views {
    public static class Public {}
    public static class Private extends Public {} // json view of elements stored but not exposed
    public static class Transient extends Public {} // json view of elements exposed but not stored
}