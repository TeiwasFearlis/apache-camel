package ru.apachecamel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.ListJacksonDataFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class Starter : RouteBuilder() {
    //private val logger= LoggerFactory.getLogger(TaskBean::class.java)

    // override fun configure() {
    //     val objectMapper = ObjectMapper()
    //     objectMapper.registerModule(KotlinModule())
    //     from("file:{{consume.folder}}")
    //         .routeId("traveling")
    //         .unmarshal(ListJacksonDataFormat(objectMapper, Person::class.java))
    //         .log(">>>>> \${body}")
    //         .log(">>>>> \${headers.${Exchange.FILE_NAME}}")
    //         .split(body())
    //         .setBody(simple("INSERT INTO age_table(age,name,date) values('\${body.age}','\${body.name}','\${body.date}')"))
    //         .log(">>>>> \${body}")
    //         .to("jdbc:dataSource")
    //     from("quartz://timerName?cron=0/5+*+*+*+*+?")
    //         .process {
    //             val listOf = listOf(Person(Timestamp.valueOf(LocalDateTime.now()), "1", "bob"),
    //                 Person(Timestamp.valueOf(LocalDateTime.now()), "5", "ant"))
    //             it.`in`.body = listOf
    //             it.`in`.headers[Exchange.FILE_NAME] = "Test.json"
    //         }
    //         .marshal(ListJacksonDataFormat(objectMapper, Person::class.java))
    //         .to("file:{{consume.folder}}")
    // }

    override fun configure() {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        from("file:{{consume.folder}}")
            .routeId("traveling")
            .unmarshal(ListJacksonDataFormat(objectMapper, Person::class.java))
            .log(">>>>> \${body}")
            .log(">>>>> \${headers.${Exchange.FILE_NAME}}")
            .split(body())
            .setBody(simple("INSERT INTO age_table(age,name,date) values('\${body.age}','\${body.name}','\${body.date}')"))
            .log(">>>>> \${body}")
            .to("jdbc:dataSource")
        from("quartz://timerName?cron=0/5+*+*+*+*+?")
            .process {
                val listOf = listOf(Person(Timestamp.valueOf(LocalDateTime.now()), "1", "bob"),
                    Person(Timestamp.valueOf(LocalDateTime.now()), "5", "ant"))
                it.`in`.body = listOf
                it.`in`.headers[Exchange.FILE_NAME] = "Test.json"
            }
            .marshal(ListJacksonDataFormat(objectMapper, Person::class.java))
            .to("file:{{consume.folder}}")
    }



    data class Person(
        val date: Timestamp,
        val age: String,
        val name: String,
    )
}
