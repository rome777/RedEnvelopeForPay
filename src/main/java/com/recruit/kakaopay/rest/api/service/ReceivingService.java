package com.recruit.kakaopay.rest.api.service;

import com.recruit.kakaopay.rest.api.constant.Constant;
import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.exception.CustomException;
import com.recruit.kakaopay.rest.api.repo.OriginJpaRepo;
import com.recruit.kakaopay.rest.api.repo.SplitJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReceivingService
{
    private final OriginJpaRepo originJpaRepo;
    private final SplitJpaRepo splitJpaRepo;
    private final OriginService originSvc;
    private final SplitService splitSvc;
    private final ViewService viewSvc;

    //check the token exists and is expired over 10 minutes
    public void verifyToken(String token) throws CustomException
    {
        Origin origin = originJpaRepo.findByToken(token);
        if(origin == null) throw new CustomException("There is no token [" + token + "]");

        List<Split> splitList = splitJpaRepo.findByToken(token);
        //Origin exists, but split not exists. Is this verification necessary?
        if(splitList.size() == 0) throw new CustomException("There is no token to be received.");

        Timestamp originTimestamp = origin.getCreateTime();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        double differMinute = (currentTimestamp.getTime() - originTimestamp.getTime())/1000.0/60.0;
        if(differMinute >= 10) throw new CustomException("token was expired 10 minutes.");
    }

    //check the receiver are trying to get money twice, not same room
    public void verifyReceiver(String token, String roomId, String receiverId) throws CustomException
    {
        if(viewSvc.areYouSender(token, receiverId)) throw new CustomException("you are sender, cannot receive");
        
        Origin origin = originJpaRepo.findByToken(token);
        if(origin != null)
        {
            String receiverExpected = origin.getReceiverExpected();
            List<String> receiverExpectedList = Arrays.asList(receiverExpected.split(","));
            boolean isExpectedReceiver = receiverExpectedList.stream().anyMatch(r -> r.equals(receiverId));
            if(!isExpectedReceiver) throw new CustomException("you are not expected Receiver");
        }

        List<Split> splitList = splitJpaRepo.findByTokenAndReceiver(token, receiverId);
        if(splitList.size() > 0) throw new CustomException("you already got split money");

        boolean sameRoom = splitJpaRepo.findByToken(token).stream().anyMatch(s -> s.getRoomId().equals(roomId));
        if(!sameRoom) throw new CustomException("not same room the money was sent");
    }

    public Split assignReceiver(String token, String receiverId)
    {
        Origin origin = originJpaRepo.findByToken(token);
        Split split = splitJpaRepo.findByToken(token).stream()
                .filter(s -> s.getReceiver() == null && s.getReceiveTime() == null)
                .findFirst().get();

        origin.setReceiver(origin.getReceiver() == null ? receiverId : origin.getReceiver() + "," + receiverId);
        origin.setBalanceMoney(origin.getBalanceMoney() - split.getSplitMoney());
        origin.setLastEventUser(receiverId);
        origin.setLastEventName(Constant.RECEIVE);

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        split.setReceiveTime(currentTimestamp); //TODO: Should be modified as actual Time
        split.setReceiver(receiverId);
        split.setLastEventName(Constant.RECEIVE);
        split.setLastEventUser(receiverId);

        originSvc.save(origin);
        splitSvc.save(split);

        return split;
    }
}
