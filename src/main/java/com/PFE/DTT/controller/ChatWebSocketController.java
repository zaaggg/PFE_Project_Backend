package com.PFE.DTT.controller;

import com.PFE.DTT.dto.ChatMessageDTO;
import com.PFE.DTT.dto.UserDTO;
import com.PFE.DTT.model.ChatMessage;
import com.PFE.DTT.model.Conversation;
import com.PFE.DTT.model.MessageStatus;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.ChatMessageRepository;
import com.PFE.DTT.repository.ConversationRepository;
import com.PFE.DTT.repository.UserRepository;
import com.PFE.DTT.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ChatWebSocketController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @MessageMapping("/conversations/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}")
    public ChatMessageDTO sendPrivateMessage(
            @DestinationVariable String conversationId,
            ChatMessageDTO messageDTO
    ) {
        messageDTO.setTimestamp(LocalDateTime.now().toString());

        // Parse IDs from conversationId
        String[] parts = conversationId.split("_");
        Long user1Id = Long.parseLong(parts[0]);
        Long user2Id = Long.parseLong(parts[1]);

        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Find or create conversation
        Conversation conversation = conversationRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUser1(userRepository.findById(user1Id).orElseThrow());
                    c.setUser2(userRepository.findById(user2Id).orElseThrow());
                    c.setLastMessageAt(LocalDateTime.now());
                    return conversationRepository.save(c);
                });

        // Save message
        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(messageDTO.getContent());
        message.setImageUrl(messageDTO.getImageUrl());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus(MessageStatus.SENT);
        chatMessageRepository.save(message);

        // Build DTO response
        ChatMessageDTO response = new ChatMessageDTO();
        response.setSenderId(sender.getId());
        response.setReceiverId(receiver.getId());
        response.setSender(UserDTO.fromEntity(sender));
        response.setContent(message.getContent());
        response.setImageUrl(message.getImageUrl());
        response.setTimestamp(message.getTimestamp().toString());
        response.setStatus(message.getStatus());
        response.setSeen(false);

        return response;
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

    @GetMapping("/api/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/api/chat/contacts/{userId}")
    public ResponseEntity<List<UserDTO>> getChatContacts(@PathVariable Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findBySenderOrParticipant(userId);
        Set<Long> contactIds = messages.stream()
                .map(msg -> {
                    Long u1 = msg.getConversation().getUser1().getId();
                    Long u2 = msg.getConversation().getUser2().getId();
                    return Objects.equals(u1, userId) ? u2 : u1;
                })
                .collect(Collectors.toSet());

        List<UserDTO> contacts = userRepository.findAllById(contactIds).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/api/conversations/users")
    public ResponseEntity<List<UserDTO>> getConversationUsers(@RequestParam Long userId) {
        List<Long> conversationUserIds = chatMessageRepository.findDistinctConversationUserIds(userId);
        List<UserDTO> users = userRepository.findAllById(conversationUserIds).stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
