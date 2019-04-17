package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-05-12 上午11:15
 **/
@Setter
@Getter
public class StudyAppData implements Serializable {
    private static final long serialVersionUID = 5331081305474787947L;
    public static final String PROGRESS = "progress";
    public static final String BOOKING = "booking"; //字段只能沿用booking,实际应该是show,表示是否显示入口
    public static final String ALBUMCOUNT = "albumCount";
    public static final String ICONURL = "iconUrl";
    public static final String NOTIFYCONTENT = "notifyContent";
    public static final String NOTIFYUNIQUEID= "notifyUniqueId";

    private Long userId;

    private SelfStudyType selfStudyType;


    /**
     * 进度
     */
    private String progress;

    /**
     * 优先于进度显示的文案
     */
    private String notifyContent;

    private String notifyUniqueId;

    /**
     * 订阅专辑数量
     */
//    private Long ablumCount;

    /**
     * 是否显示,目前直播客,小课堂再用
     */
    private Boolean show;

    private String iconUrl;


    public boolean fetchShow(){
        return SafeConverter.toBoolean(show);
    }


    public static StudyAppData fromMap(Map<String, Object> map, Long userId, SelfStudyType selfStudyType) {
        if (map == null)
            return null;
        StudyAppData selfStudyData = new StudyAppData();
        selfStudyData.setUserId(userId);
        selfStudyData.setSelfStudyType(selfStudyType);
//        selfStudyData.setAblumCount(SafeConverter.toLong(map.get(ALBUMCOUNT)));
        selfStudyData.setShow(SafeConverter.toBoolean(map.get(BOOKING)));
        selfStudyData.setProgress(SafeConverter.toString(map.get(PROGRESS)));
        selfStudyData.setIconUrl(SafeConverter.toString(map.get(ICONURL)));
        selfStudyData.setNotifyContent(SafeConverter.toString(map.get(NOTIFYCONTENT)));
        selfStudyData.setNotifyUniqueId(SafeConverter.toString(map.get(NOTIFYUNIQUEID)));
        return selfStudyData;
    }
}
