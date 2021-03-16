package com.recruit.kakaopay.rest.api.entity;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(OriginHistoryKey.class)
@Table(name = "originHistory")
public class OriginHistory
{
    @Id
    private String token;
    @Id
    private Timestamp eventTime;
    @Column(length = 40)
    private String eventUser;
    @Column(length = 40)
    private String eventName;
    @Column
    private Timestamp createTime;
    @Column(length = 40)
    private String roomId;
    @Column(length = 40)
    private String sender;
    @Column
    private int originMoney;
    @Column
    private int balanceMoney;
    @Column(length = 4000)
    private String receiverExpected;
    @Column(length = 4000)
    private String receiver;
}

