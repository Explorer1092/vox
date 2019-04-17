package com.voxlearning.utopia.entity.task;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 老师特权信息表
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-teacher-app")
@DocumentCollection(collection = "vox_teacher_task_privilege")
@UtopiaCacheRevision("20181026")
public class TeacherTaskPrivilege implements CacheDimensionDocument {

    private static final long serialVersionUID = -231027556128090225L;

    @DocumentId private Long id;                            //老师ID
    private List<Privilege> privileges;                     //特权信息
    private Integer level;                                  //当前老师的等级
    private Long levelTime;                                 //等级生效时间
    @DocumentCreateTimestamp private Long createTime;       //创建时间
    @DocumentUpdateTimestamp private Long updateTime;       //上次修改事件

    @Getter
    @Setter
    public static class Privilege implements Serializable {
        private static final long serialVersionUID = -4947791250459362097L;
        private Long id;                                //特权的ID
        private String name;                            //冗余一下特权名字，方便查看，业务以TPL为准
        private Integer times;                          //总次数, NULL表示不限制次数
        private Integer useTimes;                       //已经使用的次数
        private Long expireTime;                        //特权过期时间
        private String createDate;                      //特权获取时间
        private String updateDate;                      //特权更新时间
        private List<PrivilegeCoupon> privilegeCoupons; //如果是券的话，则有券的信息
        private Map<String, Object> ext;                //特权的扩展属性信息
    }

    @Getter
    @Setter
    public static class PrivilegeCoupon implements Serializable {
        private static final long serialVersionUID = 3055801619949897483L;
        private Boolean isCoupon;           //是否是券类型
        private String couponUserRefId;     //关联的券与用户的ID
        private Boolean isUsed;             //是否已经使用
        private String userSystem;          //消费的相关功能
        private String useDate;             //使用的时间
        private String createDate;          //生成日期
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
        };
    }

    @DocumentFieldIgnore
    public Privilege getByPrivilegeTplId(Long id) {
        if (id == null) {
            return null;
        }
        for (Privilege privilege : privileges) {
            if (Objects.equals(id, privilege.getId())) {
                return privilege;
            }
        }
        return null;
    }

}
