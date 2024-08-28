package com.example.rag.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QAService {

	private final ChatModel chatModel;
	private final VectorStore vectorStore;

	@Autowired
	public QAService(ChatModel chatModel, VectorStore vectorStore) {
		this.chatModel = chatModel;
		this.vectorStore = vectorStore;
	}

	public String generate(String message, boolean stuffit) {
		ChatResponse response = generateTestResponse(message, stuffit);
		return response.getResult().getOutput().getContent();
	}

	public ChatResponse generateTestResponse(String message, boolean stuffit) {
		ChatClient chatClient = ChatClient.builder(chatModel).build();

		if (stuffit) {
			return chatClient.prompt()
					.advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
					.user(message)
					.call()
					.chatResponse();
		} else {
			return chatClient.prompt()
					.user(message)
					.call()
					.chatResponse();
		}
	}
}
