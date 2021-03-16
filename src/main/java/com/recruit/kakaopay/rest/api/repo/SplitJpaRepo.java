package com.recruit.kakaopay.rest.api.repo;

import com.recruit.kakaopay.rest.api.entity.Split;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SplitJpaRepo extends JpaRepository<Split, Long>
{
    List<Split> findByToken(String token);

    List<Split> findByReceiver(String receiverId);

    List<Split> findByTokenAndReceiver(String token, String receiver);
}
