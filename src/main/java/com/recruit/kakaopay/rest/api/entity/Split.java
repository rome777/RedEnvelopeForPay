package com.recruit.kakaopay.rest.api.entity;

import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "split")
public class Split implements Comparable<Split>
{
    @Id
    private String splitId;
    @Column
    private String token;
    @Column
    private String roomId;
    @Column
    private Timestamp receiveTime;
    @Column(length = 40)
    private String receiver;
    @Column
    private int splitMoney;
    @Column
    @UpdateTimestamp
    private Timestamp lastEventTime;
    @Column(length = 40)
    private String lastEventUser;
    @Column(length = 40)
    private String lastEventName;

    @Override
    public int compareTo(Split s)
    {
        return splitId.compareTo(s.getSplitId());
    }
}
