package com.jhworld.catcash.service.chat;

import com.jhworld.catcash.configuration.ChatGptConfig;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.chat.ChatDTO;
import com.jhworld.catcash.dto.llm.GptRequest;
import com.jhworld.catcash.dto.llm.GptResponse;
import com.jhworld.catcash.entity.ChatEntity;
import com.jhworld.catcash.entity.UserCatEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.ChatRepository;
import com.jhworld.catcash.repository.UserCatRepository;
import com.jhworld.catcash.repository.UserCategoryRepository;
import com.jhworld.catcash.repository.UserRepository;
import com.jhworld.catcash.service.llm.GptPrompt;
import com.jhworld.catcash.service.llm.GptRole;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final UserRepository userRepository;
    private final UserCatRepository userCatRepository;
    private final ChatRepository chatRepository;
    private final GptPrompt gptPrompt;
    private final JwtUtil jwtUtil;

    public ChatService(UserRepository userRepository, UserCatRepository userCatRepository,
                       ChatRepository chatRepository, GptPrompt gptPrompt, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userCatRepository = userCatRepository;
        this.chatRepository = chatRepository;
        this.gptPrompt = gptPrompt;
        this.jwtUtil = jwtUtil;
    }

    @Value("${chatgpt.api.key}")
    private String gptApiKey;

    private UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    public ResponseEntity<ChatDTO> createMessage(String token, String userText) {
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        GptRequest request = new GptRequest(
                ChatGptConfig.DEFAULT_MODEL, ChatGptConfig.TEMPERATURE, ChatGptConfig.MAx_TOKENS,
                List.of(new GptRequest.Message(GptRole.system, gptPrompt.getPrompt(true, userText)))
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<GptRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<GptResponse> response = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entity, GptResponse.class);

        String responseText = response.getBody().getChoices().get(0).getMessage().getContent();

        UserCatEntity userCatEntity = userCatRepository.findByUser(userEntity).orElse(null);
        if(userCatEntity == null) {
            System.out.println("Error: User cat not found");
            return ResponseEntity.status(404).body(null);
        }

        ChatEntity chatEntity = ChatEntity.builder()
                .content(userText + "\n" + responseText)
                .user(userEntity)
                .createdTime(LocalDateTime.now())
                .build();
        chatEntity = chatRepository.save(chatEntity);

        ChatDTO chatDTO = ChatDTO.builder()
                .chatId(chatEntity.getChatId())
                .content(responseText)
                .chatTime(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(chatDTO);
    }

    public ResponseEntity<List<ChatDTO>> loadMessage(String token) {
        UserEntity userEntity = findUserByToken(token);
        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        List<ChatEntity> chatEntityList = chatRepository.findAllByUser(userEntity);
        List<ChatDTO> chatDTOList = new ArrayList<>();
        for(ChatEntity chat : chatEntityList) {
            chatDTOList.add(ChatDTO.builder()
                    .chatId(chat.getChatId())
                    .content(chat.getContent())
                    .chatTime(LocalDateTime.now())
                    .build()
            );
        }

        return ResponseEntity.ok(chatDTOList);
    }
}