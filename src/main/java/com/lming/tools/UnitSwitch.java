package com.lming.tools;
import java.text.DecimalFormat;
public class UnitSwitch {
    /**
     * 处理文件大小
     */
    public static String formatSize(long size) {
        DecimalFormat fnum = new DecimalFormat("##0.0");
        String result;

        if (size > (1024 * 1024 * 1024)) {
            result = fnum.format((size / ((float) (1024 * 1024 * 1024)))) + "GB";
        }else if (size > (1024 * 1024)) {
            result = fnum.format((size / ((float) (1024 * 1024)))) + "MB";
        } else if (size > 1024) {
            result = fnum.format((float) size / ((float) 1024)) + "KB";
        } else {
            result = String.valueOf((int) size) + "B";
        }
        return result;
    }

    /**
     * @author gaoqiang
     *         <p>
     *         计算网络速率
     *         <p>
     *         返回的格式 Kb/s
     * @param delta
     *            ： 传入一个Bytes数据
     * @param duration_time
     *            ： 下载delta数据所需要用的时间
     * @return
     */
    public static String calculateSpeed(long delta, long duration_time) {
        DecimalFormat fnum = new DecimalFormat("##0.0");
        String speedStr;
        float speed = ((float) delta * 1000 / (float) (duration_time));// 毫秒转换

        if (speed > (1024 * 1024)) {
            speedStr = fnum.format((speed / ((float) (1024 * 1024)))) + "M/s";
        } else if (speed > 1024) {
            speedStr = fnum.format((float) speed / ((float) 1024)) + "K/s";
        } else {
            speedStr = String.valueOf((int) speed) + "B/s";
        }
        return speedStr;
    }
}
