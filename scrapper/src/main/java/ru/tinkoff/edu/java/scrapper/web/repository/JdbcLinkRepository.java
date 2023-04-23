package ru.tinkoff.edu.java.scrapper.web.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.web.dto.repository.Link;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class JdbcLinkRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Link> findAll()
    {
        return jdbcTemplate.query("SELECT * FROM links",new LinkMapper());
    }

    public int add(long userId,String url)
    {
        return jdbcTemplate.update("INSERT INTO links (link,tracking_user,last_check) VALUES (?,?,?)",url,userId, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public int delete(long tgChatId,String link)
    {
        return jdbcTemplate.update("DELETE FROM links WHERE link = ? AND tracking_user = ?",link,tgChatId);
    }


}
