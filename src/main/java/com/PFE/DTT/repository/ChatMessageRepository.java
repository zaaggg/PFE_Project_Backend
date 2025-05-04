package com.PFE.DTT.repository;

import com.PFE.DTT.model.ChatMessage;
import com.PFE.DTT.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Get all messages where the sender or the conversation contains the user
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE m.sender.id = :userId " +
            "OR m.conversation.user1.id = :userId " +
            "OR m.conversation.user2.id = :userId")
    List<ChatMessage> findBySenderOrParticipant(@Param("userId") Long userId);

    // Get userIds of people the current user has conversations with
    @Query("SELECT DISTINCT CASE " +
            "WHEN m.conversation.user1.id = :userId THEN m.conversation.user2.id " +
            "WHEN m.conversation.user2.id = :userId THEN m.conversation.user1.id " +
            "END FROM ChatMessage m " +
            "WHERE m.conversation.user1.id = :userId OR m.conversation.user2.id = :userId")
    List<Long> findDistinctConversationUserIds(@Param("userId") Long userId);
}
