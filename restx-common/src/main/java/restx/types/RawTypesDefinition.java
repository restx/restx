package restx.types;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public interface RawTypesDefinition {
    boolean accepts(Type type);
    boolean accepts(TypeMirror type, ProcessingEnvironment processingEnvironment);

    class ClassBasedRawTypesDefinition implements RawTypesDefinition {
        final ImmutableList<Class> acceptableClasses;
        ImmutableList<TypeMirror> acceptableTypeMirrors = null;

        public ClassBasedRawTypesDefinition(Class... acceptableClasses) {
            this.acceptableClasses = ImmutableList.copyOf(acceptableClasses);
        }

        @Override
        public boolean accepts(final Type type) {
            if(!(type instanceof Class)) {
                return false;
            }

            return Iterables.tryFind(acceptableClasses, new Predicate<Class>() {
                @Override
                public boolean apply(Class clazz) {
                    return clazz.isAssignableFrom((Class)type);
                }
            }).isPresent();
        }

        @Override
        public boolean accepts(final TypeMirror type, final ProcessingEnvironment processingEnvironment) {
            if(acceptableTypeMirrors == null) {
                this.acceptableTypeMirrors = ImmutableList.copyOf(Lists.transform(acceptableClasses, new Function<Class, TypeMirror>() {
                    @Override
                    public TypeMirror apply(Class input) {
                        if(input.isPrimitive()) {
                            return processingEnvironment.getTypeUtils().getPrimitiveType(TypeKind.valueOf(input.toString().toUpperCase()));
                        } else {
                            return processingEnvironment.getElementUtils().getTypeElement(input.getCanonicalName()).asType();
                        }
                    }
                }));
            }

            return Iterables.tryFind(acceptableTypeMirrors, new Predicate<TypeMirror>() {
                @Override
                public boolean apply(TypeMirror acceptableTypeMirror) {
                    return processingEnvironment.getTypeUtils().isAssignable(acceptableTypeMirror, type);
                }
            }).isPresent();
        }
    }

    class PrimitiveRawTypesDefinition extends ClassBasedRawTypesDefinition {
        public PrimitiveRawTypesDefinition() {
            super(
                    byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class,
                    Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class
            );
        }
    }
    class CommonJDKRawTypesDefinition extends ClassBasedRawTypesDefinition {
        public CommonJDKRawTypesDefinition() {
            super(
                    String.class, Class.class, Enum.class, File.class, BigDecimal.class, BigInteger.class,
                    Currency.class, Date.class, Locale.class, TimeZone.class, UUID.class, Charset.class, Path.class,
                    Pattern.class, URI.class, URL.class
            );
        }
    }
    class JodaTimeRawTypesDefinition extends ClassBasedRawTypesDefinition {
        public JodaTimeRawTypesDefinition() {
            super(
                    DateTime.class, Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class, DateTimeZone.class
            );
        }
    }
    class IsEnumRawTypeDefinition implements RawTypesDefinition {
        @Override
        public boolean accepts(Type type) {
            return Types.isAssignableFrom(Enum.class, type);
        }
        @Override
        public boolean accepts(TypeMirror type, ProcessingEnvironment processingEnvironment) {
            return Types.isEnum(type, processingEnvironment.getTypeUtils());
        }
    }
}
