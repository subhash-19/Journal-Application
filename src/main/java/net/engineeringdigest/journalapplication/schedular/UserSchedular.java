package net.engineeringdigest.journalapplication.schedular;

import net.engineeringdigest.journalapplication.entity.JournalEntry;
import net.engineeringdigest.journalapplication.entity.User;
import net.engineeringdigest.journalapplication.enums.Sentiment;
import net.engineeringdigest.journalapplication.model.SentimentData;
import net.engineeringdigest.journalapplication.repository.UserRepositoryImpl;
import net.engineeringdigest.journalapplication.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class UserSchedular {

    private final EmailService emailService;
    private final UserRepositoryImpl userRepository;
    private final KafkaTemplate<String, SentimentData> kafkaTemplate;

    @Autowired
    public UserSchedular(EmailService emailService, UserRepositoryImpl userRepository, KafkaTemplate<String, SentimentData> kafkaTemplate) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(cron = "0 0 9 ? * SUN")
    public void fetchUsersAndSaMail() {
        List<User> users = userRepository.getUserForSA();
        for (User user : users) {
            List<Sentiment> sentiments = getRecentSentiments(user);
            Sentiment mostFrequentSentiment = getMostFrequentSentiment(sentiments);

            if (mostFrequentSentiment != null) {
                sendSentiment(user, mostFrequentSentiment);
            }
        }
    }

    private List<Sentiment> getRecentSentiments(User user) {
        return user.getJournalEntries().stream()
                .filter(x -> x.getDate().isAfter(LocalDateTime.now().minusDays(7)))
                .map(JournalEntry::getSentiment)
                .toList();
    }

    private Sentiment getMostFrequentSentiment(List<Sentiment> sentiments) {
        Map<Sentiment, Integer> sentimentCounts = new EnumMap<>(Sentiment.class);

        for (Sentiment sentiment : sentiments) {
            if (sentiment != null) {
                sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
            }
        }

        return sentimentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private void sendSentiment(User user, Sentiment mostFrequentSentiment) {
        try {
            SentimentData sentimentData = SentimentData.builder()
                    .email(user.getEmail())
                    .sentiment("Sentiment for last 7 days: " + mostFrequentSentiment)
                    .build();
            kafkaTemplate.send("weekly-sentiments", sentimentData.getEmail(), sentimentData);
        } catch (Exception e) {
            emailService.sendEmail(user.getEmail(), "Sentiment for last 7 days", mostFrequentSentiment.toString());
        }
    }
}
