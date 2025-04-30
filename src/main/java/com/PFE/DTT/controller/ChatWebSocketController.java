package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ChatMessageDTO;
import com.PFE.DTT.service.AuthService;
import com.PFE.DTT.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class ChatWebSocketController {

    @Autowired
    private CloudinaryService cloudinaryService;


    @MessageMapping("/chat") // listens to /app/chat
    @SendTo("/topic/messages") // broadcasts to /topic/messages
    public ChatMessageDTO send(ChatMessageDTO message) {
        message.setTimestamp(java.time.LocalDateTime.now().toString());
        return message;
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, String>> uploadChatPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadChatPhoto(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}

