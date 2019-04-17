package com.voxlearning.utopia.service.newhomework.api.entity.poetry;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentIdAutoGenerator;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.random.RandomUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 古诗活动
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-siberia")
@DocumentDatabase(database = "vox-poetry")
@DocumentCollection(collection = "poetry_activity")
@UtopiaCacheExpiration(604800)
@UtopiaCacheRevision("20190220")
public class AncientPoetryActivity implements Serializable {
    private static final long serialVersionUID = 5527714420837502555L;

    @DocumentId(autoGenerator = DocumentIdAutoGenerator.NONE)
    private String id;                          // 活动ID

    private String name;                        // 活动名称
    private String coverImgUrl;                 // 封面图url
    private String backgroundImgUrl;            // 背景图url
    private String backgroundTopImgUrl;         // 背景头部图url
    private String missionTopImgUrl;            // 关卡头部图url
    private List<Region> regions;               // 地区列表
    private List<String> label;                 // 标签
    private List<Mission> missions;              // 包含古诗(关卡)
    @DocumentCreateTimestamp
    private Date createAt;                      // 创建时间
    private Date startDate;                     // 开始时间
    private Date endDate;                       // 结束时间
    private List<Integer> classLevel;           // 适用年级
    private Boolean disabled;                   // 默认false，删除true
    private Long joinCount;                     // 活动参加人数

    public static String ck_id(String id) {
        return CacheKeyGenerator.generateCacheKey(AncientPoetryActivity.class, id);
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class ID implements Serializable {
        private static final long serialVersionUID = -6137643316995850166L;

        private String month;
        private String randomId = RandomUtils.nextObjectId();

        public ID(String month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month + "_" + randomId;
        }
    }

    @Getter
    @Setter
    public static class Mission implements Serializable {
        private static final long serialVersionUID = -1418790989157858276L;

        private String missionId;                 //关卡ID(古诗ID)
        private String missionName;             //关卡名称(古诗名)
        private String coverImgUrl;             //关卡图
        private String backgroundImgUrl;        //关卡背景图(河流)
        private String signImgUrl;              //关卡标记图(第几关)
    }

    @Getter
    @Setter
    public static class Region implements Serializable {
        private static final long serialVersionUID = 6500693510512463046L;
        private RegionLevel regionLevel;     // 区域级别 0：全国,1：省级,2：市级,3：区级,4：校级
        private List<Long> regionIds;        // 地区ID列表
    }

    @AllArgsConstructor
    public enum RegionLevel {
        nation("全国"),
        province("省"),
        city("市"),
        country("区"),
        school("学校");

        @Getter
        private final String desc;

        public static RegionLevel of(String name) {
            try {
                return valueOf(name);
            } catch (Exception ex) {
                return school;
            }
        }
    }
}
