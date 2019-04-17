package com.voxlearning.utopia.service.newhomework.api.mapper.response.outside;

import com.voxlearning.utopia.service.newhomework.api.mapper.response.base.BaseResp;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/16
 */
@Getter
@Setter
public class StudentBookDetailResp extends BaseResp {
    private static final long serialVersionUID = 5351605584412981380L;

    private String outsideReadingId;
    private String bookId;
    private String bookName;
    private String coverPic;         //封面
    private String author;           //作者
    private Double totalWords;        //总字数
    private Date endTime;            //截止时间
    private String description;      //简介

    private List<LeadinAudio> leadinAudios = new LinkedList<>(); //导读
    private List<Mission> missions = new LinkedList<>();         //关卡

    @Getter
    @Setter
    public static class Mission implements Serializable {
        private static final long serialVersionUID = 4015759012808175247L;

        private String missionId;                   // 关卡id
        private String missionName;                 // 关卡名称
        private int star;                           //星星
        private String missionDescription;          //关卡描述
        private String status;                      //关卡状态 "LOCK", "UNLOCK"
        private String doUrl;                       //闯关地址
        private Double missionWords;                //关卡字数
    }

    @Getter
    @Setter
    public static class LeadinAudio implements Serializable{
        private static final long serialVersionUID = 6907034095297055718L;

        private String audioTitle;    // 导读音频标题
        private String audioUrl;      //导读音频
        private Integer audioDuration; //音频时长
    }

    public enum MissionStatus {
        LOCK,
        UNLOCK
    }
}
