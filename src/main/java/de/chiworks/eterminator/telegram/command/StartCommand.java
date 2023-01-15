package de.chiworks.eterminator.telegram.command;

import de.chiworks.eterminator.eterminservice.data.QualificationGroup;
import de.chiworks.eterminator.eterminservice.data.QualificationSubgroup;
import de.chiworks.eterminator.eterminservice.data.SearchParameters;
import de.chiworks.eterminator.eterminservice.service.QualificationService;
import de.chiworks.eterminator.eterminservice.service.TerminService;
import de.chiworks.eterminator.telegram.SendService;
import de.chiworks.eterminator.telegram.user.User;
import lombok.Data;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.chiworks.eterminator.telegram.command.CommandStrings.START_COMMAND;


@Component
public class StartCommand extends Command<StartCommand.Status> {

    private static final List<Integer> POSSIBLE_RADII = List.of(5, 10, 20, 50, 100, 150);
    private final Map<String, QualificationGroup> qualificationGroups;
    private final SendService sendService;
    private final TerminService terminService;
    private final InlineKeyboardMarkup radiusKeyboard = createRadiusKeyboard();

    private final InlineKeyboardMarkup qualificationGroupKeyboard;

    private InlineKeyboardMarkup qualificationSubgroupKeyboard;

    StartCommand(SendService sendService, QualificationService qualificationService, TerminService terminService) {
        this.sendService = sendService;
        this.terminService = terminService;
        qualificationGroups = qualificationService.queryQualifications();
        qualificationGroupKeyboard = createQualificationGroupKeyboard();
    }

    @Override
    public @NonNull String getCommandString() {
        return START_COMMAND;
    }

    @Override
    public @NonNull String getDescription() {
        return "Start or restart the appointment search with new parameters";
    }

    @Override
    public @NonNull CommandInProgress<Status> execute(User user) {
        sendService.send(user, "Hello " + user.getName());
        sendService.send(user, "I'm here to inform you about open appointments on 116117");
        sendService.send(user, "Please tell me your \"Vermittlungscode\"");
        return new CommandInProgress<>(this, new Status(), user);
    }

    @Override
    public void continueExec(User user, @NonNull String command, Status status) {
        if (status.getToken() == null) {
            receiveEtsCode(user, command, status);
            return;
        }
        if (status.getZipCode() == null) {
            receiveZipCode(user, command, status);
            return;
        }
        if (status.getRadius() == null) {
            receiveRadius(user, command, status);
            return;
        }
        if (status.getQualificationGroup() == null) {
            receiveQualificationGroup(user, command, status);
            return;
        }
        if (status.getQualificationSubgroup() == null) {
            receiveQualificationSubgroup(user, command, status);
        }
    }

    private void receiveQualificationSubgroup(User user, String command, Status status) {
        if (status.getQualificationGroup().hasSubgroup(command)) {
            QualificationSubgroup subgroup = status.getQualificationGroup().getSubgroup(command);
            status.setQualificationSubgroup(subgroup);
            startSearchForAppointment(user, status);
        } else {
            sendService.send(user, "I'm afraid they dont have that kind of appointment. Please choose one from the list below.", qualificationSubgroupKeyboard);
        }
    }

    private void receiveQualificationGroup(User user, String command, Status status) {
        if (qualificationGroups.containsKey(command)) {
            QualificationGroup group = qualificationGroups.get(command);
            status.setQualificationGroup(group);
            if (setSubgroupIfUnambiguous(group, status)) {
                startSearchForAppointment(user, status);
                return;
            }
            qualificationSubgroupKeyboard = createQualificationSubgroupKeyboard(status);
            askForQualificationSubgroup(user);
        } else {
            sendService.send(user, "I'm afraid they dont have the profession " + command + ". Please choose one from the list below.", qualificationGroupKeyboard);
        }
    }

    private void startSearchForAppointment(User user, Status status) {
        sendService.send(user, "Thank you. I've everything I need. I will inform you, when I see an open appointment slot.");
        user.setSearchParameters(status.toSearchParameters());
        // TODO: trigger quartz job
    }

    private void receiveRadius(User user, String command, Status status) {
        if (command.matches("[0-9]*") && POSSIBLE_RADII.contains(Integer.parseInt(command))) {
            status.setRadius(command);
            askForQualificationGroup(user);
        } else {
            sendService.send(user, "The radius you entered is invalid. Please enter one of the radii below.", radiusKeyboard);
        }
    }

    private void receiveZipCode(User user, String command, Status status) {
        if (command.matches("[0-9]{5}")) {
            status.setZipCode(command);
            askForRadius(user);
        } else {
            sendService.send(user, "The zip code you provided is invalid. Zip codes consist of five digits.");
            sendService.tryAgain(user.getId());
        }
    }

    private void receiveEtsCode(User user, String command, Status status) {
        if (command.matches("[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}") &&
                isValidEtsCode(command)) {
            status.setToken(encode(command));
            askForZipCode(user);
        } else {
            sendService.send(user, "The \"Vermittlungscode\" you entered is invalid.\nIt should have the form of XXXX-XXXX-XXXX and contains letters and digits.");
            sendService.tryAgain(user.getId());
        }
    }

    private boolean isValidEtsCode(String command) {
        return terminService.validateToken(encode(command));
    }

    private void askForQualificationSubgroup(User user) {
        sendService.send(user, "What kind of appointment are you looking for?", qualificationSubgroupKeyboard);
    }

    private void askForZipCode(User user) {
        sendService.send(user, "Now please enter your zip code. I need this to search for open appointments in your area.");
    }

    private void askForRadius(User user) {
        sendService.send(user, "In which radius should I look for appointments?", radiusKeyboard);
    }

    private void askForQualificationGroup(User user) {
        sendService.send(user, "In which profession do you need an appointment?", qualificationGroupKeyboard);
    }

    private boolean setSubgroupIfUnambiguous(QualificationGroup group, Status status) {
        if (group.size() == 1) {
            QualificationSubgroup subgroup = group.getSubgroups().get(0);
            status.setQualificationSubgroup(subgroup);
            return true;
        }
        return false;
    }

    private String encode(String etsCode) {
        return new String(Base64.getEncoder().encode(etsCode.toUpperCase().getBytes()));
    }

    private static InlineKeyboardMarkup createRadiusKeyboard() {
        List<InlineKeyboardButton> buttons = POSSIBLE_RADII.stream().map(StartCommand::createRadiusButton).collect(Collectors.toList());
        return InlineKeyboardMarkup.builder()
                .keyboardRow(buttons).build();
    }

    private static InlineKeyboardButton createButton(String text, String callbackText) {
        return InlineKeyboardButton.builder()
                .text(text).callbackData(callbackText)
                .build();
    }

    private static InlineKeyboardButton createRadiusButton(Integer radius) {
        return createButton(radius.toString() + "km", radius.toString());
    }

    private InlineKeyboardMarkup createQualificationGroupKeyboard() {
        List<InlineKeyboardButton> buttons = qualificationGroups.values().stream().map(StartCommand::createQualificationGroupButton).toList();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        buttons.stream().map(List::of).forEach(keyboardBuilder::keyboardRow);
        return keyboardBuilder.build();
    }

    private static InlineKeyboardButton createQualificationGroupButton(QualificationGroup group) {
        return createButton(group.getName(), group.getName());
    }

    private InlineKeyboardMarkup createQualificationSubgroupKeyboard(Status status) {
        List<InlineKeyboardButton> buttons = status.getQualificationGroup().getSubgroups().stream().map(StartCommand::createQualificationSubgroupButton).toList();
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder = InlineKeyboardMarkup.builder();
        buttons.stream().map(List::of).forEach(keyboardBuilder::keyboardRow);
        return keyboardBuilder.build();
    }

    private static InlineKeyboardButton createQualificationSubgroupButton(QualificationSubgroup subgroup) {
        return createButton(subgroup.getName(), subgroup.getName());
    }

    @Data
    protected static class Status implements CommandStatus {
        private String token;
        private String zipCode;
        private String radius;
        private QualificationGroup qualificationGroup;
        private QualificationSubgroup qualificationSubgroup;

        @Override
        public boolean isFinished() {
            return token != null &&
                    zipCode != null &&
                    radius != null &&
                    qualificationGroup != null &&
                    qualificationSubgroup != null;
        }

        public SearchParameters toSearchParameters() {
            return SearchParameters.builder()
                    .token(token)
                    .zipCode(zipCode)
                    .radius(radius)
                    .qualificationSubgroup(qualificationSubgroup)
                    .build();
        }
    }
}
