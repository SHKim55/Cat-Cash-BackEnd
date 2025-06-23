package com.jhworld.catcash.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jhworld.catcash.configuration.ChatGptConfig;
import com.jhworld.catcash.configuration.JwtUtil;
import com.jhworld.catcash.dto.chat.*;
import com.jhworld.catcash.dto.llm.GptRequest;
import com.jhworld.catcash.dto.llm.GptResponse;
import com.jhworld.catcash.entity.*;
import com.jhworld.catcash.entity.pg.VectorChatEntity;
import com.jhworld.catcash.repository.*;
import com.jhworld.catcash.pg.repository.VectorChatRepository;
import com.jhworld.catcash.service.llm.GptPrompt;
import com.jhworld.catcash.service.llm.GptRole;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import com.jhworld.catcash.entity.ChatEntity;
import com.jhworld.catcash.entity.UserCatEntity;
import com.jhworld.catcash.entity.UserEntity;
import com.jhworld.catcash.repository.ChatRepository;
import com.jhworld.catcash.repository.UserCatRepository;
import com.jhworld.catcash.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {
    private final UserRepository userRepository;
    private final UserCatRepository userCatRepository;
    private final ChatRepository chatRepository;
    private final RecentChatRepository recentChatRepository;
    private final GptPrompt gptPrompt;
    private final JwtUtil jwtUtil;
    private final VectorChatRepository vectorChatRepository;
    private final ExpenditureRepository expenditureRepository;
    private final ChatEventRepository chatEventRepository;

    public ChatService(UserRepository userRepository, UserCatRepository userCatRepository,
                       ChatRepository chatRepository, RecentChatRepository recentChatRepository, GptPrompt gptPrompt, JwtUtil jwtUtil, VectorChatRepository vectorChatRepository, ExpenditureRepository expenditureRepository, ChatEventRepository chatEventRepository) {
        this.userRepository = userRepository;
        this.userCatRepository = userCatRepository;
        this.chatRepository = chatRepository;
        this.recentChatRepository = recentChatRepository;
        this.gptPrompt = gptPrompt;
        this.jwtUtil = jwtUtil;
        this.vectorChatRepository = vectorChatRepository;
        this.expenditureRepository = expenditureRepository;
        this.chatEventRepository = chatEventRepository;
    }

    @Value("${chatgpt.api.key}")
    private String gptApiKey;

    @Value("${anthropic.api.key}")
    private String claudeApiKey;

    @Value("${weather_api}")
    private String weatherApiKey;

    private UserEntity findUserByToken(String token) {
        Claims claims = jwtUtil.extractTokenValue(token);
        String userSeq = claims.get("sub", String.class); // userSeq(username) 추출

        Optional<UserEntity> optionalUserEntity = userRepository.findByUserSequence(userSeq);
        return optionalUserEntity.orElse(null);
    }

    public ResponseEntity<ChatResponseDTO> createMessage(String token, ChatRequestDTO userInput) throws JsonProcessingException {
        // 설정
        UserEntity userEntity = findUserByToken(token);

        if(userEntity == null) {
            System.out.println("Error: User not found");
            return ResponseEntity.status(404).body(null);
        }

        Optional<UserCatEntity> catEntity = userCatRepository.findByUser(userEntity);
        if(catEntity.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저의 고양이가 존재하지 않습니다");
        }

        // Hyde 방식을 위한 gpt 콜
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message(GptRole.system, ChatPrompt.getBridge()));

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

        System.out.println("response text is " + responseText);

        int braceIndex = responseText.indexOf('{');
        if (braceIndex < 0) {
            responseText = "{\"topic\":\"기억\"," + "\"answer1\": \"" + responseText +"\"," + "\"answer2\": \"" + responseText +"\"," + "\"answer3\": \"" + responseText +"\""  + "}";
            System.out.println(responseText);
        }
        braceIndex = responseText.indexOf('{');
        String jsonOnly = responseText.substring(braceIndex);

        System.out.println("bidge input json " + jsonOnly);

        jsonOnly = jsonOnly.replaceAll(",(?=\\s*\\})", "");

        System.out.println("removed json only " + jsonOnly);

        // 2) Jackson ObjectMapper를 이용해 Map<String, String> 형태로 파싱
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> bridgeResult = mapper.readValue(
                jsonOnly, new TypeReference<Map<String, String>>() {}
        );

        // 비슷한 경우 저장. 다른 경우 청크를 벡터화하고, 초기화
        Optional<RecentChatEntity> lastChat = recentChatRepository.findFirstByUserOrderByCreatedTimeDesc(userEntity);
        List<RecentChatEntity> allByUserOrderByCreatedTimeAsc = recentChatRepository.findAllByUserOrderByCreatedTimeAsc(userEntity);
        String currEmbedding = getEmbedding(bridgeResult.get("topic"));
        if(lastChat.isEmpty() || isSimilar(lastChat.get().getEmbedding(), currEmbedding, 0.7)) {
            System.out.println("isSimilar");
        }
        else {
            List<GptRequest.Message> summary = new ArrayList<>();
            summary.add(new GptRequest.Message(GptRole.system, ChatPrompt.getSummary()));

            for(RecentChatEntity recentChat: allByUserOrderByCreatedTimeAsc) {
                summary.add(new GptRequest.Message(recentChat.getRole().equals("user") ? GptRole.user : GptRole.assistant, recentChat.getChat()));
            }

            GptRequest summaryRequest = new GptRequest(ChatGptConfig.DEFAULT_MODEL, ChatGptConfig.TEMPERATURE, ChatGptConfig.MAx_TOKENS, summary, ChatGptConfig.TOP_P);
            HttpEntity<GptRequest> summaryEntity = new HttpEntity<>(summaryRequest, headers);

            ResponseEntity<GptResponse> summaryResponse = restTemplate.postForEntity(ChatGptConfig.BASE_URL, summaryEntity, GptResponse.class);

            String summaryText = summaryResponse.getBody().getChoices().get(0).getMessage().getContent();
            System.out.println("summary text is " + summaryText);

            String summaryEmbedding = this.getEmbedding(summaryText);

            this.vectorChatRepository.save(VectorChatEntity.builder()
                    .embedding(this.parseEmbeddingVector(summaryEmbedding))
                    .userId(userEntity.getUserId())
                    .createdTime(LocalDateTime.now())
                    .content(summaryText)
                    .build()
            );

            this.recentChatRepository.deleteAllByUser(userEntity);

            System.out.println("isDifferent");
        }

        String emb1 = getEmbedding(bridgeResult.get("answer1"));
        String emb2 = getEmbedding(bridgeResult.get("answer2"));
        String emb3 = getEmbedding(bridgeResult.get("answer3"));

        List<VectorChatEntity> sims = vectorChatRepository
                .findNearestByUserAnyOfThreeWithinThreshold(
                        userEntity.getUserId(),
                        emb1, emb2, emb3,
                        0.7f,    // threshold
                        5        // limit
                );

        for(VectorChatEntity vectorChatEntity: sims) {
            System.out.println(vectorChatEntity.getContent());
        }

        // 가장 마지막 유저 입력 저장
        List<String> recentChats = new ArrayList<>();
        for(ChatDTO chat:userInput.getMessages()) {
            if(!chat.getRole().equals("user")) {
                recentChats.clear();
            }
            else {
                recentChats.add(chat.getContent());
            }
        }

        for(String recentChat: recentChats) {
            recentChatRepository.save(RecentChatEntity.builder()
                    .content(bridgeResult.get("topic"))
                    .user(userEntity)
                    .createdTime(LocalDateTime.now())
                    .userCat(catEntity.get())
                    .embedding(currEmbedding)
                    .chat(recentChat)
                    .role("user")
                    .build()
            );

            chatRepository.save(
                    ChatEntity.builder()
                            .content(recentChat)
                            .user(userEntity)
                            .createdTime(LocalDateTime.now())
                            .role("user")
                            .userCat(catEntity.get())
                            .build()
            );
        }

        // 클로드로 채팅 응답 받기
        String output = this.getChatCompletion(this.buildClaudeMessages(userInput.getMessages()), userEntity, catEntity.get(), userInput.getStatus());
        if(output.equals("down")) {
            List<GptRequest.Message> tempMessage = new ArrayList<>();
            String prompt = this.makeClaudePrompt(userEntity, catEntity.get(), userInput.getStatus());
            tempMessage.add(new GptRequest.Message(GptRole.system, prompt));
            for(ChatDTO chatDTO: userInput.getMessages()) {
                tempMessage.add(new GptRequest.Message(chatDTO.getRole().equals("user") ? GptRole.user : GptRole.assistant, chatDTO.getContent()));
            }
            GptRequest requesttemp = new GptRequest("gpt-4.1-mini-2025-04-14", 0.8, 500, tempMessage, 0.9);
            HttpEntity<GptRequest> entityTemp = new HttpEntity<>(requesttemp, headers);
            ResponseEntity<GptResponse> responseTemp;
            try {
                responseTemp = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entityTemp, GptResponse.class);
            } catch (Exception e) {
                System.out.println("Gpt Apit Call 실패");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "", e);
            }
            output = responseTemp.getBody().getChoices().get(0).getMessage().getContent();
            System.out.println("gpt 우회!");
        }
        System.out.println("output is " + output);

        recentChatRepository.save(RecentChatEntity.builder()
                .content(bridgeResult.get("topic"))
                .user(userEntity)
                .createdTime(LocalDateTime.now())
                .userCat(catEntity.get())
                .embedding(currEmbedding)
                .chat(output)
                .role("assistant")
                .build()
        );

        List<String> responseChatList = new ArrayList<>();
        if (output == null || output.isEmpty()) {
            responseChatList.add("");
        }
        else {
            final int MAX_LENGTH = 20;
            assert output != null;
            if (output.length() <= MAX_LENGTH) {
                responseChatList.add(output);
            }
            // 길 경우 GPT를 호출해서 자르기
            else {
                try {
                    List<String> gptSplit = callGptToSplit(output);
                    responseChatList.addAll(gptSplit);
                } catch (Exception e) {
                    // GPT 호출 실패 시 fallback
                    responseChatList.add(output);
                }
            }
        }

        List<ChatDTO> returnList = new ArrayList<ChatDTO>();


        for(String chat: responseChatList) {
            ChatEntity responseChat = chatRepository.save(
                    ChatEntity.builder()
                            .content(chat)
                            .user(userEntity)
                            .createdTime(LocalDateTime.now())
                            .role("assistant")
                            .userCat(catEntity.get())
                            .build()
            );

            returnList.add(ChatDTO.builder()
                    .chatId(responseChat.getChatId())
                    .content(responseChat.getContent())
                    .chatDate(responseChat.getCreatedTime())
                    .role(responseChat.getRole())
                    .build()
            );
        }

        saveEvent(userInput, userEntity);

        return ResponseEntity.ok(ChatResponseDTO.builder().messages(returnList).build());
    }

    private List<String> callGptToSplit(String output) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message(GptRole.system, ChatPrompt.split()));
        messages.add(new GptRequest.Message(GptRole.user, output));

        GptRequest request = new GptRequest("gpt-4.1-mini-2025-04-14", 0.0, ChatGptConfig.MAx_TOKENS, messages, 1.0);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<GptRequest> entity = new HttpEntity<>(request, headers);


        ResponseEntity<GptResponse> response;
        try {
            response = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entity, GptResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ChatGPT API 호출 중 오류 발생", e);
        }

        String responseText = response.getBody().getChoices().get(0).getMessage().getContent();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseText);

        JsonNode responseArray = root.path("response");

        List<String> result = new ArrayList<>();
        if (responseArray.isArray()) {
            for (JsonNode item : responseArray) {
                result.add(item.asText());
            }
        }

        return result;
    }

    private ResponseEntity<Void> saveEvent(ChatRequestDTO chatRequestDTO, UserEntity user) throws JsonProcessingException {
        LocalDate today = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, E", Locale.KOREAN);
        String formatted = today.format(formatter);

        System.out.println(formatted);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptApiKey);

        List<GptRequest.Message> messages = new ArrayList<>();
        messages.add(new GptRequest.Message(GptRole.system, ChatPrompt.makeEvent()));

        int size = chatRequestDTO.getMessages().length;
        int fromIndex = Math.max(0, size - 10);

        ChatDTO[] last10 = Arrays.copyOfRange(chatRequestDTO.getMessages(), fromIndex, size);

        String messageStr = "";
        for(ChatDTO chatDTO: last10) {
            if(chatDTO.getRole().equals("user")) {
                messageStr+=("user: " + chatDTO.getContent() + "\n");
            }
            else {
                messageStr+=("cat: " + chatDTO.getContent() + "\n");
            }
        }

        messages.add(new GptRequest.Message(GptRole.user, "[1] " + LocalDate.now().toString() + "\n[2]" + messageStr));

        GptRequest request = new GptRequest("gpt-4.1-mini-2025-04-14", 0.0, ChatGptConfig.MAx_TOKENS, messages, 1.0);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<GptRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<GptResponse> response;
        try {
            response = restTemplate.postForEntity(ChatGptConfig.BASE_URL, entity, GptResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ChatGPT API 호출 중 오류 발생", e);
        }

        String responseText = response.getBody().getChoices().get(0).getMessage().getContent();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(responseText);

        String content = node.path("content").asText();
        if(content.equals("no content")) return null;

        String endDateStr = node.path("endDate").asText();
        LocalDateTime endDate = LocalDate.parse(endDateStr).atStartOfDay();

        // 2. ChatEventEntity 생성 및 저장
        ChatEventEntity eventEntity = ChatEventEntity.builder()
                .content(content)
                .endDate(endDate)
                .createdTime(LocalDateTime.now())
                .user(user)
                .build();

        chatEventRepository.save(eventEntity);
        return null;
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
                    .chatDate(chat.getCreatedTime())
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

    private float[] parseEmbeddingVector(String jsonVector) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonVector, float[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 JSON 문자열을 float[]로 변환하는 데 실패했습니다.", e);
        }
    }

    public String getChatCompletion(List<ClaudeMessage> messages, UserEntity user, UserCatEntity cat, StatusDTO statusDTO) throws JsonProcessingException {
        String url = "https://api.anthropic.com/v1/messages";

        String prompt = this.makeClaudePrompt(user, cat, statusDTO);

        ClaudeChatRequest body = new ClaudeChatRequest(
                "claude-sonnet-4-20250514",
                300,
                prompt,
                messages
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<ClaudeChatRequest> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            String rawJson = restTemplate
                    .exchange(url, HttpMethod.POST, request, String.class)
                    .getBody();

            ObjectMapper mapper = new ObjectMapper();
            ClaudeChatResponse res = mapper.readValue(rawJson, ClaudeChatResponse.class);

            StringBuilder sb = new StringBuilder();
            for (ContentBlock block : res.getContent()) {
                if ("text".equals(block.getType())) {
                    sb.append(block.getText());
                }
            }
            String reply = sb.toString();

            return reply;
        } catch (HttpServerErrorException e) {
            int status = e.getStatusCode().value();

            if (status == 500 || status == 529 || status == 503) {
                return "down";
            } else {
                throw e;
            }
        }
    }

    private String makeClaudePrompt(UserEntity user, UserCatEntity cat, StatusDTO statusDTO) {
        String basePrompt = ChatPrompt.getResponse(user.getUsername());
        List<ExpenditureEntity> expenditureEntities = expenditureRepository.findAllByUser(user);
        String result = expenditureEntities.stream()
                .map(expenditureEntity -> {
                    return "[" + expenditureEntity.getDate() + "] " + expenditureEntity.getCategory().getCategory() + ": " + expenditureEntity.getMemo() + "(" + expenditureEntity.getAmount() + ")";
                }) // 혹은 e -> e.getCategoryName()
                .collect(Collectors.joining("\n"));

        RestTemplate restTemplate = new RestTemplate();
        String weatherResponse = restTemplate.getForObject("https://api.openweathermap.org/data/2.5/weather?q=Seoul&units=metric&lang=kr&appid=" + weatherApiKey, String.class);
        String weatherStr = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(weatherResponse);

            String description = root.path("weather").get(0).path("description").asText();
            double temp = root.path("main").path("temp").asDouble();

            weatherStr = "현재 날씨는 " + description + "이며, 기온은 " + temp + "도입니다.";
        } catch (Exception e) {
            return "날씨 정보를 불러오지 못했습니다.";
        }

        List<ChatEventEntity> chatEvents = chatEventRepository.findAllByUser(user);
        String eventResult = chatEvents.stream()
                .map(event -> {
                    return "[" + event.getEndDate() + "] " + event.getContent();
                })
                .collect(Collectors.joining("\n"));


        basePrompt += "\n#Informations\n";
        basePrompt += "## 저축냥's State\n";
        basePrompt += "### 배고픔\n" + ChatUtil.makeHungryStr(statusDTO.getHunger()) + "\n";
        basePrompt += "### 애정도\n" + ChatUtil.makeAffectionStr(statusDTO.getLove()) + "\n";
        basePrompt += "## 소비 상황\n" + result;
        basePrompt += "## 오늘 날짜" + "\n" + LocalDate.now().toString() + "\n";
        basePrompt += "## 오늘 날씨" + "\n" + weatherStr;
        basePrompt += "## 앞으로 소비가 예상되는 이벤트\n" + eventResult;

        return basePrompt;
    }

    public List<ClaudeMessage> buildClaudeMessages(ChatDTO[] userInputs) {
        List<ClaudeMessage> messages = new ArrayList<>();
        // 3) 각 엔티티를 role/user 구분해서 DTO에 매핑
        for (ChatDTO dto : userInputs) {
            String role = dto.getRole().equals("user") ? "user" : "assistant";
            messages.add(new ClaudeMessage(role, dto.getContent()));
        }

        return messages;
    }
}