package com.voxlearning.utopia.service.business.buffer.mapper;

import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TeachingResourceList implements Serializable {
    private static final long serialVersionUID = 2839359292124950801L;

    private List<TeachingResource> teachingResourceList;
    public List<TeachingResourceRaw> teachingResourceRawList;
    private long version;
}