package com.voxlearning.utopia.service.vendor.api.mapper;

import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Getter
@Setter
@NoArgsConstructor
public class VersionedTextBookManagementList implements Serializable {
    private static final long serialVersionUID = 4125317704458702094L;

    private long version;
    private List<TextBookManagement> textBookManagementList;
    private List<TextBookMapper> textBookMapperList;

    public VersionedTextBookManagementList(long version, List<TextBookManagement> textBookManagementList,
                                           List<TextBookMapper> textBookMapperList){
        this.version = version;
        this.textBookManagementList = textBookManagementList;
        this.textBookMapperList = textBookMapperList;
    }

}
