package com.recruit.kakaopay.rest.api.service;

import com.recruit.kakaopay.rest.api.constant.Constant;
import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.OriginHistory;
import com.recruit.kakaopay.rest.api.repo.OriginHistoryJpaRepo;
import com.recruit.kakaopay.rest.api.repo.OriginJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OriginService
{
    private final OriginJpaRepo originJpaRepo;
    private final OriginHistoryJpaRepo originHistoryJpaRepo;

    public Origin getOrigin(String token)
    {
        Origin origin = originJpaRepo.findByToken(token);

        return origin;
    }

    public List<Origin> getOriginList()
    {
        return originJpaRepo.findAll();
    }

    public Origin generateOrigin(String token, String roomId, String sender, int totalMoney, List<String> receiverList)
    {
        //TODO: Should be modified as actual Time
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

        Origin origin = Origin.builder()
                .token(token)
                .createTime(currentTimestamp)
                .roomId(roomId)
                .sender(sender)
                .originMoney(totalMoney)
                .balanceMoney(totalMoney)
                .receiverExpected(receiverList.stream().collect(Collectors.joining(",")))
                .lastEventTime(currentTimestamp)
                .lastEventUser(sender)
                .lastEventName(Constant.SAVE)
                .build();

        return origin;
    }

    public Origin save(Origin origin)
    {
        Origin savedOrigin = originJpaRepo.save(origin);

        OriginHistory originHistory = OriginHistory.builder()
                .token(savedOrigin.getToken())
                .eventTime(savedOrigin.getLastEventTime())
                .eventUser(savedOrigin.getLastEventUser())
                .eventName(savedOrigin.getLastEventName())
                .createTime(savedOrigin.getCreateTime())
                .roomId(savedOrigin.getRoomId())
                .sender(savedOrigin.getSender())
                .originMoney(savedOrigin.getOriginMoney())
                .balanceMoney(savedOrigin.getBalanceMoney())
                .receiverExpected(savedOrigin.getReceiverExpected())
                .receiver(savedOrigin.getReceiver()).build();
        save(originHistory);

        return savedOrigin;
    }

    public OriginHistory save(OriginHistory originHistory)
    {
        return originHistoryJpaRepo.save(originHistory);
    }
}
