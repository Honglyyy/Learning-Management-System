package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.notificationdtos.NotificationResponseDTO;
import com.ly.lmsbackend.model.Notification;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.NotificationRepository;
import com.ly.lmsbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationResponseDTO sendNotification(Users user, String title, String message, String type, String actionUrl) {
        if (user == null) {
            return null;
        }

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type != null ? type : "SYSTEM")
                .isRead(false)
                .actionUrl(actionUrl)
                .build();

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved != null ? saved : notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getMyNotifications(String email) {
        return notificationRepository.findByUser_EmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        return notificationRepository.countByUser_EmailAndIsReadFalse(email);
    }

    @Transactional
    public NotificationResponseDTO markAsRead(Long notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to notification");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return toDTO(saved != null ? saved : notification);
    }

    @Transactional
    public int markAllAsRead(String email) {
        return notificationRepository.markAllAsReadByEmail(email);
    }

    private NotificationResponseDTO toDTO(Notification n) {
        if (n == null) {
            return null;
        }
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .actionUrl(n.getActionUrl())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
