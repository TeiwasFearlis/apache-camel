package ru.apachecamel

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.springframework.stereotype.Component

@Component
class Starter : RouteBuilder(){

    override fun configure() {
        from("file:/Users/azaitsev/Downloads/apache-camel/src/main/files/from")
            .routeId("traveling")
            .log("\${headers.CamelFileName}")
            .log(">>>>>>>> \${body}")
            .to("file:/Users/azaitsev/Downloads/apache-camel/src/main/files/toA")
            }
    //     from("timer:foo")
    //         .log("Hello camel")
    // }
}