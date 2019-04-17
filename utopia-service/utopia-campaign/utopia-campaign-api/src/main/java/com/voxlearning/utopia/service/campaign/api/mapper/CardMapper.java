package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CardMapper implements java.io.Serializable {

    public CardMapper(String type, Integer count) {
        this.type = type;
        this.count = count;
    }

    private String type;
    private Integer count;
    private List<CardDetail> details = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardDetail implements java.io.Serializable {
        private String type;
        private Integer offset;
        private Date createTime;
    }
}