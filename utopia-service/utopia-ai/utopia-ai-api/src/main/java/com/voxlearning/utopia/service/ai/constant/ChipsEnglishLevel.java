package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChipsEnglishLevel {
    One("一级"), Two("二级"), Three("三级");
    @Getter
    private final String description;
}
