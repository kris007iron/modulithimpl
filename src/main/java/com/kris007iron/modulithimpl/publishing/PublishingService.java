package com.kris007iron.modulithimpl.publishing;


import com.kris007iron.modulithimpl.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublishingService {
    private final ContentRepository contentRepository;
    private final NotificationService notificationService;

    public List<Content> findAll() {
        return contentRepository.findAll();
    }

    public Optional<Content> findBy(Long id) {
        return contentRepository.findById(id);
    }

    @Transactional
    public Content publish(String title, String url, ContentType type) {
        var content = contentRepository.save(Content.draft(title, url, type));
        notificationService.notifySubscribers(content);

        return content;
    }
}
