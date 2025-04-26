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
        for (User user: users) {
            List<JournalEntry> journalEntries = user.getJournalEntries();

            List<Sentiment> sentiments = journalEntries.stream().filter(x -> x.getDate().isAfter(LocalDateTime.now().minusDays(7))).map(JournalEntry::getSentiment).toList();

            Map<Sentiment, Integer> sentimentCounts = new EnumMap<>(Sentiment.class);
            for (Sentiment sentiment: sentiments) {
                if(sentiment != null) {
                    sentimentCounts.put(sentiment, sentimentCounts.getOrDefault(sentiment, 0) + 1);
                }
            }
            Sentiment mostFrequentSentiment = null;
            int maxCount = 0;
            for (Map.Entry<Sentiment, Integer> entry : sentimentCounts.entrySet()) {
                if(entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostFrequentSentiment = entry.getKey();
                }
            }
            if(mostFrequentSentiment != null) {
                SentimentData sentimentData = SentimentData.builder().email(user.getEmail()).sentiment("Sentiment for last 7 days" + mostFrequentSentiment).build();
                kafkaTemplate.send("weekly-sentiments", sentimentData.getEmail(), sentimentData);
//                emailService.sendEmail(user.getEmail(), "Sentiment for last 7 days", mostFrequentSentiment.toString());
            }
        }
    }
}
