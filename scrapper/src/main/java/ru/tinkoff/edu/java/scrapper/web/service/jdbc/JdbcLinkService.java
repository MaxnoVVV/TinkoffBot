package ru.tinkoff.edu.java.scrapper.web.service.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.tinkoff.edu.java.ParseResult;
import ru.tinkoff.edu.java.Parser;
import ru.tinkoff.edu.java.scrapper.web.dto.controllers.ApiErrorResponse;
import ru.tinkoff.edu.java.scrapper.web.dto.controllers.LinkResponse;
import ru.tinkoff.edu.java.scrapper.web.dto.controllers.ListLinksResponse;
import ru.tinkoff.edu.java.scrapper.web.dto.repository.Link;
import ru.tinkoff.edu.java.scrapper.web.repository.jdbc.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.web.service.LinkService;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public class JdbcLinkService implements LinkService {

    private Parser parser;
    private JdbcLinkRepository repository;

    public JdbcLinkService(Parser parser, JdbcLinkRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public int update(long tgChatId,String link) {
        return repository.update(tgChatId,link);
    }

    @Override
    public ResponseEntity<?> add(long tgChatId, URI uri) {
        try {
            log.info("Adding " + uri.toString() + " to " + tgChatId + " chat");

            ParseResult result = parser.parse(uri.toString());

            if (result == null || repository.findAll().stream().anyMatch(l -> l.getLink().equals(uri.toString()) && l.trackingUser() == tgChatId)) {
                log.warn("link " + uri.toString() + " exists or incorrect");
                return new ResponseEntity<>(new ApiErrorResponse("Link format error",
                        "400",
                        "Wrong link format",
                        "Wrong link format",
                        null), HttpStatus.BAD_REQUEST);
            }


            int addedNumber = repository.add(tgChatId, uri.toString());

            if (addedNumber != 0) {
                var res = new LinkResponse(repository.findAll().stream().filter(u -> u.getLink().equals(uri.toString()) && u.trackingUser() == tgChatId).findFirst().get().linkId(), uri.toString());
                log.info("link " + uri.toString() + " success added");
                return new ResponseEntity<>(res, HttpStatus.OK);
            }

            log.info("link " + uri.toString() + " wasn't added");
            return new ResponseEntity<>(new ApiErrorResponse("no links added",
                    "400",
                    "no links added",
                    "no links added",
                    null), HttpStatus.BAD_REQUEST);


        } catch (DataAccessException e) {
            log.error("catched exception in add service method");
            return new ResponseEntity<>(new ApiErrorResponse("error",
                    "400",
                    e.toString(),
                    e.getMessage(),
                    Arrays.stream(e.getStackTrace()).map(s -> s.toString()).toArray(String[]::new)), HttpStatus.BAD_REQUEST);
        }


    }

    @Override
    public ResponseEntity<?> remove(long tgChatId, URI uri) {
        log.info("deleting " + tgChatId + " " + uri.toString());
        try {
            var link_id = repository.findAll().stream().filter(u -> u.getLink().equals(uri.toString())).findFirst().get().linkId();
            int number = repository.delete(tgChatId, uri.toString());
            log.info("found " + number + " link in db");
            if (number == 0) {
                return new ResponseEntity<>(new ApiErrorResponse("error", "404", "No such link", "error", null), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new LinkResponse(link_id, uri.toString()), HttpStatus.OK);
            }
        } catch (NoSuchElementException ex) {
            return new ResponseEntity<>(new ApiErrorResponse("error", "404", "No such link", "error", null), HttpStatus.BAD_REQUEST);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(new ApiErrorResponse("error", "400", e.toString(), e.getMessage(), Arrays.stream(e.getStackTrace()).map(u -> u.toString()).toArray(String[]::new)), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> listAll(long tgChatId) {

        try {
            var result = repository.findAll().stream().filter(u -> u.trackingUser() == tgChatId).map(u -> new LinkResponse(u.linkId(), u.getLink())).toArray(LinkResponse[]::new);
            return new ResponseEntity<>(new ListLinksResponse(result.length, result), HttpStatus.OK);
        } catch (DataAccessException e) {
            return new ResponseEntity<>(new ApiErrorResponse("error", "400", e.toString(), e.getMessage(), Arrays.stream(e.getStackTrace()).map(u -> u.toString()).toArray(String[]::new)), HttpStatus.BAD_REQUEST);
        }
    }

    public List<Link> findAll()
    {
        return repository.findAll();
    }
}
