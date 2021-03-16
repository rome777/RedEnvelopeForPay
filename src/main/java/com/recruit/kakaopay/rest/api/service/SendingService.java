package com.recruit.kakaopay.rest.api.service;

import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.exception.CustomException;
import com.recruit.kakaopay.rest.api.repo.OriginJpaRepo;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class SendingService
{
    private final OriginService originSvc;
    private final OriginJpaRepo originJpaRepo;

//    public OriginService getInstance()
//    {
//        return getInstance();
//    }

    public String getNewToken()
    {
        List<Origin> originList = originSvc.getOriginList();
        RandomString rs = new RandomString(3);
        boolean existTokenAlready = true;
        while (existTokenAlready)
        {
            String newToken = rs.nextString();
            existTokenAlready = originList.stream().anyMatch(o -> o.getToken().equals(newToken));
            if (!existTokenAlready) return newToken;
        }
        return null;
    }

    public void verifyRequest(String roomId, String sender, int totalMoney, List<String> receiverList) throws CustomException
    {
        List<Origin> originList = originJpaRepo.findByRoomIdAndSenderAndOriginMoneyAndReceiverExpected(roomId, sender, totalMoney, String.join(",", receiverList));
        for(Origin origin : originList)
        {
            Timestamp originTimestamp = origin.getCreateTime();
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            double differSecond = (currentTimestamp.getTime() - originTimestamp.getTime())/1000.0;
            if(differSecond < 10) throw new CustomException("same request was sent in 10 seconds");
        }
    }

    public List<Split> distributeMoney(List<Split> splitList, int totalMoney)
    {
        Collections.shuffle(splitList);
        int remainingTotal = totalMoney;
        Random rand = new Random();
        for (int i = 0; i < splitList.size() - 1; i++)
        {
            Split split = splitList.get(i);
            split.setSplitMoney(rand.nextInt(remainingTotal));
            remainingTotal -= split.getSplitMoney();
        }
        splitList.get(splitList.size() - 1).setSplitMoney(remainingTotal);

        Collections.sort(splitList);
        return splitList;
    }
}
