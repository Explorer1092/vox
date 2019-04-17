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

package com.voxlearning.utopia.admin.data;


import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.voxlearning.utopia.service.config.api.constant.AdDetailAuditStatus.DRAFT;
import static com.voxlearning.utopia.service.config.api.constant.AdDetailAuditStatus.PENDING;

/**
 * 广告查询条件
 */
@Getter
@Setter
public class AdQueryContext implements Serializable {
    private static final long serialVersionUID = -7192032955389931334L;

    private Long adId;                // 广告ID
    private String slotId;            // 广告位ID
    private String creator;           // 创建人
    private String auditor;           // 审核人
    private Integer status;           // 状态
    private Integer auditStatus;      // 审核状态
    private String startDate;         // 开始日期
    private String endDate;           // 结束日期
    private Integer expireStatus;     // 过期状态
    private String businessCategory;  // 业务类型
    private String type;              // 广告类型
    private Integer page;             // 当前页码
    private String adCode;            //广告编码

    public Page<AdvertisementDetail> filterList(List<AdvertisementDetail> adCandidates) {
        if (CollectionUtils.isEmpty(adCandidates)) {
            return new PageImpl<>(Collections.emptyList());
        }
        page = Integer.max(1, page);
        Pageable pageable = new PageRequest(page - 1, 10);
        if (byAdId()) {
            return new PageImpl<>(adCandidates);
        }

        Stream<AdvertisementDetail> candidatesStream = adCandidates.stream().filter(Objects::nonNull);
        if (StringUtils.isNotBlank(slotId)) {
            candidatesStream = candidatesStream.filter(ad -> slotId.equals(ad.getAdSlotId()));
        }
        if (StringUtils.isNotBlank(adCode)) {//adCode兼容id
            candidatesStream = candidatesStream.filter(ad -> adCode.equals(ad.getAdCode()) || adCode.equals(ad.getId() + ""));
        }
        if (StringUtils.isNotEmpty(creator)) {
            candidatesStream = candidatesStream.filter(ad -> creator.equals(ad.getCreator()));
        }
        if (StringUtils.isNotBlank(auditor)) {
            candidatesStream = candidatesStream.filter(ad -> auditor.equals(ad.getAuditor()));
        }
        if (auditStatus != null && auditStatus >= 0) {
            candidatesStream = candidatesStream.filter(ad -> ad.getAuditStatus() - auditStatus >= DRAFT.getStatus() && ad.getAuditStatus() - auditStatus <= PENDING.getStatus());
        }
        if (status != null && status >= 0) {
            candidatesStream = candidatesStream.filter(ad -> status.equals(ad.getStatus()));
        }
        if (StringUtils.isNotEmpty(startDate)) {
            Date start = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATE);
            candidatesStream = candidatesStream.filter(ad -> start != null && ad.getShowTimeEnd() != null && ad.getShowTimeEnd().after(start));
        }
        if (StringUtils.isNotEmpty(endDate)) {
            Date end = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE);
            candidatesStream = candidatesStream.filter(ad -> end != null && ad.getShowTimeStart() != null && ad.getShowTimeStart().before(end));
        }
        if (expireStatus != null && expireStatus >= 0) {
            if (expireStatus == 1) {
                candidatesStream = candidatesStream.filter(ad -> ad.getShowTimeEnd() != null && ad.getShowTimeEnd().before(new Date()));
            } else {
                candidatesStream = candidatesStream.filter(AdvertisementDetail::hasNotExpired);
            }
        }
        if (StringUtils.isNotBlank(businessCategory)) {
            candidatesStream = candidatesStream.filter(ad -> StringUtils.equals(ad.getBusinessCategory(), businessCategory));
        }
        if (StringUtils.isNotBlank(type)) {
            candidatesStream = candidatesStream.filter(ad -> StringUtils.equals(ad.getType(), type));
        }

        List<AdvertisementDetail> allAdsList = candidatesStream.collect(Collectors.toList());

        // 如果传递page超过数据展示的最大页数，则拿最后一页
//        page = Integer.max(1, Math.min(page, (allAdsList.size() + 9) / 10));

        return PageableUtils.listToPage(allAdsList, pageable);
    }

    public boolean byAdId() {
        return adId != null && adId > 0L;
    }

}
