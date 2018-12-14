package org.scc.samples.harrypotteronlinebookstore

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HarryPotterOnlineBookStoreApplicationTests extends Specification {

    @Autowired
    Environment environment


    def "check if Harry opened the shop :D"() {
        expect:
        environment != null
    }


}
