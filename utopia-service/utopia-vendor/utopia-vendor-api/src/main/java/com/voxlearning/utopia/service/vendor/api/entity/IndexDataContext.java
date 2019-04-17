package com.voxlearning.utopia.service.vendor.api.entity;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author jiangpeng
 * @since 2017-10-20 上午11:23
 **/
@Data
public class IndexDataContext implements Serializable {

    private static final long serialVersionUID = -4094862611763340368L;
    private Map<SelfStudyType, FairylandProduct> sst2FairyLandProductMap;
    Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMa;
    Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMapKeepExpired;
    private String currentSchema;
    private String currentCdnSchema;

    private StudentDetail studentDetail;
    private User Parent;
    private boolean login;
    private boolean hasChild;
    private boolean shenhe;
    private boolean hasZuoYeCuoTi;
    private String channel;
    private boolean inBlackList;
    private String version;
    private String sys;
    private boolean hitAdIgnoreGray;
    private Position position;
    private boolean hitLiveCastAdIgnoreGray;

    private boolean hitLiveCastImageAdGray;
    @Deprecated
    private boolean hitXiaoUReportGray;

    private boolean hitShutiaoGray;

    private Set<SelfStudyType> jztAvailableSSTSet;

    private Map<SelfStudyType, AlpsFuture<StudyAppData>> studyAppDataFutureMap;

    private Map<String, String> appKey2DescMap;

    private boolean above223; //是否高于2.2.3.0版本

    public void setVersion(String version){
        this.version = version;
        this.above223 = VersionUtil.compareVersion(version, "2.2.3.0") >= 0;
    }

    public static IndexDataContext empty = new IndexDataContext();
    static {
        empty.setSelfStudyTypeDayRangeMa(Collections.emptyMap());
        empty.setSst2FairyLandProductMap(Collections.emptyMap());
        empty.setCurrentSchema("");
        empty.setCurrentCdnSchema("");
        empty.setStudyAppDataFutureMap(Collections.emptyMap());
        empty.setAppKey2DescMap(Collections.emptyMap());
    }

    public enum  Position{
        index,
        edit
    }
}
