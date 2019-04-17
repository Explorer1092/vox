package com.voxlearning.utopia.service.reward.api.enums.support;

import lombok.Data;

import java.util.List;

@Data
public class RewardTagNode {
    private Long id;
    private String name;
    private List<RewardTagNode> children;
}