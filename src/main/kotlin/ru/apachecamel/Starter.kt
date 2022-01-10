package ru.apachecamel

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.component.jackson.ListJacksonDataFormat
import org.springframework.stereotype.Component

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

    // data class Person(
    //     val date: Timestamp,
    //     val age: String,
    //     val name: String,
    // )

    override fun configure() {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule())
        restConfiguration().host("gorest.co.in/").producerComponent("https")
        from("quartz://timerName?cron=0/5+*+*+*+*+?&stateful=true")
            .setProperty("continue", constant(true))
            .setProperty("page", constant(1))
            .loopDoWhile(simple("\${exchangeProperty.continue}"))
            .setHeader(Exchange.HTTP_QUERY, simple("page=\${exchangeProperty.page}"))
            .to("rest:get:public/v1/users")
            .unmarshal(JacksonDataFormat(objectMapper, Response::class.java))
            .process {
                val pages = it.getIn().getBody(Response::class.java).meta.pagination.pages
                val page = it.getIn().getBody(Response::class.java).meta.pagination.page
                if (page == pages) {
                    it.properties["continue"] = false
                } else {
                    it.properties["page"] = it.properties["page"] as Int + 1
                }
                it.`in`.body = it.getIn().getBody(Response::class.java).data
            }
            .split(body())
            .process {
                val body = it.getIn().getBody(Users::class.java)
                it.getIn().headers["CamelJdbcParameters"] = mapOf(
                    "id" to body.id,
                    "name" to body.name,
                    "email" to body.email,
                    "gender" to body.gender,
                    "status" to body.status,
                )
            }
            .choice()
            .`when`(simple("\${body.status} == 'active'"))
            .setBody(simple("INSERT  INTO active_user(id,name,email,gender,status) values(:?id,:?name,:?email,:?gender,:?status) ON CONFLICT DO NOTHING"))
            .to("jdbc:dataSource?useHeadersAsParameters=true")
            .`when`(simple("\${body.status} == 'inactive'"))
            .setBody(simple("INSERT INTO inactive_user(id,name,email,gender,status) values(:?id,:?name,:?email,:?gender,:?status) ON CONFLICT DO NOTHING"))
            .to("jdbc:dataSource?useHeadersAsParameters=true")
            .end()
        from("quartz://timer?cron=0/6+*+*+*+*+?&stateful=true")
            .setProperty("continue", constant(true))
            .setBody(simple("SELECT id FROM active_user UNION SELECT id FROM inactive_user"))
            .to("jdbc:dataSource")
            //.marshal(JacksonDataFormat(objectMapper, Map::class.java))
            .log(">>>>> all id:\${body}")
            .split(body())
            .process {
                it.properties["user_id"] = it.getIn().getBody(Map::class.java)["id"]
            }
            .toD("rest:get:/public/v1/users/\${exchangeProperty.user_id}/posts")
            .unmarshal(JacksonDataFormat(objectMapper, Data::class.java))
            .log(">>>>> unmarshal:\${body}")
            .process {
                it.`in`.body = it.getIn().getBody(Data::class.java).data
            }
            .log(">>>>> data body:\${body}")
            .split(body())
            .log(">>>>> data body:\${body}")
            .setBody(simple("INSERT  INTO posts_table(user_id,title,body) values('\${body.user_id}','\${body.title}'," +
                "'\${body.body}') ON CONFLICT DO NOTHING"))
            .to("jdbc:dataSource")
    }

    data class Response(
        val meta: Meta,
        val data: List<Users>,
    )

    data class Meta(
        val pagination: Pagination,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Pagination(
        val pages: Long,
        val page: Long,
    )

    data class Users(
        val id: Long,
        val name: String,
        val email: String,
        val gender: String,
        val status: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Data(
        val data: List<Posts>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Posts(
        val user_id: Long,
        val title: String,
        val body: String,
    )
}
