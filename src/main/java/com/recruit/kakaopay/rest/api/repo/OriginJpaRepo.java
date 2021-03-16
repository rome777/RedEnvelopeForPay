package com.recruit.kakaopay.rest.api.repo;

import com.recruit.kakaopay.rest.api.entity.Origin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OriginJpaRepo extends JpaRepository<Origin, Long>
{
    Origin findByToken(String token);

    List<Origin> findByRoomIdAndSenderAndOriginMoneyAndReceiverExpected(String roomId, String sender, int totalMoney, String receiverList);
}
