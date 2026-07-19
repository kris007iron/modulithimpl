package com.kris007iron.modulithimpl.notifications;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("subscriber")
public record Subscriber(@Id Long id, String email) {
}
