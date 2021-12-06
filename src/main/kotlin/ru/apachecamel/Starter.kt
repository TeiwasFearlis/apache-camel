package ru.apachecamel


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.model.dataformat.JsonLibrary
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class Starter : RouteBuilder(){

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate


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
        val objectMapper = ObjectMapper()
            objectMapper.registerModule(KotlinModule())
        var age=""
        var name=""
       // JacksonDataFormat(objectMapper,Person::class.java)
        from("file:/Users/azaitsev/Downloads/files/input")
            .routeId("traveling")
           // .log("\${headers.CamelFileName}")

             .unmarshal(JacksonDataFormat(objectMapper,Person::class.java))
             .process { e ->
                age = e.getIn().getBody(Person::class.java).age
               name = e.getIn().getBody(Person::class.java).name
                // val sql = "INSERT INTO age_table(age,name) values('$age','$name')"
                 // jdbcTemplate.queryForObject(sql, String::class.java)
                 // e.getIn().body
             }
            // .unmarshal(JacksonDataFormat(objectMapper,String::class.java))
            // .to("file:/Users/azaitsev/Downloads/files/output")
             .setBody(simple("INSERT INTO age_table(age,name) values('$age','$name')"))
            .log(">>>>> \${body}")
             .to("jdbc:dataSource")
            .end()
            // .setBody(simple("select * from age_table"))
            // .to("jdbc:dataSource")
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