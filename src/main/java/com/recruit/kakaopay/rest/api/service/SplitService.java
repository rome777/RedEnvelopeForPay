package com.recruit.kakaopay.rest.api.service;

import com.recruit.kakaopay.rest.api.constant.Constant;
import com.recruit.kakaopay.rest.api.entity.Origin;
import com.recruit.kakaopay.rest.api.entity.Split;
import com.recruit.kakaopay.rest.api.entity.SplitHistory;
import com.recruit.kakaopay.rest.api.repo.SplitHistoryJpaRepo;
import com.recruit.kakaopay.rest.api.repo.SplitJpaRepo;
import freemarker.template.utility.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SplitService
{
    private final SplitJpaRepo splitJpaRepo;
    private final SplitHistoryJpaRepo splitHistoryJpaRepo;

    public List<Split> getSplitByToken(String token)
    {
        List<Split> splitList = splitJpaRepo.findByToken(token);

        return splitList;
    }

    public SplitHistory getHistory(Split split)
    {
        return SplitHistory.builder()
                .splitId(split.getSplitId())
                .eventTime(split.getLastEventTime())
                .eventUser(split.getLastEventUser())
                .eventName(split.getLastEventName())
                .token(split.getToken())
                .roomId(split.getRoomId())
                .receiveTime(split.getReceiveTime())
                .receiver(split.getReceiver())
                .splitMoney(split.getSplitMoney())
                .build();
    }

    public String generateSplitId(String token, int sequence)
    {
        //supposed to maximum receiver count is under 10000.
        return token + StringUtil.leftPad(Integer.toString(sequence), 4, '0');
    }

    public List<Split> generateSplitList(Origin origin, int receiverListSize)
    {
        List<Split> splitList = new ArrayList<Split>();
        for (int i = 1; i <= receiverListSize; i++)
        {
            splitList.add(Split.builder()
                    .splitId(generateSplitId(origin.getToken(), i))
                    .token(origin.getToken())
                    .roomId(origin.getRoomId())
                    .lastEventTime(origin.getLastEventTime())
                    .lastEventUser(origin.getLastEventUser())
                    .lastEventName(Constant.SAVE)
                    .build());
        }

        return splitList;
    }

    public Split save(Split split)
    {
        splitJpaRepo.save(split);
        SplitHistory splitHistory = getHistory(split);
        splitHistoryJpaRepo.save(splitHistory);

        return split;
    }

    public List<Split> save(List<Split> splitList)
    {
        List<Split> savedSplitList = splitJpaRepo.saveAll(splitList);

        List<SplitHistory> splitHistoryList = new ArrayList<SplitHistory>();
        for (Split split : savedSplitList)
            splitHistoryList.add(getHistory(split));
        splitHistoryJpaRepo.saveAll(splitHistoryList);

        return savedSplitList;
    }
}
