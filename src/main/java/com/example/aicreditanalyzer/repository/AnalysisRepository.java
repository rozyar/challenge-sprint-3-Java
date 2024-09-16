package com.example.aicreditanalyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.aicreditanalyzer.model.Analysis;
import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByUserId(Long userId);
}
