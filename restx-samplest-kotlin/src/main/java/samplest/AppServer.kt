package samplest

import restx.server.simple.simple.SimpleWebServer

fun main(args : Array<String>) {
    val port = Integer.valueOf(System.getenv("PORT")?:"8080")
    val server = SimpleWebServer.builder().setRouterPath("/api").setPort(port).build()

    /*
         * load mode from system property if defined, or default to dev
         * be careful with that setting, if you use this class to launch your server in production, make sure to launch
         * it with -Drestx.mode=prod or change the default here
         */
    System.setProperty("restx.mode", System.getProperty("restx.mode", "dev"))

    server.startAndAwait()
}
