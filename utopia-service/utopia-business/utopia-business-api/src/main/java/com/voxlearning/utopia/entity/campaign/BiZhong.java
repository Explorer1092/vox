package com.voxlearning.utopia.entity.campaign;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Summer on 2016/12/19.
 * <p>
 * 2016年会抽奖  必中！！！
 * 然而并没有中！！！17-02-04 路过
 */
@DocumentTable(table = "VOX_BIZHONG")
@NoArgsConstructor
@DocumentConnection(configName = "hs_misc")
public class BiZhong extends AbstractDatabaseEntity {
    private static final long serialVersionUID = -8854634244321876456L;
    @Getter @Setter @UtopiaSqlColumn private Long workNo;           // 工号
    @Getter @Setter @UtopiaSqlColumn private String userName;       // 姓名
    @Getter @Setter @UtopiaSqlColumn private Boolean disabled;
}