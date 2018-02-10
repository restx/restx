package samplest.core;

import com.google.common.base.Optional;
import restx.annotations.*;
import restx.factory.Component;
import samplest.annotations.MyAnnotation;
import samplest.annotations.MyEnum;
import samplest.annotations.MyNestedAnnotation;

/**
 * Date: 4/1/14
 * Time: 09:28
 */
@RestxResource("/core") @Component
public class CoreResource {
    public static final class Message {
        private String msg;

        public String getMsg() {
            return msg;
        }

        public Message setMsg(String msg) {
            this.msg = msg;
            return this;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "msg='" + msg + '\'' +
                    '}';
        }
    }

    /**
     * Says hello to the given person.
     *
     * @param who the person to whom we say hello
     * @return hello message
     */
    @GET("/hello")
    public String hello(String who) {
        return "hello " + who;
    }

    @GET("/hellomsg")
    public Message helloMsg(String who) {
        return new Message().setMsg("hello " + who);
    }

    @POST("/hellomsg")
    public Message helloMsg(Message who) {
        return new Message().setMsg("hello " + who.getMsg());
    }

    @DELETE("/hellomsg")
    public String deleteHello(String who) {
        return "hello "+who;
    }

    @GET("/testingAnnotations")
    @MyAnnotation(
            aByte=123,
            aShort=123,
            anInt=123,
            aLong=123,
            aFloat=123.456f,
            aDouble=123.456,
            aBool=true,
            aChar='A',

            // Complex types
            aString="AAA{\\\"'$AA",
            aClass=CoreResource.class,
            aParameterizedTypeClass=Long.class,
            anEnum=MyEnum.A,

            // Another annotation
            // Not supported (yet)
            anAnnotation=@MyNestedAnnotation(value={ "BBB", "CCC" }),

            severalBytes={ 123, 123 },
            severalShorts={ 123, 456 },
            severalInts={ 123, 456 },
            severalLongs={ 123, 456 },
            severalFloats={ 123.456f, 321.654f},
            severalDoubles={ 123.456, 321.654 },
            severalBools={ true, false },
            severalChars={ 'A', 'B', 'C' },

            severalStrings={ "AAA{\\\"'$AA", "BBB", "CCC" },
            severalClasses={ Integer.class, String.class },
            severalParameterizedTypeClasses={ Integer.class, Short.class },
            severalEnums={ MyEnum.A, MyEnum.B },

            // Not supported (yet)
            severalAnnotations={
                @MyNestedAnnotation(value={ "BBB", "CCC" }),
                @MyNestedAnnotation(value={ "DDD", "EEE" })
            }
    )
    public String testingAnnotations() {
        return "hello blah";
    }
}
