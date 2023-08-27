package com.kobi.smartbot.service.vk;

import com.kobi.smartbot.config.VkBotProperties;
import com.kobi.smartbot.model.User;
import com.kobi.smartbot.model.VkMessage;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.messages.ConversationMember;
import com.vk.api.sdk.objects.messages.GetHistoryRev;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatService.class);

    private final VkBotProperties vkBotProperties;

    @Autowired
    public ChatService(VkBotProperties vkBotProperties) {
        this.vkBotProperties = vkBotProperties;
    }

    // Получаем список участников беседы
    public List<User> getChatUsers(String peerId) throws ClientException, ApiException {
        List<ConversationMember> members = vkBotProperties.getVkClient().messages()
                .getConversationMembers(vkBotProperties.getActor(), Integer.parseInt(peerId))
                .execute()
                .getItems();

        List<String> userIds = members.stream()
                .map(member -> String.valueOf(member.getMemberId()))
                .collect(Collectors.toList());
        List<GetResponse> usersInfo = vkBotProperties.getVkClient().users().get(vkBotProperties.getActor())
                .userIds(userIds)
                .fields(Fields.ONLINE,
                        Fields.FIRST_NAME_NOM,
                        Fields.LAST_NAME_NOM
                ).execute();

        return usersInfo.stream()
                .map(User::new)
                .collect(Collectors.toList());
    }


    public List<VkMessage> getChatMessages(Integer peer_id) {
        //размер диалога с шагом в 200 смс
        Integer N = 1;

        Integer count;
        List<VkMessage> chatVkMessages = new ArrayList<>();
        List<com.vk.api.sdk.objects.messages.Message> listMessage;
        try {
            int i = 0;
            int offset = 0;
            while (i < N) {
                GetHistoryResponse historyResponse = vkBotProperties.getVkClient().messages().getHistory(vkBotProperties.getActor())
                        .unsafeParam("v", vkBotProperties.getVersion())
                        .peerId(peer_id)
                        .count(200)
                        .offset(offset)
                        .rev(GetHistoryRev.REVERSE_CHRONOLOGICAL)
                        .execute();

                if (i == 0) {
                    count = historyResponse.getCount();
                    if (count > 400)
                        N = count / 200;
                }
                listMessage = historyResponse.getItems();
                for (com.vk.api.sdk.objects.messages.Message msg : listMessage) {
                    VkMessage vkMessage = new VkMessage(msg);
                    vkMessage.setPeerId(Long.valueOf(peer_id));
                    chatVkMessages.add(vkMessage);
                }
                TimeUnit.MILLISECONDS.sleep(300);
                offset += 200;
                i++;
            }
        } catch (ApiException | ClientException | InterruptedException e) {
            LOG.error("Сбой", e);
        }
        return chatVkMessages;
    }
}
