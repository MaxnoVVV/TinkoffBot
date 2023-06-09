package ru.tinkoff.edu.java.scrapper.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.tinkoff.edu.java.Parser;
import ru.tinkoff.edu.java.scrapper.web.repository.jpa.JpaLinkRepository;
import ru.tinkoff.edu.java.scrapper.web.repository.jpa.JpaUserRepository;
import ru.tinkoff.edu.java.scrapper.web.service.LinkService;
import ru.tinkoff.edu.java.scrapper.web.service.TgChatService;
import ru.tinkoff.edu.java.scrapper.web.service.jpa.JpaLinkService;
import ru.tinkoff.edu.java.scrapper.web.service.jpa.JpaTgChatService;

@Configuration
@ConditionalOnProperty(prefix = "scrapper", name = "database-access-type", havingValue = "jpa")
@EnableJpaRepositories(basePackages = "ru.tinkoff.edu.java.scrapper.web.repository")
@Import(ParserConfig.class)
public class JpaAccessConfiguration {

  @Bean
  public LinkService linkService(JpaLinkRepository repository, Parser parser) {
    return new JpaLinkService(repository, parser);
  }

  @Bean
  public TgChatService tgChatService(JpaUserRepository repository) {
    return new JpaTgChatService(repository);
  }
}
