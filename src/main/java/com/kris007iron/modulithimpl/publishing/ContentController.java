package com.kris007iron.modulithimpl.publishing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {
    private final PublishingService publishingService;

    record PublishRequest(String title, String url, ContentType type){}

    @GetMapping()
    List<Content> all() {return publishingService.findAll();}

    @GetMapping("/{id}")
    ResponseEntity<Content> byId(@PathVariable Long id) {
        return publishingService.findBy(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping()
    ResponseEntity<Content> publish(@RequestBody PublishRequest  request) {
        var content = publishingService.publish(request.title(), request.url(), request.type());
        return ResponseEntity.created(URI.create("/api/content/" + content.id()))
                .body(content);
    }
}
