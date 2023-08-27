package com.kobi.smartbot.service;

import com.kobi.smartbot.integrations.GoogleService;
import com.kobi.smartbot.integrations.OpenExchangeRatesService;
import com.kobi.smartbot.integrations.YandexService;
import com.kobi.smartbot.integrations.gpt.GptService;
import com.kobi.smartbot.integrations.gpt.response.ImageGenerationResponse;
import com.kobi.smartbot.integrations.weather.OpenWeatherMapService;
import com.kobi.smartbot.model.AbstractMessage;
import com.kobi.smartbot.model.TgMessage;
import com.kobi.smartbot.model.User;
import com.kobi.smartbot.model.VkMessage;
import com.kobi.smartbot.model.enums.*;
import com.kobi.smartbot.service.tg.TgCommandService;
import com.kobi.smartbot.service.vk.FriendsService;
import com.kobi.smartbot.service.vk.UserService;
import com.kobi.smartbot.service.vk.VkCommandService;
import com.kobi.smartbot.util.Helper;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@Service
public class CommandExecuterService {

    private static final Logger LOG = LoggerFactory.getLogger(CommandExecuterService.class);

    private static final Pattern detectRegex = Pattern.compile("\\bя\\b|\\bменя\\b|\\bмне\\b|\\bмой\\b|\\bмоя\\b|\\bмоё\\b|\\bмою\\b|\\bмоего\\b",
            Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);


    private final Helper helper;
    private final VkCommandService vkService;
    private final TgCommandService tgService;
    private final YandexService yandexService;
    private final OpenExchangeRatesService exchangeRatesService;
    private final UserService userService;
    private final GptService gptService;
    private final OpenWeatherMapService weatherService;
    private final GoogleService googleService;
    private final FriendsService friendsService;

    @Autowired
    public CommandExecuterService(Helper helper,
                                  VkCommandService vkService,
                                  YandexService yandexService,
                                  UserService userService,
                                  GptService gptService,
                                  OpenWeatherMapService weatherService,
                                  TgCommandService tgService,
                                  GoogleService googleService,
                                  OpenExchangeRatesService exchangeRatesService,
                                  FriendsService friendsService) {
        this.helper = helper;
        this.vkService = vkService;
        this.yandexService = yandexService;
        this.userService = userService;
        this.gptService = gptService;
        this.weatherService = weatherService;
        this.tgService = tgService;
        this.googleService = googleService;
        this.friendsService = friendsService;
        this.exchangeRatesService = exchangeRatesService;
    }

    public List<AbstractMessage> execute(AbstractMessage msg) throws ClientException, ApiException, IOException, ParseException {
        MessageTypeEnum type = msg.getMessageType();
        String source = msg.getSourceType().equals(SourceTypeEnum.TELEGRAM) ? "TG" : "VK";
        if (source.equals("VK")) {
            String fio = userService.getFio(msg.getUserId(), source);
            msg.setFio(fio);
            msg.setUserName(fio);
        }
        List<AbstractMessage> listMsg = new ArrayList<>();

        if (type instanceof UserEnum) {
            UserEnum typeUsr = (UserEnum) type;
            switch (typeUsr) {
                case USER_IGNORE -> {
                    return listMsg;
                }
            }
        }

        if (type instanceof CommandEnum) {
            CommandEnum typeCom = (CommandEnum) type;

            AbstractMessage answer = null;
            switch (typeCom) {
                case DVACH -> answer = vkService.getAnswerCommandDVACH(msg);
                case MEM -> answer = vkService.getAnswerCommandMEM(msg);
                case VIDEO -> answer = vkService.getAnswerCommandVIDEO(msg);
                case GIF -> answer = vkService.getAnswerCommandGIF(msg);
                case SAY -> answer = getAnswerCommandSAY(msg);
                case WEATHER -> answer = getAnswerCommandWEATHER(msg);
                case RATES -> answer = getAnswerCommandRates(msg);
                case ONLINE -> answer = getAnswerCommandOnline(msg);
                case DETECTOR -> answer = getAnswerCommandDETECTOR(msg);
                case SEARCH -> answer = getAnswerCommandSEARCH(msg);
                case IMAGES -> answer = getImageChatGPT(msg);
                case RESET -> answer = resetDialog(msg);
                case COMMANDS -> answer = getAnswerCommandCOMMANDS(msg);
                case EXECUTE -> answer = executeCommandRoot(msg);
                default -> LOG.debug("No valid command found. Returning null.");
            }
            listMsg.add(answer);
        }

        if (OtherEnum.NAME_BOT.equals(type)) {
            if (isVkSourceType(msg.getSourceType())) {
                friendsService.checkRequestToFriends(msg);
            }
            listMsg = getAnswerChatGPT(msg);
        }

        return listMsg;
    }

    private AbstractMessage getAnswerCommandCOMMANDS(AbstractMessage msg) {
        StringBuilder strAnswer = new StringBuilder("Список команд: \n");

        Stream<CommandEnum> commandEnumStream = Arrays.stream(CommandEnum.values());
        String answer = "Команды для этой платформы недоступны";

        if (isTgSourceType(msg.getSourceType())) {
            answer = commandEnumStream
                    .filter(commandEnum -> "all".equals(commandEnum.getType()))
                    .map(CommandEnum::getDescription)
                    .collect(Collectors.joining("\n\n"));
        }
        if (isVkSourceType(msg.getSourceType())) {
            answer = commandEnumStream
                    .map(CommandEnum::getDescription)
                    .collect(Collectors.joining("\n"));
        }
        strAnswer.append(answer);
        msg.setText(strAnswer.toString());

        return msg;
    }

    public AbstractMessage getAnswerCommandSAY(AbstractMessage msg) throws IOException, ParseException, ClientException, ApiException {
        String text = msg.getText();
        if (!text.isEmpty()) {
            byte[] bytes = yandexService.speech(text);
            if (isVkSourceType(msg.getSourceType())) {
                String attach = vkService.getAnswerCommandSAY(bytes);
                List<String> attachList = new ArrayList<>();
                attachList.add(attach);
                msg.setAttachments(attachList);
            }
            if (isTgSourceType(msg.getSourceType())) {
                File voiceFile = File.createTempFile("voice", ".ogg");
                Files.write(voiceFile.toPath(), bytes, StandardOpenOption.WRITE);
                InputFile voiceInputFile = new InputFile(new FileInputStream(voiceFile), voiceFile.getName());
                ((TgMessage) msg).setInputFile(voiceInputFile);
            }
        } else {
            msg.setText("Что мне сказать?");
        }
        return msg;
    }

    private AbstractMessage getAnswerCommandRates(AbstractMessage msg) {
        String answer = exchangeRatesService.getExchangeRates();
        msg.setText(answer);
        return msg;
    }

    private AbstractMessage getAnswerCommandOnline(AbstractMessage msg) throws ApiException, ClientException {
        String answer = "Модуль не подключен";
        List<User> users = null;

        if (isTgSourceType(msg.getSourceType())) {
            msg.setText("Модуль не подключен");
            return msg;
        }

        if (isVkSourceType(msg.getSourceType())) {
            users = vkService.getAllUserList(msg);
        }

        if (users != null) {
            StringBuilder strAnswer = users.stream()
                    .filter(User::isOnline)
                    .map(user -> "[id" + user.getUserId() + "|" + user.getFirstName() + " " + user.getLastName() + "]")
                    .collect(Collectors.collectingAndThen(Collectors.joining("\n"), StringBuilder::new));

            answer = strAnswer.length() > 0 ? strAnswer.toString() : "Нет никого!";
        }

        msg.setText(answer);
        return msg;
    }

    private AbstractMessage getAnswerCommandDETECTOR(AbstractMessage msg) {
        StringBuilder answer = new StringBuilder("Уровень ЧСВ: \n");

        Map<String, Integer> mapCountLiter = new HashMap<>();
        Map<String, Integer> mapCountMsg = new HashMap<>();
        List<AbstractMessage> listAbsMsg = new ArrayList<>();

        if (isVkSourceType(msg.getSourceType())) {
            listAbsMsg = vkService.getAnswerCommandDETECTOR(msg);
        }
        if (isTgSourceType(msg.getSourceType())) {
            msg.setText("Модуль не подключен");
            return msg;
        }
        for (AbstractMessage item : listAbsMsg) {
            String userId = item.getUserId().toString();
            String text = item.getText();

            int count = countDetectorOccurrences(text);

            mapCountMsg.merge(userId, 1, Integer::sum);
            mapCountLiter.merge(userId, count, Integer::sum);
        }

        Map<String, Integer> sortedMap = sortMapByValueDesc(mapCountLiter);
        computePercentage(sortedMap, mapCountMsg);

        List<String> userIds = new ArrayList<>(sortedMap.keySet());
        Map<String, String> users = userService.getMapUsers(userIds);

        appendAnswer(answer, sortedMap, mapCountMsg, users);
        answer.append("\nВсего сообщений обработано: ").append(listAbsMsg.size()).append(" \n");

        msg.setText(answer.toString());

        return msg;
    }

    private int countDetectorOccurrences(String text) {
        int count = 0;
        Matcher m = detectRegex.matcher(text);
        while (m.find()) count++;
        return count;
    }

    private Map<String, Integer> sortMapByValueDesc(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e2, e1) -> e2, LinkedHashMap::new));
    }

    private void computePercentage(Map<String, Integer> mapCountLiter, Map<String, Integer> mapCountMsg) {
        for (String key : mapCountLiter.keySet()) {
            int countMsg = mapCountMsg.get(key);
            if (countMsg > 30) {
                int countLiter = mapCountLiter.get(key);
                int value = countLiter * 100 / countMsg;
                mapCountLiter.put(key, value);
            } else {
                mapCountLiter.put(key, 0);
            }
        }
    }

    private void appendAnswer(StringBuilder answer, Map<String, Integer> sortedMap, Map<String, Integer> mapCountMsg, Map<String, String> users) {
        for (String key : sortedMap.keySet()) {
            int countMsg = sortedMap.get(key);
            if (countMsg != 0) {
                answer.append(users.get(key)).append(" - ").append(countMsg).append(" (Сообщений: ").append(mapCountMsg.get(key)).append(")\n");
            }
        }
    }

    private AbstractMessage getAnswerCommandWEATHER(AbstractMessage msg) {
        String cityName = msg.getText();
        String answer;
        if (!cityName.isEmpty()) {
            answer = weatherService.getWeather(msg.getText());
        } else {
            answer = "не указан населенный пункт";
        }
        msg.setText(answer);
        return msg;
    }

    public AbstractMessage getImageChatGPT(AbstractMessage msg) {
        ImageGenerationResponse response = gptService.getImageChatGPT(msg);
        if (isVkSourceType(msg.getSourceType())) {
            ((VkMessage) msg).setForward(true);
            List<String> attachments = vkService.getPhotos(response);
            msg.setAttachments(attachments);
            if (attachments.isEmpty()) {
                msg.setText("Сбой соединения");
            }
        }

        if (isTgSourceType(msg.getSourceType())) {
            List<InputMedia> photos = tgService.getPhotos(response);
            ((TgMessage) msg).setPhotos(photos);
        }
        return msg;
    }

    private AbstractMessage getAnswerCommandSEARCH(AbstractMessage msg) {
        String answer = "Ошибка поиска!";
        String searchText = msg.getText();
        if (!searchText.isEmpty()) {
            try {
                answer = googleService.googleSearch(searchText);
            } catch (GeneralSecurityException | IOException e) {
                LOG.error("", e);
            }
        } else {
            answer = "Что мне искать?";
        }
        msg.setText(answer);
        return msg;
    }

    private List<AbstractMessage> getAnswerChatGPT(AbstractMessage msg) {
        String answer = gptService.getAnswerChatGPT(msg);

        if (isVkSourceType(msg.getSourceType())) {
            ((VkMessage) msg).setForward(true);
        }
        List<String> answers = helper.splitAnswer(answer);

        List<AbstractMessage> listMsg = new ArrayList<>();
        for (String item : answers) {
            AbstractMessage newMsg = msg.clone();
            newMsg.setText(item);
            listMsg.add(newMsg);
        }
        return listMsg;
    }

    AbstractMessage executeCommandRoot(AbstractMessage msg) {
        String answer = "Команда выполнена";

//        fillUsers();

        msg.setText(answer);
        return msg;
    }

    private void fillUsers() {
        userService.fillUsers();
    }

    private AbstractMessage resetDialog(AbstractMessage msg) {
        String answer = gptService.resetDialog(msg);
        msg.setText(answer);
        return msg;
    }

    private boolean isVkSourceType(SourceTypeEnum sourceType) {
        return SourceTypeEnum.VK_CHAT.equals(sourceType) || SourceTypeEnum.VK_GROUP.equals(sourceType);
    }

    private boolean isTgSourceType(SourceTypeEnum sourceType) {
        return SourceTypeEnum.TELEGRAM.equals(sourceType);
    }

}
