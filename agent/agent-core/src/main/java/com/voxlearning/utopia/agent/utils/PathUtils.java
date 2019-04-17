package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.agent.view.permission.ModuleAndOperationView;
import com.voxlearning.utopia.agent.bean.permission.SystemModuleAndOperationHolder;

import java.util.List;

/**
 *
 *
 * @author song.wang
 * @date 2018/5/9
 */
public class PathUtils {

    // 输入 /account/user/index.vpage 返回 account/user/index形式的
    public static String resolvePath(String path){
        if(StringUtils.isBlank(path)){
            return "";
        }
        String result = "";
        String s = StringUtils.replace(path, "/", " ");
        s = StringUtils.replace(s, ".vpage", "");

        String[] paths = s.split(" ");
        for(String tmp : paths){
            if(StringUtils.isNotBlank(tmp)){
                result += "/" + tmp;
            }
        }
        return StringUtils.removeStart(result, "/");
    }

    public static void main(String[] args) {
        System.out.println(PathUtils.resolvePath("/"));
        System.out.println(PathUtils.resolvePath(" /"));
        System.out.println(PathUtils.resolvePath("user/"));
        System.out.println(PathUtils.resolvePath("user//"));
        System.out.println(PathUtils.resolvePath(""));
        System.out.println(PathUtils.resolvePath("// "));
        System.out.println(PathUtils.resolvePath("//ta"));
        System.out.println(PathUtils.resolvePath("//ta//s"));
        System.out.println(PathUtils.resolvePath("//ta/s"));


        List<ModuleAndOperationView> list = SystemModuleAndOperationHolder.getInstance().getSystemModuleAndOperation();
        System.out.println(JsonUtils.toJson(list));

    }

}
