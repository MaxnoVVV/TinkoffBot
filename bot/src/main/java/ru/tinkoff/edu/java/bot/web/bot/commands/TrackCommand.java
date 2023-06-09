package ru.tinkoff.edu.java.bot.web.bot.commands;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.tinkoff.edu.java.bot.web.dto.clients.AddLinkRequest;

@Slf4j
public class TrackCommand extends Command {

  @Override
  public CommandResponse proccess(Update update) {
    if (update.message().text().trim().equals("/track")) {
      log.info(String.format("Update %d with %s message is /track command", update.updateId(),
          update.message().text()));
      return new SuccessCommandResponse("Отправьте ссылку для отслеживания", true);
    } else {
      return next(update);
    }
  }

  public static SendMessage startTrack(Update update) {
    ResponseEntity<?> response = client.addLink(update.message().chat().id(),
        new AddLinkRequest(update.message().text()));

    if (response.getStatusCode() == HttpStatus.OK) {
      return new SendMessage(update.message().chat().id(), "Ссылка добавлена");
    } else if (response.getStatusCode().is4xxClientError()){
      return new SendMessage(update.message().chat().id(),
          "Ссылка некорректна или уже отслеживется");
    }
    else{
      return new SendMessage(update.message().chat().id(),
          "Сервис недоступен, попробуйте позднее");
    }

  }
}
