package com.voxlearning.utopia.agent.mockexam.service.dto.output;

import com.voxlearning.utopia.agent.mockexam.service.dto.TreeNode;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 区域树节点
 *
 * @author xiaolei.li
 * @version 2018/8/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionTreeNode extends TreeNode<RegionDto> {

    /**
     * 区域类型
     *
     * @see RegionType
     */
    private RegionType type;
}
