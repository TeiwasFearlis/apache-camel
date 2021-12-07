package ru.apachecamel


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.component.jackson.ListJacksonDataFormat
import org.springframework.stereotype.Component

@Component
class Starter : RouteBuilder(){

    // override fun configure() {
    //     from("file:/Users/azaitsev/Downloads/apache-camel/files/input")
    //         .routeId("traveling")
    //         .log("\${headers.CamelFileName}")
    //         .log(">>>>>>>> \${body}")
    //         .to("file:/Users/azaitsev/Downloads/files/output")
    //         }

    // override fun configure() {
    //    // regist()
    //     from("timer:base?period=30000")
    //         .routeId("traveling")
    //         .setBody(simple("INSERT INTO age_table(age,name) values('18','anton')"))
    //         .to("jdbc:dataSource")
    //         .setBody(simple("select * from age_table"))
    //         .to("jdbc:dataSource")
    //         .log(">>>>>>>> \${body}")
    //         }

    override fun configure() {
       // val file = File("Users/azaitsev/Downloads/files/input/test.json")
       // val format: JacksonDataFormat = ListJacksonDataFormat(Person::class.java)
        val objectMapper = ObjectMapper()
            objectMapper.registerModule(KotlinModule())
        from("file:/Users/azaitsev/Downloads/files/input")
            .routeId("traveling")
             // .unmarshal(JacksonDataFormat(objectMapper,PersonList::class.java))
              .unmarshal(JacksonDataFormat(objectMapper,PersonList::class.java))
             .log(">>>>> \${body}")
             .setBody(simple("INSERT INTO age_table(age,name) values('\${body.list.get(0).age','\${body.list.get(0).name}')"))
            .log(">>>>> \${body}")
             .to("jdbc:dataSource")
            }

    data class PersonList(
        val list: List<Person>,
    )


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