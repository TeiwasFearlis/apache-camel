package ru.apachecamel

import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.processing.Processor
import javax.sql.DataSource

@Component
class Starter : RouteBuilder(){

    @Autowired
    lateinit var dataSource: DataSource
    
    // override fun configure() {
    //     from("file:/Users/azaitsev/Downloads/apache-camel/files/input")
    //         .routeId("traveling")
    //         .log("\${headers.CamelFileName}")
    //         .log(">>>>>>>> \${body}")
    //         .to("file:/Users/azaitsev/Downloads/files/output")
    //         }

    override fun configure() {
       // regist()
        from("timer:base?period=30000")
            .routeId("traveling")
            .setBody(simple("INSERT INTO age_table(age) values(18)"))
            //.log("\${headers.CamelFileName}")
            .to("jdbc:dataSource")
            .setBody(simple("select * from age_table"))
            .to("jdbc:dataSource")
            .log(">>>>>>>> \${body}")
            }
}