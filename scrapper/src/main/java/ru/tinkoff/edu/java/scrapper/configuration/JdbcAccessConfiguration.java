package ru.tinkoff.edu.java.scrapper.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.edu.java.Parser;
import ru.tinkoff.edu.java.scrapper.web.repository.jdbc.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.web.repository.jdbc.JdbcUserRepository;
import ru.tinkoff.edu.java.scrapper.web.service.LinkService;
import ru.tinkoff.edu.java.scrapper.web.service.TgChatService;
import ru.tinkoff.edu.java.scrapper.web.service.jdbc.JdbcLinkService;
import ru.tinkoff.edu.java.scrapper.web.service.jdbc.JdbcTgChatService;

@Configuration
@ConditionalOnProperty(prefix = "scrapper", name = "database-access-type", havingValue = "jdbc")
public class JdbcAccessConfiguration {

  @Bean
  public JdbcLinkRepository linkRepository() {
    return new JdbcLinkRepository();
  }

  @Bean
  public JdbcUserRepository userRepository() {
    return new JdbcUserRepository();
  }

  @Bean
  public LinkService linkService(Parser parser, JdbcLinkRepository linkRepository) {
    return new JdbcLinkService(parser, linkRepository);
  }

  @Bean
  public TgChatService tgChatService(JdbcUserRepository userRepository) {
    return new JdbcTgChatService(userRepository);
  }
}
