package com.voxlearning.utopia.service.action.api.document;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin
 * @since 12/8/2016
 */
@Getter
@Setter
public class ClazzAchievementWall implements Serializable {

    private static final long serialVersionUID = -262913543401989324L;

    private Long clazzId;
    private List<ClazzAchievementLog> achievementLogs;
}
