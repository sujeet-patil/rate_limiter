package com.sujeet.projects.rate_limiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {
    @GetMapping("/hello")
    public ResponseEntity<String> hello(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok("Hello, " + userId + "!");
    }
}
