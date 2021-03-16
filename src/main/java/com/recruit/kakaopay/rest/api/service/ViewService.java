package com.recruit.kakaopay.rest.api.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.exception.CustomException;
import com.recruit.kakaopay.rest.api.repo.OriginJpaRepo;
import com.recruit.kakaopay.rest.api.repo.SplitJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ViewService
{
    private final OriginJpaRepo originJpaRepo;
    private final SplitJpaRepo splitJpaRepo;
    private final OriginService originSvc;
    private final SplitService splitSvc;

    //check the token exists and is expired over 7 days
    public void verifyToken(String token) throws CustomException
    {
        Origin origin = originJpaRepo.findByToken(token);
        if(origin == null) throw new CustomException("There is no token [" + token + "]");

        List<Split> splitList = splitJpaRepo.findByToken(token);
        //Origin exists, but split not exists. Is this verification necessary?
        if(splitList.size() == 0) throw new CustomException("There is no token to be received.");

        Timestamp originTimestamp = origin.getCreateTime();
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        double differDay = (currentTimestamp.getTime() - originTimestamp.getTime())/1000.0/60.0/60.0/24.0;
        if(differDay > 7) throw new CustomException("token was expired 7 days.");
    }

    public boolean areYouSender(String token, String viewer) throws CustomException
    {
        Origin origin = originJpaRepo.findByToken(token);
        String sender = origin.getSender();

        return viewer.equals(sender);
    }

    public JsonObject makeResponse(String token)
    {
        Origin origin = originSvc.getOrigin(token);
        List<Split> splitList = splitSvc.getSplitByToken(token);
        List<Split> receivedSplitList = splitList.stream().filter(s -> s.getReceiver() != null).collect(Collectors.toList());

        JsonArray arr = new JsonArray();
        for(Split receivedSplit : receivedSplitList)
        {
            JsonObject splitobj = new JsonObject();
            splitobj.addProperty("receivedMoney", receivedSplit.getSplitMoney());
            splitobj.addProperty("receiver", receivedSplit.getReceiver());
            arr.add(splitobj);
        }

        JsonObject obj = new JsonObject();
        obj.addProperty("createTime", origin.getCreateTime().toString());
        obj.addProperty("originMoney", origin.getOriginMoney());
        obj.addProperty("receivedMoney", origin.getOriginMoney() - origin.getBalanceMoney());
        obj.add("receivedList", arr);

        return obj;
    }
}
