package samplest.models;

import org.bson.types.ObjectId;
import org.joda.time.*;

import javax.validation.constraints.Size;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public class AllRawTypesCriteria {
    enum MyEnum { foo, bar }

    @Size(min=3,max=10)
    String str;

    Class clazz;
    MyEnum myEnum;
    File file;
    BigDecimal bigDecimal;
    BigInteger bigInteger;

    Currency currency;
    Date date;
    Locale locale;
    TimeZone timeZone;
    UUID uuid;
    Charset charset;
    Path path;

    Pattern pattern;
    URI uri;
    URL url;

    ObjectId objectId;

    DateTime jodaDateTime;
    Instant jodaInstant;
    LocalDate jodaLocalDate;
    LocalDateTime jodaLocalDateTime;
    LocalTime jodaLocalTime;
    DateTimeZone jodaTimeZone;

    byte aByte;
    short aShort;
    int anInt;
    long aLong;
    float aFloat;
    double aDouble;
    boolean aBoolean;
    char aChar;

    Byte aByteWrapper;
    Short aShortWrapper;
    Integer anIntegerWrapper;
    Long aLongWrapper;
    Float aFloatWrapper;
    Double aDoubleWrapper;
    Boolean aBooleanWrapper;
    Character aCharacterWrapper;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public MyEnum getMyEnum() {
        return myEnum;
    }

    public void setMyEnum(MyEnum myEnum) {
        this.myEnum = myEnum;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    public BigInteger getBigInteger() {
        return bigInteger;
    }

    public void setBigInteger(BigInteger bigInteger) {
        this.bigInteger = bigInteger;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public DateTime getJodaDateTime() {
        return jodaDateTime;
    }

    public void setJodaDateTime(DateTime jodaDateTime) {
        this.jodaDateTime = jodaDateTime;
    }

    public Instant getJodaInstant() {
        return jodaInstant;
    }

    public void setJodaInstant(Instant jodaInstant) {
        this.jodaInstant = jodaInstant;
    }

    public LocalDate getJodaLocalDate() {
        return jodaLocalDate;
    }

    public void setJodaLocalDate(LocalDate jodaLocalDate) {
        this.jodaLocalDate = jodaLocalDate;
    }

    public LocalDateTime getJodaLocalDateTime() {
        return jodaLocalDateTime;
    }

    public void setJodaLocalDateTime(LocalDateTime jodaLocalDateTime) {
        this.jodaLocalDateTime = jodaLocalDateTime;
    }

    public LocalTime getJodaLocalTime() {
        return jodaLocalTime;
    }

    public void setJodaLocalTime(LocalTime jodaLocalTime) {
        this.jodaLocalTime = jodaLocalTime;
    }

    public DateTimeZone getJodaTimeZone() {
        return jodaTimeZone;
    }

    public void setJodaTimeZone(DateTimeZone jodaTimeZone) {
        this.jodaTimeZone = jodaTimeZone;
    }

    public String getObjectId() {
        if(objectId == null) {
            return null;
        }
        return objectId.toString();
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public byte getaByte() {
        return aByte;
    }

    public void setaByte(byte aByte) {
        this.aByte = aByte;
    }

    public short getaShort() {
        return aShort;
    }

    public void setaShort(short aShort) {
        this.aShort = aShort;
    }

    public int getAnInt() {
        return anInt;
    }

    public void setAnInt(int anInt) {
        this.anInt = anInt;
    }

    public long getaLong() {
        return aLong;
    }

    public void setaLong(long aLong) {
        this.aLong = aLong;
    }

    public float getaFloat() {
        return aFloat;
    }

    public void setaFloat(float aFloat) {
        this.aFloat = aFloat;
    }

    public double getaDouble() {
        return aDouble;
    }

    public void setaDouble(double aDouble) {
        this.aDouble = aDouble;
    }

    public boolean isaBoolean() {
        return aBoolean;
    }

    public void setaBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public char getaChar() {
        return aChar;
    }

    public void setaChar(char aChar) {
        this.aChar = aChar;
    }

    public Byte getaByteWrapper() {
        return aByteWrapper;
    }

    public void setaByteWrapper(Byte aByteWrapper) {
        this.aByteWrapper = aByteWrapper;
    }

    public Short getaShortWrapper() {
        return aShortWrapper;
    }

    public void setaShortWrapper(Short aShortWrapper) {
        this.aShortWrapper = aShortWrapper;
    }

    public Integer getAnIntegerWrapper() {
        return anIntegerWrapper;
    }

    public void setAnIntegerWrapper(Integer anIntegerWrapper) {
        this.anIntegerWrapper = anIntegerWrapper;
    }

    public Long getaLongWrapper() {
        return aLongWrapper;
    }

    public void setaLongWrapper(Long aLongWrapper) {
        this.aLongWrapper = aLongWrapper;
    }

    public Float getaFloatWrapper() {
        return aFloatWrapper;
    }

    public void setaFloatWrapper(Float aFloatWrapper) {
        this.aFloatWrapper = aFloatWrapper;
    }

    public Double getaDoubleWrapper() {
        return aDoubleWrapper;
    }

    public void setaDoubleWrapper(Double aDoubleWrapper) {
        this.aDoubleWrapper = aDoubleWrapper;
    }

    public Boolean getaBooleanWrapper() {
        return aBooleanWrapper;
    }

    public void setaBooleanWrapper(Boolean aBooleanWrapper) {
        this.aBooleanWrapper = aBooleanWrapper;
    }

    public Character getaCharacterWrapper() {
        return aCharacterWrapper;
    }

    public void setaCharacterWrapper(Character aCharacterWrapper) {
        this.aCharacterWrapper = aCharacterWrapper;
    }
}
