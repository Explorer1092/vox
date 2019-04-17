package com.voxlearning.utopia.service.vendor.buffer;

import com.voxlearning.utopia.service.vendor.api.entity.VendorResgContent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class VersionedVendorResgContentList implements Serializable {
    private static final long serialVersionUID = -6080207937753965389L;

    private long version;
    private List<VendorResgContent> contentList;
}
