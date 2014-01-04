package samplest.core;

import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;

/**
 * Date: 4/1/14
 * Time: 09:28
 */
@RestxResource @Component
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

    @GET("/core/hello")
    public String hello(String who) {
        return "hello " + who;
    }

    @GET("/core/hellomsg")
    public Message helloMsg(String who) {
        return new Message().setMsg("hello " + who);
    }

    @POST("/core/hellomsg")
    public Message helloMsg(Message who) {
        return new Message().setMsg("hello " + who.getMsg());
    }

}
