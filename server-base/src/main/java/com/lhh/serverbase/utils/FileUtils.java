package com.lhh.serverbase.utils;

import com.lhh.serverbase.common.constant.Const;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;

/**
 * 文件操作类
 */
public class FileUtils {
    /**
     * 格式化保留两位小数
     */
    private static DecimalFormat df = new DecimalFormat("######0.00");

    /**
     * 文件大小转换
     *
     * @param orgSize 原文件大小（单位：B）
     * @param unit    转换单位（B,K,M,G）
     * @return
     */
    public static double FileSizeConvert(long orgSize, String unit) {
        double fileSize = 0;
        switch (unit) {
            case Const.STR_B:
                fileSize = Double.valueOf(df.format((double) orgSize));
                break;
            case Const.STR_K:
                fileSize = Double.valueOf(df.format((double) orgSize / 1024));
                break;
            case Const.STR_M:
                fileSize = Double.valueOf(df.format((double) orgSize / 1048576));
                break;
            case Const.STR_G:
                fileSize = Double.valueOf(df.format((double) orgSize / 1073741824));
                break;
            default:
                break;
        }
        return fileSize;
    }

    /**
     * 根据文件名获取扩展名
     *
     * @param fileName
     * @return
     */
    public static String GetExtName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        if (fileName.indexOf(Const.STR_SPOT) > 0) {
            return fileName.substring(fileName.lastIndexOf(Const.STR_SPOT) + 1);
        }
        return null;
    }
}
