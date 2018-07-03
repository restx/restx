package restx.types;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public interface RawTypesDefinition {
    boolean accepts(Type type);
    boolean accepts(TypeMirror type, ProcessingEnvironment processingEnvironment);

    abstract class AbstractValueBasedRawTypesDefinition<T> implements RawTypesDefinition {
        final ImmutableList<T> acceptableValues;
        ImmutableList<TypeMirror> acceptableTypeMirrors = null;

        public AbstractValueBasedRawTypesDefinition(T... acceptableValues) {
            this.acceptableValues = ImmutableList.copyOf(acceptableValues);
        }

        protected abstract boolean isApplicableTo(T value, Class type);
        protected abstract TypeMirror createTypeMirrorFrom(T value, ProcessingEnvironment processingEnvironment);

        @Override
        public boolean accepts(final Type type) {
            if(!(type instanceof Class)) {
                return false;
            }

            return Iterables.tryFind(acceptableValues, new Predicate<T>() {
                @Override
                public boolean apply(T value) {
                    return isApplicableTo(value, (Class)type);
                }
            }).isPresent();
        }

        @Override
        public boolean accepts(final TypeMirror type, final ProcessingEnvironment processingEnvironment) {
            if(acceptableTypeMirrors == null) {
                this.acceptableTypeMirrors = ImmutableList.copyOf(Lists.transform(acceptableValues, new Function<T, TypeMirror>() {
                    @Override
                    public TypeMirror apply(T value) {
                        return createTypeMirrorFrom(value, processingEnvironment);
                    }
                }));
            }

            return Iterables.tryFind(acceptableTypeMirrors, new Predicate<TypeMirror>() {
                @Override
                public boolean apply(TypeMirror acceptableTypeMirror) {
                    return processingEnvironment.getTypeUtils().isAssignable(acceptableTypeMirror, type)
                            || Types.rawTypeFrom(acceptableTypeMirror.toString()).equals(Types.rawTypeFrom(type.toString()));
                }
            }).isPresent();
        }
    }

    class ClassBasedRawTypesDefinition extends AbstractValueBasedRawTypesDefinition<Class> {
        public ClassBasedRawTypesDefinition(Class... acceptableClasses) {
            super(acceptableClasses);
        }

        @Override
        protected boolean isApplicableTo(Class clazz, Class type) {
            return clazz.isAssignableFrom(type);
        }

        @Override
        protected TypeMirror createTypeMirrorFrom(Class clazz, ProcessingEnvironment processingEnvironment) {
            if(clazz.isPrimitive()) {
                return processingEnvironment.getTypeUtils().getPrimitiveType(TypeKind.valueOf(clazz.toString().toUpperCase()));
            } else {
                return processingEnvironment.getElementUtils().getTypeElement(clazz.getCanonicalName()).asType();
            }
        }
    }

    class FQCNBasedRawTypesDefinition extends AbstractValueBasedRawTypesDefinition<String> {
        public FQCNBasedRawTypesDefinition(String... acceptableFQCNs) {
            super(acceptableFQCNs);
        }

        @Override
        protected boolean isApplicableTo(String fqcn, Class type) {
            return type.getCanonicalName().equals(fqcn);
        }

        @Override
        protected TypeMirror createTypeMirrorFrom(String fqcn, ProcessingEnvironment processingEnvironment) {
            return processingEnvironment.getElementUtils().getTypeElement(fqcn).asType();
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
