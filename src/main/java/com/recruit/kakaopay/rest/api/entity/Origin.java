package com.recruit.kakaopay.rest.api.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "origin")
public class Origin
{
    @Id
    private String token;
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
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
    @Column
    @UpdateTimestamp
    private Timestamp lastEventTime;
    @Column(length = 40)
    private String lastEventUser;
    @Column(length = 40)
    private String lastEventName;
}
