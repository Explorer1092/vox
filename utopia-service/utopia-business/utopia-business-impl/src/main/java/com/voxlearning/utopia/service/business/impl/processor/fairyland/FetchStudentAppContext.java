package com.voxlearning.utopia.service.business.impl.processor.fairyland;

import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.business.api.context.AbstractContext;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruib
 * @since 2019/1/2
 */
@Getter
@Setter
@NoArgsConstructor
public class FetchStudentAppContext extends AbstractContext<FetchStudentAppContext> {
    private static final long serialVersionUID = 1094898681716802130L;

    // in
    private Long studentId;
    private String version;
    private AppSystemType ast;

    // middle
    private StudentDetail student;
    private boolean white;
    private List<VendorApps> vas;
    private Map<String, VendorApps> vam;
    private List<FairylandProduct> fps;
    private Map<String, FairylandProduct> fpm;
    private Set<String> candidates;

    // out
    private List<Map<String, Object>> apps = new ArrayList<>();
}
