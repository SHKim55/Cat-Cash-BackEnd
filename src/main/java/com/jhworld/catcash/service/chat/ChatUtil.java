package com.jhworld.catcash.service.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * 채팅 응답 문자열을 문장 단위로 분리하고,
 * 각 문장이 MAX_LENGTH(글자 수)보다 길면 적절히 잘라서 반환하는 유틸 클래스
 */
public class ChatUtil {
    // 한 문장의 최대 길이
    private static final int MAX_LENGTH = 30;

    /**
     * 주어진 텍스트를 문장 종료 기호(. ? !) 기준으로 먼저 나눈 뒤,
     * 각 문장이 MAX_LENGTH보다 크면 띄어쓰기 단위로 적절히 잘라서 리스트로 반환합니다.
     *
     * @param text 분리할 원본 텍스트
     * @return 분리된 문장 리스트
     */
    public static List<String> splitBySentenceAndLength(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        final int MAX_LENGTH = 20;



        return result;
    }

    public static String makeHungryStr(Long hunger) {
        if (hunger < 10) {
            return "Starving to death";
        } else if (hunger < 30) {
            return "Very hungry";
        } else if (hunger < 50) {
            return "A bit hungry";
        } else if (hunger < 70) {
            return "Slightly full";
        } else if (hunger < 90) {
            return "Very full";
        } else {
            return "Stuffed to the brim";
        }
    }

    public static String makeAffectionStr(Long affection) {
        if (affection < 10) {
            return "I'm so lonely... Please play with me, nya...";
        } else if (affection < 30) {
            return "It feels like I'm all alone... Stay with me, meow...";
        } else if (affection < 50) {
            return "I think we're starting to connect, yaong~";
        } else if (affection < 70) {
            return "I feel comfy by your side, nyang~";
        } else if (affection < 90) {
            return "I really like you! Being with you makes me purr~";
        } else {
            return "You're my whole world! I love you so so much!! Nyang nyang";
        }
    }

    /**
     * 띄어쓰기 기준으로 최대 길이로 잘라서 반환
     */
//    private static List<String> chunkByLength(String sentence, int limit) {
//        List<String> chunks = new ArrayList<>();
//        int start = 0;
//        int len   = sentence.length();
//
//        while (start < len) {
//            int remaining = len - start;
//
//            // 남은 문자열이 limit 이하라면 그대로 한 덩어리
//            if (remaining <= limit) {
//                chunks.add(sentence.substring(start).trim());
//                break;
//            }
//
//            int cutoff = start + limit;
//            // cutoff 이후 처음 나오는 공백 위치
//            int nextSpace = sentence.indexOf(' ', cutoff);
//
//            int end;
//            if (nextSpace >= 0) {
//                // 공백까지 한 덩어리 (공백 다음부터 새 청크 시작)
//                end = nextSpace + 1;
//            } else {
//                // 공백이 없으면 남은 전체
//                end = len;
//            }
//
//            chunks.add(sentence.substring(start, end).trim());
//            start = end;
//        }
//
//        return chunks;
//    }
}