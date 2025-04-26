package net.engineeringdigest.journalapplication.service;

import net.engineeringdigest.journalapplication.model.SentimentData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SemtimentConsumerService {

    private final EmailService emailService;

    @Autowired
    public SemtimentConsumerService(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "weekly-sentiments", groupId = "weekly-sentiment-group")
    public void consume(SentimentData sentimentData) {
        sendEmail(sentimentData);
    }

    private void sendEmail(SentimentData sentimentData) {
        emailService.sendEmail(sentimentData.getEmail(), "Sentiment for previous week", sentimentData.getSentiment());
    }
}
