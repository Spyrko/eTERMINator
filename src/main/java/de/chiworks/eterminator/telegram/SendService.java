package de.chiworks.eterminator.telegram;

import de.chiworks.eterminator.telegram.user.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Service
@Scope(SCOPE_SINGLETON)
public class SendService {

    private TelegramLongPollingBot bot;

    public void registerBot(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void send(Long to, String msg) {
        SendMessage sm = SendMessage.builder()
                .chatId(to.toString()) //Who are we sending a message to
                .text(msg).build();    //Message content
        try {
            bot.execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void send(Long to, String msg, ReplyKeyboard markup) {
        SendMessage sm = SendMessage.builder()
                .chatId(to.toString()) //Who are we sending a message to
                .text(msg).replyMarkup(markup).build();    //Message content
        try {
            bot.execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    public void send(User to, String msg) {
        send(to.getId(), msg);
    }

    public void send(User to, String msg, ReplyKeyboard markup) {
        send(to.getId(), msg, markup);
    }

    public void tryAgain(long to) {
        send(to, "Please try again.");
    }
}
