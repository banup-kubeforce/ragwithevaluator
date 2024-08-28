package com.example.rag.chat;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.RelevancyEvaluator;
import org.springframework.ai.model.Content;
import org.springframework.ai.chat.model.ChatModel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/qa")
public class QAController {

    private final QAService qaService;
    private final ChatModel chatModel;

    @Autowired
    public QAController(QAService qaService, ChatModel chatModel) {
        this.qaService = qaService;
        this.chatModel = chatModel;
    }

    @GetMapping
    public Map completion(
            @RequestParam(value = "question", defaultValue = "What is the purpose of CVS?") String question,
            @RequestParam(value = "stuffit", defaultValue = "true") boolean stuffit) {
        String answer = this.qaService.generate(question, stuffit);
        Map map = new LinkedHashMap();
        map.put("question", question);
        map.put("answer", answer);
        return map;
    }

    @GetMapping("/test")
    public ChatResponse testResponse(
            @RequestParam(value = "question", defaultValue = "What is the purpose of CVS?") String question,
            @RequestParam(value = "stuffit", defaultValue = "true") boolean stuffit) {
        return qaService.generateTestResponse(question, stuffit);
    }

    @GetMapping("/evaluate")
    public Map<String, Object> evaluateResponse(
            @RequestParam(value = "question", defaultValue = "What is the purpose of Carina?") String question,
            @RequestParam(value = "stuffit", defaultValue = "true") boolean stuffit) {
        ChatResponse response = qaService.generateTestResponse(question, stuffit);

        var relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(chatModel));
        EvaluationRequest evaluationRequest = new EvaluationRequest(question,
                (List<Content>) response.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS), response.toString());
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);
        result.put("answer", response.getResult().getOutput().getContent());
        result.put("isRelevant", evaluationResponse.isPass());
        result.put("evaluationScore", evaluationResponse.getScore());
        result.put("evaluationFeedback", evaluationResponse.getFeedback());

        return result;
    }
}