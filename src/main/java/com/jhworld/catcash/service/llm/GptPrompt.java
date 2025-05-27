package com.jhworld.catcash.service.llm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class GptPrompt {
    private String personaText = "너는 지금부터 사용자의 저축 습관 형성을 도와주는 도우미야." +
            "아래 정보에 따라 사용자의 저축을 유도할 수 있는 멘트를 생성해서 보내줘";

    private Boolean isTodaysFirstMessage = true;
    private String firstMessageText = "먼저, 사용자에게 인사말을 건네고 오늘 하루 일정을 물어봐줘.";
    private String nextMessageText = "사용자가 보낸 질문을 바탕으로 자연스럽게 대화를 이어나가줘.";

    private String getSystemPrompt(Boolean isFirst) {
        if(isFirst)
            return personaText + "\n" + firstMessageText;
        return personaText + "\n" + nextMessageText;
    }

    public String getPrompt(Boolean isFirst, String userInput) {
        return getSystemPrompt(isFirst) + "\n" + userInput;
    }
}
