package com.kris007iron.modulithimpl.publishing;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("content")
public record Content(@Id
                      Long id,
                      String title,
                      String url,
                      ContentType type,
                      Instant publishedAt) {
    public static Content draft(String title, String url, ContentType type){
        return new Content(null, title, url, type, Instant.now());
    }
}
