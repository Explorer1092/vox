package com.voxlearning.utopia.service.parent.homework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class UserPreference implements Serializable {

    private Long userId; // 学生id

    private String subject; // 学科

    private String bookId; // 教材

    private List<String> levels;

    private String bizType;
}
