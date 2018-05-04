package samplest.annotations;

// Annotation aimed at representing every potential values for section 9.6.1 of the JLS
// (https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.1)
public @interface MyAnnotation {
    // primitive types
    byte aByte() default 121;
    short aShort() default 321;
    int anInt() default 321;
    long aLong() default 321;
    float aFloat() default 564.321f;
    double aDouble() default 564.321;
    boolean aBool() default false;
    char aChar() default 'B';

    // Complex types
    String aString() default "BBB";
    Class aClass() default String.class;
    Class<? extends Number> aParameterizedTypeClass() default Integer.class;
    MyEnum anEnum() default MyEnum.B;

    // Another annotation
    MyNestedAnnotation anAnnotation() default @MyNestedAnnotation({ "AAA" });

    byte[] severalBytes() default { 121, 121 };
    short[] severalShorts() default { 654, 321 };
    int[] severalInts() default { 654, 321 };
    long[] severalLongs() default { 654, 321 };
    float[] severalFloats() default { 654.321f, 123.456f};
    double[] severalDoubles() default { 654.321, 123.456};
    boolean[] severalBools() default { false, true };
    char[] severalChars() default { 'C', 'B', 'A' };

    String[] severalStrings() default { "CCC", "BBB", "AAA" };
    Class[] severalClasses() default { String.class, Integer.class };
    Class<? extends Number>[] severalParameterizedTypeClasses() default { Integer.class, Long.class, Double.class };
    MyEnum[] severalEnums() default { MyEnum.B, MyEnum.A };

    MyNestedAnnotation[] severalAnnotations() default {
            @MyNestedAnnotation(value={ "AAA", "CCC" }),
            @MyNestedAnnotation(value={ "BBB", "DDD" })
    };
}
