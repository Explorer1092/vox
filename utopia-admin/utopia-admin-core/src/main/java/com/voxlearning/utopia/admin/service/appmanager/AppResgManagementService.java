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

package com.voxlearning.utopia.admin.service.appmanager;

import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResg;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import com.voxlearning.utopia.service.vendor.client.VendorResgContentServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
public class AppResgManagementService extends AbstractAdminService {

    @Inject private VendorResgContentServiceClient vendorResgContentServiceClient;

    public VendorResg getResg(Long resgId) {
        VendorResg resg = vendorLoaderClient.getExtension().loadVendorResg(resgId);
        if (resg == null) {
            return null;
        }
        resg.setResgContentList(vendorResgContentServiceClient.getVendorResgContentService()
                .loadAllVendorResgContentsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(resg.getId(), e.getResgId()))
                .collect(Collectors.toList()));
        return resg;
    }

    public List<VendorResg> getResgList() {
        List<VendorResg> resgList = vendorLoaderClient.loadVendorResgsIncludeDisabled().values()
                .stream()
                .filter(t -> !t.isDisabledTrue())
                .collect(Collectors.toList());
        if (resgList.size() > 0) {
            for (VendorResg resg : resgList) {
                resg.setResgContentList(vendorResgContentServiceClient.getVendorResgContentService()
                        .loadAllVendorResgContentsFromDB()
                        .getUninterruptibly()
                        .stream()
                        .filter(e -> Objects.equals(resg.getId(), e.getResgId()))
                        .collect(Collectors.toList()));
            }
        }
        return resgList;
    }

    public void deleteResg(Long resgId) {
        vendorServiceClient.deleteVendorResgs(resgId);
    }

    public VendorResgContent getResgContent(Long resgContentId) {
        return vendorResgContentServiceClient.getVendorResgContentService()
                .loadAllVendorResgContentsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(resgContentId, e.getId()))
                .findFirst()
                .orElse(null);
    }

}