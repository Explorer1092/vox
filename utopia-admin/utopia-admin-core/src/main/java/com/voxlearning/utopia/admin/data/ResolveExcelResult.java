package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author peng.zhang.a
 * @since 17-7-5
 */
@Getter
@Setter
public class ResolveExcelResult implements Serializable {
    private static final long serialVersionUID = -4117203647879867580L;


    private String fileName;
    private List<List<String>> data;


    public static ResolveExcelResult newInstance(String fileName, List<List<String>> data) {
        ResolveExcelResult result = new ResolveExcelResult();
        result.setFileName(fileName);
        result.setData(data);
        return result;
    }

    public String fetchSimpleFileName() {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        int index = fileName.lastIndexOf((int) '.');
        if (index < 0) {
            return null;
        }
        return fileName.substring(0, index);

    }
}
