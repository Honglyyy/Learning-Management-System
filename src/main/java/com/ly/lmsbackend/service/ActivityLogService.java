package com.ly.lmsbackend.service;

import com.ly.lmsbackend.dto.activitydtos.ActivityLogResponseDTO;
import com.ly.lmsbackend.model.ActivityLog;
import com.ly.lmsbackend.model.Users;
import com.ly.lmsbackend.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public ActivityLogResponseDTO logActivity(Users user, String activityType, String description) {
        if (user == null) {
            return null;
        }

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .activityType(activityType != null ? activityType : "GENERAL")
                .description(description)
                .build();

        ActivityLog saved = activityLogRepository.save(log);
        return toDTO(saved != null ? saved : log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponseDTO> getMyActivities(String email) {
        return activityLogRepository.findByUser_EmailOrderByTimestampDesc(email)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ActivityLogResponseDTO toDTO(ActivityLog l) {
        if (l == null) {
            return null;
        }
        return ActivityLogResponseDTO.builder()
                .id(l.getId())
                .activityType(l.getActivityType())
                .description(l.getDescription())
                .timestamp(l.getTimestamp())
                .build();
    }
}
