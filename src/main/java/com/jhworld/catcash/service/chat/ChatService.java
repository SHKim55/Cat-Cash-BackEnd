package com.jhworld.catcash.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhworld.catcash.configuration.ChatGptConfig;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.chat.*;
import com.jhworld.catcash.dto.llm.GptRequest;
import com.jhworld.catcash.dto.llm.GptResponse;
import com.jhworld.catcash.entity.*;
import com.jhworld.catcash.repository.*;
import com.jhworld.catcash.service.llm.GptPrompt;
import com.jhworld.catcash.service.llm.GptRole;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final UserRepository userRepository;
    private final UserCatRepository userCatRepository;
    private final ChatRepository chatRepository;
    private final RecentChatRepository recentChatRepository;
    private final GptPrompt gptPrompt;
    private final JwtUtil jwtUtil;

    public ChatService(UserRepository userRepository, UserCatRepository userCatRepository,
                       ChatRepository chatRepository, RecentChatRepository recentChatRepository, GptPrompt gptPrompt, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userCatRepository = userCatRepository;
        this.chatRepository = chatRepository;
        this.recentChatRepository = recentChatRepository;
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

    public ResponseEntity<ChatResponseDTO> createMessage(String token, ChatRequestDTO userInput) throws JsonProcessingException {
        UserEntity userEntity = findUserByToken(token);

        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        Optional<UserCatEntity> catEntity = userCatRepository.findByUser(userEntity);
        if(catEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저의 고양이가 존재하지 않습니다");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message(GptRole.system, "#배경\n당신은 유저와 대화하는 챗봇입니다. 당신은 유저와의 대화를 읽고, 현재 유저의 마지막 질문을 문맥을 포함해 파악 한 후 어떤 주제에 대해 이야기하고 있는지 간략하게 출력하세요. 추가로 유저의 마지막 질문에 대한 답변을 3가지 다른 내용을 포함해 출력하세요. 답변은 json 형식이어야 합니다.\n#출력형식\n{\n\t\"topic\": \"유저의 마지막 질문이 어떤 대화 주제에 대한 것인지 간략한 키워드\",\n\t\"answer1\": \"유저의 질문에 대한 답변\",\n\t\"answer2\": \"답변1과 다른 방식으로 한 유저의 질문에 대한 답변\",\n\t\"answer3\": \"답변1, 답변2와 다른 방식으로 한 유저의 질문에 대한 답변\"}"));

        for(ChatDTO chatDTO: userInput.getMessages()) {
            messages.add(new GptRequest.Message(chatDTO.getRole().equals("user") ? GptRole.user : GptRole.assistant, chatDTO.getContent()));
        }

        GptRequest request = new GptRequest(ChatGptConfig.DEFAULT_MODEL, ChatGptConfig.TEMPERATURE, ChatGptConfig.MAx_TOKENS, messages, ChatGptConfig.TOP_P);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<GptRequest> entity = new HttpEntity<>(request, headers);

        System.out.println("gpt 콜 준비 완료");

        ResponseEntity<GptResponse> response;
        try {
            response = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entity, GptResponse.class);
        } catch (Exception e) {
            System.out.println("GPT API 콜 실패");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ChatGPT API 호출 중 오류 발생", e);
        }

        String responseText = response.getBody().getChoices().get(0).getMessage().getContent();

        System.out.println("response is " + responseText);

        int braceIndex = responseText.indexOf('{');
        if (braceIndex < 0) {
            throw new IllegalArgumentException("JSON 시작 '{' 문자를 찾을 수 없습니다.");
        }
        String jsonOnly = responseText.substring(braceIndex);

        // 2) Jackson ObjectMapper를 이용해 Map<String, String> 형태로 파싱
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> bridgeResult = mapper.readValue(
                jsonOnly, new TypeReference<Map<String, String>>() {}
        );

        Optional<RecentChatEntity> lastChat = recentChatRepository.findByUser(userEntity);
        String currEmbedding = getEmbedding(bridgeResult.get("topic"));
        if(lastChat.isEmpty() || isSimilar(lastChat.get().getEmbedding(), currEmbedding, 0.7)) {
            System.out.println("isSimilar");
        }
        else {
            System.out.println("isDifferent");
        }
        recentChatRepository.save(RecentChatEntity.builder()
                .content(bridgeResult.get("topic"))
                .user(userEntity)
                .createdTime(LocalDateTime.now())
                .userCat(catEntity.get())
                .embedding(currEmbedding)
                .build()
        );

        return null;
//
//        List<String> recentChats = new ArrayList<>();
//        for(ChatDTO chat:userInput.getMessages()) {
//            if(!chat.getRole().equals("user")) {
//                recentChats.clear();
//            }
//            else {
//                recentChats.add(chat.getContent());
//            }
//        }
//
//        for(String recentChat: recentChats) {
//            ChatEntity chatEntity = ChatEntity.builder()
//                    .content(recentChat)
//                    .user(userEntity)
//                    .createdTime(LocalDateTime.now())
//                    .role("user")
//                    .userCat(catEntity.get())
//                    .build();
//            chatRepository.save(chatEntity);
//        }
//
//
//        ChatEntity responseChat = chatRepository.save(
//                ChatEntity.builder()
//                        .content(responseText)
//                        .user(userEntity)
//                        .createdTime(LocalDateTime.now())
//                        .role("assistant")
//                        .userCat(catEntity.get())
//                        .build()
//        );
//
//
//        ChatDTO chatDTO = ChatDTO.builder()
//                .chatId(responseChat.getChatId())
//                .content(responseText)
//                .chatDate(LocalDateTime.now())
//                .role("assistant")
//                .build();
//
//        List<ChatDTO> chatDTOs = new ArrayList<ChatDTO>();
//        chatDTOs.add(chatDTO);
//        ChatResponseDTO chatResponseDTO = ChatResponseDTO.builder().messages(chatDTOs).build();
//
//        return ResponseEntity.ok(chatResponseDTO);
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
                    .chatDate(LocalDateTime.now())
                    .role(chat.getRole())
                    .build()
            );
        }

        return ResponseEntity.ok(chatDTOList);
    }

    private boolean isSimilar(String vector1, String vector2, double threshold) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        float[] vector1F = objectMapper.readValue(vector1, float[].class);
        float[] vector2F = objectMapper.readValue(vector2, float[].class);

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vector1F.length; i++) {
            dot += vector1F[i] * vector2F[i];
            normA += vector1F[i] * vector1F[i];
            normB += vector2F[i] * vector2F[i];
        }

        double result = normA == 0 || normB == 0 ? 0.0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
        return result > threshold;
    }

    private String getEmbedding(String keyContent) {
        String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
        String MODEL_NAME = "text-embedding-3-small";
        RestTemplate restTemplate = new RestTemplate();

        EmbeddingRequest requestBody = new EmbeddingRequest(MODEL_NAME, keyContent);

        // 2) HTTP 헤더 설정 (Bearer Authorization + Content-Type)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        // 3) HttpEntity 에 요청 바디와 헤더를 담아줍니다.
        HttpEntity<EmbeddingRequest> entity = new HttpEntity<>(requestBody, headers);

        // 4) RestTemplate 으로 POST 요청 보내고, EmbeddingResponse 클래스에 매핑받습니다.
        ResponseEntity<EmbeddingResponse> responseEntity = restTemplate.exchange(
                EMBEDDING_URL,
                HttpMethod.POST,
                entity,
                EmbeddingResponse.class
        );

        EmbeddingResponse response = responseEntity.getBody();
        if (response == null || response.getData().isEmpty()) {
            throw new RuntimeException("OpenAI Embedding API 응답이 비어있습니다.");
        }

        // 5) List<Float> → float[] 로 변환
        List<Float> floatList = response.getData().get(0).getEmbedding();
        float[] vector = new float[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
            vector[i] = floatList.get(i);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 벡터를 JSON 문자열로 직렬화하는 데 실패했습니다.", e);
        }
    }
}