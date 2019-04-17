package com.voxlearning.utopia.agent.persist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * AgentMonthlyRecommendBook
 *
 * @author song.wang
 * @date 2016/8/16
 */
@Getter
@Setter
@NoArgsConstructor
public class AgentMonthlyRecommendBook implements Serializable {
    private String bookName;
    private String bookCoverUrl; //图书封面地址
}
