package com.ly.lmsbackend.repository;

import com.ly.lmsbackend.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUser_IdOrderByTimestampDesc(Long userId);

    List<ActivityLog> findByUser_EmailOrderByTimestampDesc(String email);
}
