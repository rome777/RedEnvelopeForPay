package com.recruit.kakaopay.rest.api.entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class SplitHistoryKey implements Serializable
{
    private String splitId;
    private Timestamp eventTime;
}
