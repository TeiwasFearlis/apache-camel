package ru.apachecamel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.ListJacksonDataFormat
import org.springframework.stereotype.Component

@Component
class Starter : RouteBuilder() {

    override fun configure() {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        from("file:{{consume.folder}}")
            .routeId("traveling")
            .unmarshal(ListJacksonDataFormat(objectMapper, Person::class.java))
            .log(">>>>> \${body}")
            .split(body())
            .setBody(simple("INSERT INTO age_table(age,name) values('\${body.age}','\${body.name}')"))
            .log(">>>>> \${body}")
            .to("jdbc:dataSource")
    }


    data class Person(
        val age: String,
        val name: String,
    )
}

// fun main(){
//     val  str = String(Files.readAllBytes(Paths.get("/Users/azaitsev/Downloads/files/input/test.json")))
//     val objectMapper = ObjectMapper()
//     objectMapper.registerModule(KotlinModule())
//     println(objectMapper.readValue(str,Starter.Person::class.java))
// }