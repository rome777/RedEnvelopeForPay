package com.recruit.kakaopay.rest.api.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SplitHistoryKey.class)
@Table(name = "splitHistory")
public class SplitHistory implements Serializable
{
    @Id
    private String splitId;
    @Id
    private Timestamp eventTime;
    @Column(length = 40)
    private String eventUser;
    @Column(length = 40)
    private String eventName;
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
}
