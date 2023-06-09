package ru.tinkoff.edu.java.testing;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.edu.java.scrapper.configuration.JpaAccessConfiguration;
import ru.tinkoff.edu.java.scrapper.web.dto.repository.Link;
import ru.tinkoff.edu.java.scrapper.web.dto.repository.User;
import ru.tinkoff.edu.java.scrapper.web.service.LinkService;
import ru.tinkoff.edu.java.scrapper.web.service.TgChatService;
import ru.tinkoff.edu.java.scrapper.web.service.jpa.JpaLinkService;
import ru.tinkoff.edu.java.scrapper.web.service.jpa.JpaTgChatService;
import ru.tinkoff.edu.java.testing.configuration.IntegrationTestsConfiguration;

import javax.sql.DataSource;
import java.net.URI;


@SpringBootTest
@EntityScan("ru.tinkoff.edu.java.scrapper.web.entity")
@RunWith(SpringRunner.class)
@Import({IntegrationTestsConfiguration.class, JpaAccessConfiguration.class})
@ContextConfiguration(classes = {JpaAccessConfiguration.class, IntegrationTestsConfiguration.class})
@EnableAutoConfiguration
public class JpaServiceTest extends IntegrationEnvironment {
    @Autowired
    DataSource dataSource;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    LinkService linkService;

    @Autowired
    TgChatService tgChatService;
    //переделать
    @Test
    public void tests() {
        String url1 = "https://stackoverflow.com/questions/32269192/spring-no-entitymanager-with-actual-transaction-available-for-current-thread";
        String url2 = "https://github.com/MaxnoVVV/TinkoffBot/";
        Assertions.assertNotNull(dataSource);
        Assertions.assertNotNull(jdbcTemplate);
        Assertions.assertNotNull(linkService);
        Assertions.assertNotNull(tgChatService);

        Assertions.assertTrue(linkService instanceof JpaLinkService);
        Assertions.assertTrue(tgChatService instanceof JpaTgChatService);

        ResponseEntity response = tgChatService.register(1);
        System.out.println(response.getStatusCode());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        response = tgChatService.register(2);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        response = tgChatService.register(3);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());

        //tgChatService
        Assertions.assertTrue(jdbcTemplate.query("SELECT * FROM users", new DataClassRowMapper<>(User.class)).size() == 3);
        Assertions.assertTrue(jdbcTemplate.query("SELECT * FROM users", new DataClassRowMapper<>(User.class)).stream().anyMatch(u -> u.id() == 1));
        response = tgChatService.unregister(1);
        System.out.println(response.getStatusCode());
        Assertions.assertFalse(jdbcTemplate.query("SELECT * FROM users", new DataClassRowMapper<>(User.class)).stream().anyMatch(u -> u.id() == 1));

        //LinkService

        linkService.add(2, URI.create(url1));
        linkService.add(3, URI.create(url2));

        Assertions.assertTrue(jdbcTemplate.query("SELECT * FROM links", new DataClassRowMapper<>(Link.class)).size() == 2);
        Assertions.assertTrue(jdbcTemplate.query("SELECT * FROM links", new DataClassRowMapper<>(Link.class)).stream().anyMatch(u -> u.getLink().equals(url1) && u.trackingUser() == 2));
        Assertions.assertTrue(jdbcTemplate.query("SELECT * FROM links", new DataClassRowMapper<>(Link.class)).stream().anyMatch(u -> u.getLink().equals(url2) && u.trackingUser() == 3));


        Assertions.assertFalse(jdbcTemplate.query("SELECT * FROM links", new DataClassRowMapper<>(Link.class)).stream().anyMatch(u -> u.getLink().equals(url1) && u.trackingUser() == 2));

        response = linkService.remove(2, URI.create(url1));
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());


    }

}
