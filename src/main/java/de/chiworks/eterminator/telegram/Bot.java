package de.chiworks.eterminator.telegram;

import de.chiworks.eterminator.error.CommandException;
import de.chiworks.eterminator.telegram.command.CommandService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static java.util.Optional.ofNullable;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Component
@Scope(SCOPE_SINGLETON)
@Slf4j
public class Bot extends TelegramLongPollingBot {
    private final CommandService commandService;
    private final SendService sendService;
    private final BotConfiguration botConfiguration;

    public Bot(SendService sendService, CommandService commandService, BotConfiguration botConfiguration) {
        this.sendService = sendService;
        this.commandService = commandService;
        this.botConfiguration = botConfiguration;
        sendService.registerBot(this);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfiguration.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfiguration.getPassword();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasCallbackQuery()) {
            reactToUserMessage(update);
        } else {
            reactToButtonClick(update);
        }
    }

    private void reactToButtonClick(Update update) {
        String messageText = update.getCallbackQuery().getData();
        org.telegram.telegrambots.meta.api.objects.User from = update.getCallbackQuery().getFrom();

        User user = getUser(from.getId(), from.getFirstName());
        try {
            commandService.continueCommand(user, messageText);
        } catch (CommandException e) {
            handleInternalError(user.getId(), e);
        }
    }

    private void reactToUserMessage(Update update) {
        Message message = ofNullable(update.getMessage()).orElseGet(() -> update.getCallbackQuery().getMessage());
        long senderId = message.getFrom().getId();
        System.out.println(update);

        User user = getUser(senderId, message.getFrom().getFirstName());
        String messageText = message.getText();
        try {
            if (message.isCommand()) {
                commandService.executeCommand(user, messageText);
            } else {
                commandService.continueCommand(user, messageText);
            }
        } catch (CommandException e) {
            handleInternalError(user.getId(), e);
        }
    }

    private static User getUser(long senderId, String name) {
        return User.get(senderId).orElseGet(() -> new User(senderId, name));
    }

    private void handleInternalError(Long userId, Throwable e) {
        sendService.send(userId, "Sorry, it seems like I have some trouble processing your request at the moment. Please try again later.");
        log.error("Request could not be processed.", e);
    }
}
