package samplest.models;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

public class SearchCriteria<T> extends ParentSearchCriteria {

    // Declaring a SearchCriteria<BigDecimal> as method parameter doesn't work (Jacksong deserializes node to a String, everytime)
    public static class ConcreteSearchCriteria extends SearchCriteria<BigDecimal> {
    }
    public enum SearchType {
        SIMPLE, COMPLEX
    }

    String v1;
    Integer v2;
    BigDecimal v3;
    DateTime v4;
    T v5;
    SearchType v6;
    String[] multipleV5;
    List<String> multipleV6;
    Iterable<String> multipleV7;
    DateTime[] multipleV8;
    SearchType[] multipleV9;
    List<SearchType> multipleV10;
    SearchCriteria<T> nestedCriteria;
    // An iterable/array of "complex" (by "complex", I mean not a "value" type) type should not be mapped
    // and should trigger an error when provided in request params
    // To reference such nested entry, we should use a syntax like "unsupportedNestedMultipleCriteria[0].v1=foo"
    // and we don't want to allow such syntax in query params
    SearchCriteria<T>[] unsupportedNestedMultipleCriteria;

    public String getV1() {
        return v1;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public Integer getV2() {
        return v2;
    }

    public void setV2(Integer v2) {
        this.v2 = v2;
    }

    public BigDecimal getV3() {
        return v3;
    }

    public void setV3(BigDecimal v3) {
        this.v3 = v3;
    }

    public DateTime getV4() {
        return v4;
    }

    public void setV4(DateTime v4) {
        this.v4 = v4;
    }

    public T getV5() {
        return v5;
    }

    public void setV5(T v5) {
        this.v5 = v5;
    }

    public String[] getMultipleV5() {
        return multipleV5;
    }

    public void setMultipleV5(String[] multipleV5) {
        this.multipleV5 = multipleV5;
    }

    public List<String> getMultipleV6() {
        return multipleV6;
    }

    public void setMultipleV6(List<String> multipleV6) {
        this.multipleV6 = multipleV6;
    }

    public Iterable<String> getMultipleV7() {
        return multipleV7;
    }

    public void setMultipleV7(Iterable<String> multipleV7) {
        this.multipleV7 = multipleV7;
    }

    public DateTime[] getMultipleV8() {
        return multipleV8;
    }

    public void setMultipleV8(DateTime[] multipleV8) {
        this.multipleV8 = multipleV8;
    }

    public SearchCriteria<T> getNestedCriteria() {
        return nestedCriteria;
    }

    public void setNestedCriteria(SearchCriteria<T> nestedCriteria) {
        this.nestedCriteria = nestedCriteria;
    }

    public SearchCriteria<T>[] getUnsupportedNestedMultipleCriteria() {
        return unsupportedNestedMultipleCriteria;
    }

    public void setUnsupportedNestedMultipleCriteria(SearchCriteria<T>[] unsupportedNestedMultipleCriteria) {
        this.unsupportedNestedMultipleCriteria = unsupportedNestedMultipleCriteria;
    }

    public SearchType getV6() {
        return v6;
    }

    public void setV6(SearchType v6) {
        this.v6 = v6;
    }

    public SearchType[] getMultipleV9() {
        return multipleV9;
    }

    public void setMultipleV9(SearchType[] multipleV9) {
        this.multipleV9 = multipleV9;
    }

    public List<SearchType> getMultipleV10() {
        return multipleV10;
    }

    public void setMultipleV10(List<SearchType> multipleV10) {
        this.multipleV10 = multipleV10;
    }
}
