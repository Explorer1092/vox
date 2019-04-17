/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
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

import lombok.Data;

import java.io.Serializable;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-4-16
 */
@Data
public class ClazzBestMapper implements Serializable {
    private static final long serialVersionUID = 7049968111268189925L;

    private Long clazzId;
    // 土豪
    private Long wealthiestId;
    private String wealthiestDate;
    private String wealthiestName;
    private String wealthiestImg;
    private Integer silverCount;
    //　最赞
    private Long mostFavoriteId;
    private String mostFavoriteDate;
    private String mostFavoriteName;
    private String mostFavoriteImg;
    private Integer likeCount;
    // 学霸
    private Long smRank1Id;
    private String smRank1Name;
    private String smRank1Img;
    private Long smRank2Id;
    private String smRank2Name;
    private String smRank2Img;
    private Long smRank3Id;
    private String smRank3Name;
    private String smRank3Img;
    private String studyMasterDate;
    // 今日土豪前三
    private Long todayWealthRank1Id;
    private String todayWealthRank1Name;
    private Integer todayWealthRank1SilverCount;
    private String todayWealthRank1Img;
    private Long todayWealthRank2Id;
    private String todayWealthRank2Name;
    private Integer todayWealthRank2SilverCount;
    private String todayWealthRank2Img;
    private Long todayWealthRank3Id;
    private String todayWealthRank3Name;
    private Integer todayWealthRank3SilverCount;
    private String todayWealthRank3Img;
    // 今日最赞前三
    private Long todayFavoriteRank1Id;
    private String todayFavoriteRank1Name;
    private Integer todayFavoriteRank1LikeCount;
    private String todayFavoriteRank1Img;
    private Long todayFavoriteRank2Id;
    private String todayFavoriteRank2Name;
    private Integer todayFavoriteRank2LikeCount;
    private String todayFavoriteRank2Img;
    private Long todayFavoriteRank3Id;
    private String todayFavoriteRank3Name;
    private Integer todayFavoriteRank3LikeCount;
    private String todayFavoriteRank3Img;
    // 阿分题土豪
    private String AfentiExamWealthiestName;
    private Integer AfentiExamSilverCount;
}
