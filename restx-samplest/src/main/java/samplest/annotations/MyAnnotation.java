package samplest.annotations;

// Annotation aimed at representing every potential values for section 9.6.1 of the JLS
// (https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.1)
public @interface MyAnnotation {
    // primitive types
    byte aByte();
    short aShort();
    int anInt();
    long aLong();
    float aFloat();
    double aDouble();
    boolean aBool();
    char aChar();

    // Complex types
    String aString();
    Class aClass();
    Class<? extends Number> aParameterizedTypeClass();
    MyEnum anEnum();

    // Another annotation
    MyNestedAnnotation anAnnotation();

    byte[] severalBytes();
    short[] severalShorts();
    int[] severalInts();
    long[] severalLongs();
    float[] severalFloats();
    double[] severalDoubles();
    boolean[] severalBools();
    char[] severalChars();

    String[] severalStrings();
    Class[] severalClasses();
    Class<? extends Number>[] severalParameterizedTypeClasses();
    MyEnum[] severalEnums();

    MyNestedAnnotation[] severalAnnotations();
}
