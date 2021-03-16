package com.recruit.kakaopay.rest.api.controller;

import com.google.gson.JsonObject;
import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.exception.CustomException;
import com.recruit.kakaopay.rest.api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ApiController
{
    private final OriginService originSvc;
    private final SplitService splitSvc;
    private final SendingService sendSvc;
    private final ReceivingService rcvSvc;
    private final ViewService viewSvc;

    @PostMapping(value = "/send")
    public String send(@RequestHeader("X-USER-ID") String senderId,
                       @RequestHeader("X-ROOM-ID") String roomId,
                       @RequestParam(value="money") int money,
                       @RequestParam(value="receiverList") List<String> receiverList) throws CustomException
    {
        sendSvc.verifyRequest(roomId, senderId, money, receiverList);
        String token = sendSvc.getNewToken();
        Origin origin = originSvc.generateOrigin(token, roomId, senderId, money, receiverList);
        List<Split> splitList = splitSvc.generateSplitList(origin, receiverList.size());
        splitList = sendSvc.distributeMoney(splitList, money);
        originSvc.save(origin);
        splitSvc.save(splitList);
        return token;
    }

    @PutMapping(value = "/receive")
    public int receive(@RequestHeader("X-USER-ID") String receiverId,
                       @RequestHeader("X-ROOM-ID") String roomId,
                       @RequestParam(value="token") String token) throws CustomException
    {
        rcvSvc.verifyToken(token);    //check the token exists and is expired over 10 minutes
        rcvSvc.verifyReceiver(token, roomId, receiverId); //check the receiver are trying to get money twice, not same room
        Split assignedSplit = rcvSvc.assignReceiver(token, receiverId);
        int assignedMoney = assignedSplit.getSplitMoney();

        return assignedMoney;
    }

    @GetMapping(value = "/view")
    public String view(@RequestHeader("X-USER-ID") String viewer,
                     @RequestParam(value="token") String token) throws CustomException
    {
        viewSvc.verifyToken(token);     //check the token exists and is expired over 7 days
        if(!viewSvc.areYouSender(token, viewer))    //only sender can view
            throw new CustomException("you are not sender");
        JsonObject obj = viewSvc.makeResponse(token);

        return obj.toString();
    }
}