package com.recruit.kakaopay.rest.api.repo;

import com.recruit.kakaopay.rest.api.entity.OriginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OriginHistoryJpaRepo extends JpaRepository<OriginHistory, Long>
{}
