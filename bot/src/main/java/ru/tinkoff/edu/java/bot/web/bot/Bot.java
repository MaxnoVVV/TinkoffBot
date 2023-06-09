package ru.tinkoff.edu.java.bot.web.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.bot.configuration.BotConfig;
import ru.tinkoff.edu.java.bot.web.bot.commands.*;
import ru.tinkoff.edu.java.bot.web.dto.LinkUpdateRequest;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Import(BotConfig.class)
@RequiredArgsConstructor
public class Bot implements AutoCloseable {

  private final TelegramBot bot;
  private final Counter counter;
  private final Command commandProcessor;

  public void addCommands() {
    log.info("Adding commands");
    bot.execute(new SetMyCommands(new BotCommand("/help", "Список команд"),
        new BotCommand("/track", "Начать отслеживание ссылки"),
        new BotCommand("/untrack", "Прекратить отслеживание ссылки"),
        new BotCommand("/list", "Вывести все отслеживаемые ссылки"),
        new BotCommand("/start", "Начать работу с ботом")));
    log.info("Commands added");
  }

  public void start() {
    log.info(String.format("Bot %s started", bot.getToken()));
    bot.setUpdatesListener(new UpdatesListener() {
      @Override
      public int process(List<Update> updates) {
        for (Update update : updates) {
          log.info("Got new update");
          if (update.updateId() == 795769925 || update.updateId() == 795769927) {
            continue;
          }
          processUpdate(update);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
      }
    });
  }

  public void receiveUpdate(LinkUpdateRequest Update) {
    log.info("new update" + Update.toString());
    long[] ids = Update.tgChatIds();
    String description = Update.description();
    if (!(description == null || description.isEmpty() || ids == null)) {
      for (long id : ids) {
        SendMessage message = new SendMessage(ids[0], description);
        bot.execute(message);
      }
    }
  }

  @Override
  public void close() throws Exception {
    bot.removeGetUpdatesListener();
  }

  private void processCommand(Update update) {
    counter.increment();
    CommandResponse result = commandProcessor.proccess(update);
    if (result == null) {
      log.info(String.format("Unknown command for %d", update.updateId()));
      bot.execute(new SendMessage(update.message().chat().id(), "Неизвестная команда"));
    } else if (result instanceof SuccessCommandResponse) {

      SendMessage request = new SendMessage(update.message().chat().id(),
          ((SuccessCommandResponse) result).text());
      if (((SuccessCommandResponse) result).isForceReply()) {
        request.replyMarkup(new ForceReply());
      }
      log.info(String.format("Executing %d update command", update.updateId()));
      bot.execute(request);
    } else {
      bot.execute(
          new SendMessage(update.message().chat().id(), "Сервис недоступен, попробуйте позднее"));
      log.warn(
          String.format("Update %d command did not processed successfully", update.updateId()));
    }
  }

  public void processUpdate(Update update) {

    log.info(String.format("Got update %d, processing", update.updateId()));

    if (update.message() != null && update.message().entities() != null && Arrays.stream(
        update.message().entities()).anyMatch(entity -> {
      return entity.type() == MessageEntity.Type.bot_command;
    })) {
      processCommand(update);
      log.info(String.format("Update %d processed", update.updateId()));
    } else if (update.message() != null && update.message().replyToMessage() != null
        && update.message().replyToMessage().from().isBot()) {
      log.info(String.format("Update %d is link", update.updateId()));

      if (update.message().replyToMessage().text().equals("Отправьте ссылку для отслеживания")) {
        bot.execute(TrackCommand.startTrack(update));
      } else if (update.message().replyToMessage().text()
          .equals("Отправьте ссылку для отмены отслеживания")) {
        bot.execute(UntrackCommand.stopTrack(update));
      }
    } else {
      log.info(String.format("No action for %d update", update.updateId()));
    }
  }
}
