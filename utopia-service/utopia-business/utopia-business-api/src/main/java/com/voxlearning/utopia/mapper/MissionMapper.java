/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.mapper;

import com.voxlearning.utopia.api.constant.MissionState;
import com.voxlearning.utopia.api.constant.MissionType;
import com.voxlearning.utopia.api.constant.WishType;
import lombok.*;

import java.io.Serializable;

/**
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "newInstance")
public class MissionMapper implements Serializable {
    private static final long serialVersionUID = 6891247860897901281L;

    public static final String STUDENT_REMIND_PROGRESS = "STUDENT_REMIND_PROGRESS";//+1
    public static final String STUDENT_REMIND_REWARD = "STUDENT_REMIND_REWARD";//发奖励
    public static final String PARENT_UPDATE_PROGRESS = "PARENT_UPDATE_PROGRESS";
    public static final String PARENT_REWARD = "PARENT_REWARD";

    @NonNull private WishType wishType; // 心愿类型
    @NonNull private MissionType missionType;
    @NonNull private MissionState missionState;
    private String rewards; // 奖励
    private String mission; // 任务
    private Integer totalCount; // 任务次数
    private Integer finishCount; // 完成次数
    private String missionDate; // 任务创建时间
    private Long id; // 任务id
    private String img; // 任务照片
    private Boolean canClick; // true能点击，false不能点击
    /**
     * 提醒家长进度加一 -- "STUDENT_REMIND_PROGRESS"
     * 提醒家长颁发奖励 -- "STUDENT_REMIND_REWARD"
     * 家长设置任务 -- "PARENT_SET_MISSION"
     * 家长更新进度 -- "PARENT_UPDATE_PROGRESS"
     * 家长发放奖励 -- "PARENT_REWARD"
     */
    private String op; // 操作
}
