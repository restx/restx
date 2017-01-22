package samplest.core;

import com.google.common.base.Optional;
import restx.annotations.*;
import restx.factory.Component;

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
}
