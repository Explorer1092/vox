package com.voxlearning.utopia.service.campaign.impl.service.excel;

import org.apache.poi.xssf.usermodel.XSSFRow;

@FunctionalInterface
public interface WriteRow<T> {
    void write(XSSFRow row, int columnIndex, T data);
}