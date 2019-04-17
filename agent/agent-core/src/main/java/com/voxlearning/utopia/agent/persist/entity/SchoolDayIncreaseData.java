/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2016/2/18
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "school_day_increase_data")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20170517")
public class SchoolDayIncreaseData implements Serializable {
    private static final long serialVersionUID = 4849685573793550880L;

    @DocumentId
    private String id;
    private Long monthStuRegNum;           //月新增学生注册数
    private Long monthStuAuthNum;          //月新增学生认证数
    private Long monthTeaRegNum;           //月新增老师注册数
    private Long monthTeaAuthNum;          //月新增老师认证数

    private Long addStuRegNum;             //新增学生注册数
    private Long addStuAuthNum;            //新增学生认证数
    private Long addTeaRegNum;             //新增老师注册数
    private Long addTeaAuthNum;            //新增老师认证数
    private Integer engMauc;             // 本月英语月活  17英语业绩
    private Integer mathMauc;             // 本月数学月活  17数学业绩
    private Integer klxScanMathTpCount;   // 快乐学数学扫描量  快乐学数学业绩
    private Integer engMaucDf;         // 昨日英语月活日浮
    private Integer mathMaucDf;         // 昨日数学月活日浮
    private Integer klxScanMathTpCountDf;   // 快乐学数学扫描量日浮

    private Integer engAddMauc; // 英语新增月活
    private Integer engAddMaucDf; // 英语新增月活日浮

    private Integer engBfMauc; // 英语回流月活
    private Integer engBfMaucDf; // 英语回流月活日浮

    private Integer mathAddMauc; // 数学新增月活
    private Integer mathAddMaucDf; // 数学新增月活日浮

    private Integer mathBfMauc; // 数学回流月活
    private Integer mathBfMaucDf; // 数学回流月活日浮

    private Integer klxMathAddMauc; // 快乐学数学新增月活
    private Integer klxMathAddMaucDf; // 快乐学数学新增月活日浮

    private Integer klxMathBfMauc; // 数学回流月活
    private Integer klxMathBfMaucDf; // 数学回流月活日浮

    private Integer sglSubjMauc; // 单科月活
    private Integer sglSubjMaucDf; // 单科月活日浮

    private Integer sglSubjBfMauc; // 单科回流月活
    private Integer sglSubjBfMaucDf; // 单科回流月活日浮

    private Integer sglSubjAddMauc; // 单科新增月活
    private Integer sglSubjAddMaucDf; // 单科新增月活日浮

    private Integer chnMauc; // 语文月活
    private Integer chnMaucDf; // 语文月活日浮

    private Integer chnBfMauc; // 语文回流月活
    private Integer chnBfMaucDf; // 语文回流月活日浮

    private Integer chnAddMauc; // 语文新增月活
    private Integer chnAddMaucDf; // 语文新增月活日浮


    private Integer scanNumGte1StuCount;  // 扫描1次及以上的学生数
    private Integer scanNumGte1StuCountDf;  // 扫描1次及以上的学生数日浮

    private Integer scanNumGte1AddStuCount;  // 新增扫描1次及以上的学生数
    private Integer scanNumGte1AddStuCountDf;  // 新增扫描1次及以上的学生数日浮

    private Integer scanNumGte1BfStuCount;   // 回流扫描1次及以上的学生数
    private Integer scanNumGte1BfStuCountDf;  // 回流扫描1次及以上的学生数日浮

    private Integer day;                   //统计的天
    private Long schoolId;                 //学校ID
    private String schoolName;             //学校名称
    private Integer schoolLevel;           //学校等级
    private Integer provinceCode;          //省code
    private String provinceName;           //省name
    private Integer cityCode;              //市code
    private String cityName;               //市name
    private Integer countyCode;            //区code
    private String countyName;             //区name
    private Long monthSascBackFlow;        //本月单科回流
    private Long monthDascBackFlow;        //本月双科回流

    // ------------------------------------------------------------------------------------------------
    // Alex 20160711
    // 以下是老字段，为了兼容历史数据所以保留下来，新的数据生产逻辑可以忽略以下字段
    // 开发也不要用以下字段
    // ------------------------------------------------------------------------------------------------
//    @Deprecated private Long stuSlNum;                 //高覆盖地区结算数
//    @Deprecated private Long stuSlDsaNum;              //双科认证数
//    @Deprecated private Long addStuAuthGradeMathNum;   //1~2年级数学新增认证学生数

    public static String ck_sid_day(Long schoolId, Integer day) {
        return CacheKeyGenerator.generateCacheKey(SchoolDayIncreaseData.class,
                new String[]{"id", "d"},
                new Object[]{schoolId, day});
    }

    public static String ck_region_day(Integer regionCode, Integer day) {
        return CacheKeyGenerator.generateCacheKey(SchoolDayIncreaseData.class,
                new String[]{"r", "d"},
                new Object[]{regionCode, day});
    }

    public String toSchoolUnique() {
        return SafeConverter.toLong(schoolId) + "-" + SafeConverter.toInt(day);
    }

    public String toRegionUnique() {
        return SafeConverter.toLong(countyCode) + "-" + SafeConverter.toInt(day);
    }

}
