package com.example.telegrambot;

import com.example.telegrambot.dto.VacancyDto;
import com.example.telegrambot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VacanciesBot extends TelegramLongPollingBot {
   @Autowired
   private VacancyService vacancyService;

   private final Map<Long, String>  lastShownVacancyLevel = new HashMap<>();

    public VacanciesBot() {
        super("your token");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if(update.getMessage() != null){
                handleStartCommand(update);
            }
            //обробник callbackData
            if(update.getCallbackQuery() != null){
                String callbackData = update.getCallbackQuery().getData();
                if("showJuniorVacancies".equals(callbackData)){
                    showJuniorVacancies(update);
                } else if ("showMiddleVacancies".equals(callbackData)) {
                    showMiddleVacancies(update); 
                } else if ("showSeniorVacancies".equals(callbackData)) {
                    showSeniorVacancies(update);
                }else if (callbackData.startsWith("vacancyId=")){
                    String id = callbackData.split("=")[1];
                    showVacancyDescription(id, update);
                } else if("backToVacancies".equals(callbackData)){
                    handleBackToVacanciesCommand(update);
                } else if ("backToStartMenu".equals(callbackData)) {
                    handleBackToStartCommand(update);
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }
    private void handleBackToStartCommand(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Choose title:");
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
        sendMessage.setReplyMarkup(getStartMenu());
        execute(sendMessage);
    }
    private void handleBackToVacanciesCommand(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        //отримуємо той title що був обраний перед цим додаємо мар
        String level = lastShownVacancyLevel.get(chatId);

        if("junior".equals(level)){
            showJuniorVacancies(update);
        }else if("middle".equals(level)){
            showMiddleVacancies(update);
        }else if("senior".equals(level)){
            showSeniorVacancies(update);
        }
    }

    //Выводит короткое и длиное описание вакансии
    //Выводит кнопки Назад к меню и Назад к вакансиям
    private void showVacancyDescription(String id, Update update) throws TelegramApiException {
        VacancyDto vacancyDto = vacancyService.get(id);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());

        String vacancyInfo = """
                *Title:* %s
                *Company:* %s
                *Short Description:* %s
                *Description:* %s
                *Salary:* %s
                *Link:* [%s](%s)
                """.formatted(
                        escapeMarkdownReversedChars(vacancyDto.getTitle()),
                        escapeMarkdownReversedChars(vacancyDto.getCompany()),
                        escapeMarkdownReversedChars(vacancyDto.getShortDescription()),
                        escapeMarkdownReversedChars(vacancyDto.getLongDescription()),
                        vacancyDto.getSalary().isBlank() ? "Not specified" : escapeMarkdownReversedChars(vacancyDto.getSalary()),
                        "Click here for more details",
                        escapeMarkdownReversedChars(vacancyDto.getLink())

        );
        System.out.println(vacancyInfo);
        sendMessage.setText(vacancyInfo);
        sendMessage.setParseMode(ParseMode.MARKDOWNV2);
        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
        execute(sendMessage);

//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
//        VacancyDto vacancy = vacancyService.get(id);
//        String description = vacancy.getShortDescription();
//        sendMessage.setText(description);
//        sendMessage.setReplyMarkup(getBackToVacanciesMenu());
//        execute(sendMessage);
    }
    //екранування символів
    private String escapeMarkdownReversedChars(String text){
        return text.replace("-", "\\-")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\{")
                .replace(")", "\\}")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
    //Создает кнопки для возврата в меню или кнопкам вакансий
    private ReplyKeyboard getBackToVacanciesMenu(){
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton backToVacanciesButton = new InlineKeyboardButton();
        backToVacanciesButton.setText("Back to vacancies");
        //setCallbackData дает возможность отличить кнопки
        backToVacanciesButton.setCallbackData("backToVacancies");
        row.add(backToVacanciesButton);

        InlineKeyboardButton backToStartMenuButton = new InlineKeyboardButton();
        backToStartMenuButton.setText("Back to start menu");
        backToStartMenuButton.setCallbackData("backToStartMenu");
        row.add(backToStartMenuButton);

        return new InlineKeyboardMarkup(List.of(row));
    }

    //Выводит вакансии в виде кнопок
    private void showJuniorVacancies(Update  update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy:");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getJuniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "junior");
    }

    private ReplyKeyboard getJuniorVacanciesMenu(){
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<VacancyDto> vacancies = vacancyService.getJuniorVacancies();

        for (VacancyDto vacancy : vacancies){
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());

            row.add(vacancyButton);
        }
//        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
//        maVacancy.setText("Junior Java developer at MA");
//        maVacancy.setCallbackData("vacancyId=1");
//        row.add(maVacancy);
//
//        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
//        googleVacancy.setText("Junior Java developer at Google");
//        googleVacancy.setCallbackData("vacancyId=2");
//        row.add(googleVacancy);

        //Создает кнопки
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));

        return keyboard;
    }
    //Выводит вакансии в виде кнопок
    private void showMiddleVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy: ");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getMiddleVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "middle");
    }
    private ReplyKeyboard getMiddleVacanciesMenu(){
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<VacancyDto> vacancies = vacancyService.getMiddleVacancies();

        for (VacancyDto vacancy : vacancies){
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());

            row.add(vacancyButton);
        }
//        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
//        maVacancy.setText("Middle Java developer at MA");
//        maVacancy.setCallbackData("vacancyId=3");
//        row.add(maVacancy);
//
//        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
//        googleVacancy.setText("Middle Java developer at Google");
//        googleVacancy.setCallbackData("vacancyId=4");
//        row.add(googleVacancy);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));

        return keyboard;
    }
    //Выводит вакансии в виде кнопок
    private void showSeniorVacancies(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Please chose vacancy: ");
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSeniorVacanciesMenu());
        execute(sendMessage);

        lastShownVacancyLevel.put(chatId, "senior");
    }
    private ReplyKeyboard getSeniorVacanciesMenu(){
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<VacancyDto> vacancies = vacancyService.getSeniorVacancies();

        for (VacancyDto vacancy : vacancies){
            InlineKeyboardButton vacancyButton = new InlineKeyboardButton();
            vacancyButton.setText(vacancy.getTitle());
            vacancyButton.setCallbackData("vacancyId=" + vacancy.getId());

            row.add(vacancyButton);
        }
//        InlineKeyboardButton maVacancy = new InlineKeyboardButton();
//        maVacancy.setText("Senior Java developer at MA");
//        maVacancy.setCallbackData("vacancyId=5");
//        row.add(maVacancy);
//
//        InlineKeyboardButton googleVacancy = new InlineKeyboardButton();
//        googleVacancy.setText("Senior Java developer at Google");
//        googleVacancy.setCallbackData("vacancyId=6");
//        row.add(googleVacancy);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));

        return keyboard;
    }
    //Вызывает стартовое меню
    private void handleStartCommand(Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Welcome to vacancies bot! Please, choose your title:");
        sendMessage.setReplyMarkup(getStartMenu());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Выводит кнопки для стартового меню
    private ReplyKeyboard getStartMenu() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton junior = new InlineKeyboardButton();
        junior.setText("Junior");
        junior.setCallbackData("showJuniorVacancies");
        row.add(junior);

        InlineKeyboardButton middle = new InlineKeyboardButton();
        middle.setText("Middle");
        middle.setCallbackData("showMiddleVacancies");
        row.add(middle);

        InlineKeyboardButton senior = new InlineKeyboardButton();
        senior.setText("Senior");
        senior.setCallbackData("showSeniorVacancies");
        row.add(senior);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));

        return keyboard;
    }

    @Override
    public String getBotUsername() {
        return "TestedBot";
    }
}
