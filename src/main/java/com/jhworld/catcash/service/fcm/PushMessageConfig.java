package com.jhworld.catcash.service.fcm;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
public class PushMessageConfig {
    private static int topicIndex = 0;
    private static final List<String> topicList = Arrays.asList(
            "메인 소비 카테고리와 관련된 물품 구매 억제",
            "현재 시간에 사람들이 일반적으로 많이 구매하는 카테고리의 물품 구매 억제",
            "절약을 통해 고양이에게 밥을 줄 수 있는 코인을 얻어 사용자가 밥을 줄 수 있게 유도",
            "어플리케이션(저축냥) 접속 유도");

    private static final String SYSTEM_PROMPT = "<System Prompt>\n" +
            "너는 지금부터 절약 유도 어플리케이션 내부에서 사용자에게 보낼 푸시 알림 메시지를 만들어야 해\n" +
            "푸시 알림 메시지를 통해 사용자가 소비를 줄여 저축을 유도하는 것이 목적이야.\n" +
            "어플리케이션 내부에는 사용자의 절약 유도를 도와주는 고양이 아바타가 존재하기 때문에, 너는 고양이가 사용자한테 말을 거는 형식으로 메시지를 만들어줘야해.";

    private static final String USER_PROMPT_TEMPLATE = "<지시사항>\n" +
            "1. 사용자의 현재 상황에 맞는 절약 유도 푸시 메시지의 제목과 본문을 작성해줘.\n" +
            "2. 메시지는 '제목 텍스트'와 '본문 텍스트'로 구성이 되어야 하고, 제목은 사용자의 관심을 집중시킬 수 있는 멘트로 작성해줘.\n" +
            "3. 본문 텍스트는 반드시 10자 이내로, 본문 텍스트는 반드시 15~20자 이내로 작성해줘." +
            "4. 제목 텍스트는 고양이가 말하는 것처럼 말 끝에 '냥'을 붙여줘야해. (Ex. ~했으면 좋겠다냥, ~해달라냥)" +
            "5. 본문 텍스트는 일반적인 경어체로 작성하고, 저축냥 서비스 사용을 유도하는 멘트를 넣어줘." +
            "6. 메시지가 생성되면 반드시 아래와 같은 포맷으로 메시지를 정확하게 보내줘야해. {} 안에 네가 생성한 메시지를 넣고, {} 표시는 제거해줘.\n\n" +
            "<메시지 포맷>\n" +
            "title: {제목 텍스트}\ncontent: {본문 텍스트}\n\n" +
            "7. 아래쪽에 적힌 사용자의 기본 정보를 바탕으로 사용자의 현재 상황을 유추해서 메시지를 작성해줘.\n\n";


    public static String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public static String createMessagePrompt(PersonalMessageData personalData) {
        String newPrompt = USER_PROMPT_TEMPLATE + "<사용자 정보>\n" +
                String.format("1. 메시지 주제: %s\n", topicList.get(topicIndex)) +
                String.format("2. 현재 시각(아침, 점심, 혹은 저녁인 지 판단): %s\n", LocalDateTime.now()) +
                String.format("3. 사용자의 주된 소비 카테고리: %s\n", personalData.getMainCategory()) +
                String.format("4. 사용자의 이름: %s\n", personalData.getUsername());

        topicIndex = (topicIndex + 1) % 4;
        return newPrompt;
    }

    public static Map<String, String> parseResponseText(String response) {
        System.out.println("gpt response: " + response);

        Map<String, String> messageMap = new HashMap<>();
        List<String> responseData = Arrays.asList(response.split("\n"));
        messageMap.put("title", responseData.get(0).substring(7));
        messageMap.put("content", responseData.get(1).substring(9));
        return messageMap;
    }
}
