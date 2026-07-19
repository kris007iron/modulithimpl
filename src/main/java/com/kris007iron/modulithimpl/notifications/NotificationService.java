package com.kris007iron.modulithimpl.notifications;

import com.kris007iron.modulithimpl.publishing.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SubscriberRepository subscriberRepository;

    public void notifySubscribers(Content content) {
        subscriberRepository.findAll().forEach(subscriber -> System.out.printf("Notifying %s about: %s%n", subscriber.email(), content.title()));
    }
}
