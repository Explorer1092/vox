/**
 * Author:   xianlong.zhang
 * Date:     2018/9/13 18:38
 * Description: 大考 考试 学校关系类
 *   负责考试的同事直接往表中写考试学校关联数据
 *   天玑
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.exam;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "klx_exam")
@DocumentCollection(collection = "tianji_exams")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180917")
public class AgentExamSchoolNew implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private String  examId;
    private String name;
    private Integer grade;
    private Long schoolId;
    private Integer regionCode; //学校所在区域编码
    private Date createDateTime; //考试时间
    private Boolean disabled;
    private Boolean distributionState; //是否分配  分配完后更新下这个数据
    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
//                newCacheKey(new String[]{"countyCode"}, new Object[]{countyCode})
        };
    }
}
