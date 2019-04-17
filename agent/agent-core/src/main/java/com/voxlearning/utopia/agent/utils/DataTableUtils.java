package com.voxlearning.utopia.agent.utils;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.DataTableInfo;
import com.voxlearning.utopia.agent.bean.datareport.CityDataReportData;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DataTable数据封装方法
 * Created by yaguang.wang on 2016/10/14.
 */
public class DataTableUtils {

    public static DataTableInfo createDataTableInfo(List<Object> dataList, List<String> fieldNames) {
        if (CollectionUtils.isEmpty(dataList)) {
            return null;
        }
        DataTableInfo data = new DataTableInfo();
        data.setDraw(1);
        data.setRecordsFiltered(dataList.size());
        data.setRecordsTotal(dataList.size());
        List<List<Object>> dataContentList = new ArrayList<>();
        dataList.forEach(p -> dataContentList.add(createDataTable(p, fieldNames)));
        data.setData(dataContentList);
        return data;
    }

    private static List<Object> createDataTable(Object data, List<String> fieldNames) {
        List<Object> result = new ArrayList<>();
        fieldNames.forEach(p -> {
            try {
                Field filed = data.getClass().getDeclaredField(p);
                PropertyDescriptor pd = new PropertyDescriptor(filed.getName(), data.getClass());
                result.add(SafeConverter.toString(pd.getReadMethod().invoke(data)));
            } catch (Exception e) {
                return;
            }
        });
        return result;
    }

    public static void main(String[] args) throws NoSuchFieldException, IntrospectionException {
        CityDataReportData data = new CityDataReportData();
        Object obj = data;
        Field filed = obj.getClass().getDeclaredField("regionName");
        PropertyDescriptor pd = new PropertyDescriptor(filed.getName(), obj.getClass());
        System.out.println(pd.getReadMethod());
        System.out.println("---------------------------------------------------------------");
        Arrays.stream(obj.getClass().getMethods()).forEach(p -> {
            System.out.println(p);
        });

    }
}
