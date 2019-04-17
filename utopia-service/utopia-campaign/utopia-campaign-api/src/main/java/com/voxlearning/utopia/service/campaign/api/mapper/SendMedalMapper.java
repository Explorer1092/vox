package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Data;

import java.util.List;

@Data
public class SendMedalMapper implements java.io.Serializable {

    private Long groupId;
    private List<Long> studentIds;
    private List<Integer> medalIds;

}
