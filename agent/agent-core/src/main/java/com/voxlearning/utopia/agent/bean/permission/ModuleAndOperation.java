package com.voxlearning.utopia.agent.bean.permission;

import com.voxlearning.utopia.agent.view.permission.ModuleAndOperationView;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 功能模块及操作的对应关系
 *
 * @author song.wang
 * @date 2018/5/9
 */
@Getter
@Setter
public class ModuleAndOperation implements Serializable{
    private static final long serialVersionUID = 658840466301706344L;

    private String module;                   // 功能模块
    private String subModule;                // 子功能模块
    private String path;                     // 接口路径  account/user/index 形式
    private String operationDesc;            // 操作描述
    private boolean deprecated;              // 是否Deprecated

    public ModuleAndOperationView toViewData(){
        ModuleAndOperationView item = new ModuleAndOperationView();
        item.setModule(this.module);
        item.setSubModule(this.subModule);
        item.setPath(this.path);
        item.setOperationDesc(this.operationDesc);
        item.setDeprecated(this.deprecated);
        return item;
    }
}
