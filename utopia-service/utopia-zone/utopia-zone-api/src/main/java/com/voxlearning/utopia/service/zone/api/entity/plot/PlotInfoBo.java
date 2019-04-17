package com.voxlearning.utopia.service.zone.api.entity.plot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author : kai.sun
 * @version : 2018-11-14
 * @description :
 **/

@Getter
@Setter
@NoArgsConstructor
public class PlotInfoBo implements Serializable {

    private static final long serialVersionUID = 4278795537676853435L;

    private String plotInfoId;         //对应剧情id

    private Integer order;            //

    private Integer plotNum;         //活动id

    private String npcAudio;        //npc音频

    private Popup popup;            //弹框

    private Boolean needUnlock;       //需要解锁吗

    private String text;             //剧情字幕

    private String audio;            //音频绝对路径

}
