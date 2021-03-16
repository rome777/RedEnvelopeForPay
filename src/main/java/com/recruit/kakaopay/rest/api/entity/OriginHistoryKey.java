package com.recruit.kakaopay.rest.api.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class OriginHistoryKey implements Serializable
{
    private String token;
    private Timestamp eventTime;
}
