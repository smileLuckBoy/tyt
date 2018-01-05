package com.wangm.tyt;

import com.android.chimpchat.adb.AdbBackend;
import com.android.chimpchat.adb.AdbChimpDevice;
import com.android.chimpchat.core.IChimpImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wangmin
 * @version OmniPrime.All rights reserved.
 * @Comments
 * @since 2018/1/3
 */

public class TYT {


    private static AdbChimpDevice device = null;

    private static final int WIDTH = 1080;

    private static final int HEIGHT = 1920;

    static {
        AdbBackend ADB_BACKEND = new AdbBackend("/Users/zsmj/.android/platform-tools/adb", false);

        System.out.println("INIT=================");
        device = (AdbChimpDevice) ADB_BACKEND.waitForConnection(8000, "66a3b42b");
    }

    @Data
    @ToString
    @AllArgsConstructor
    private static class Point {
        int x;
        int y;
    }

    private static void jump(Point p1, Point p2) {
        double z = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));

        int delay = (int) (z * 1.37);
        System.out.println("jump length: " + z + ", delay : " + delay);

        int x = WIDTH / 2;
        int y = HEIGHT / 2;

        int LX = x + (int) (Math.random() * 100);
        int LY = y + (int) (Math.random() * 100);

        device.shell(String.format("input swipe %d %d %d %d %d", LX, LY, LX, LY, delay));
    }

    private static boolean isColor(int source, int dst, int dim) {
        Color currColor = new Color(source);
        Color tmpColor = new Color(dst);

        return Math.abs(currColor.getRed() - tmpColor.getRed()) <= dim && Math.abs(currColor.getBlue() - tmpColor.getBlue()) <= dim && Math.abs(currColor.getGreen() - tmpColor.getGreen()) <= dim;

    }

    private static boolean isTYT(int dst) {
        Color currColor = new Color(TYT_COLOR);
        Color tmpColor = new Color(dst);

        return Math.abs(currColor.getRed() - tmpColor.getRed()) <= 20 && Math.abs(currColor.getBlue() - tmpColor.getBlue()) <= 60 && Math.abs(currColor.getGreen() - tmpColor.getGreen()) <= 20;


    }


    private static boolean isTYT(BufferedImage image, int x, int y, int backGroud) {

        if (!isTYT(image.getRGB(x, y)))
            return false;
        for (int i = 0; i < 100; i += 2) {
            if (isColor(image.getRGB(x, y + i), backGroud, 20)
                    && isColor(image.getRGB(x, i + 5), backGroud, 20)) {
                return true;
            }
        }

        return false;
    }

    private static int COUNTER = 0;

    private static Point getPonitByColor(int currRGB) throws Exception {
        Point point = new Point(0, 0);

        COUNTER++;
        IChimpImage iChimpImage = device.takeSnapshot();
        BufferedImage image = iChimpImage.createBufferedImage();

        boolean found = false;
        for (int i = 1300; i > 1000; i -= 2) {
            for (int j = 150; j < 950; j += 2) {
                if (isColor(currRGB, image.getRGB(j, i), 15)) {
                    if (isColor(currRGB, image.getRGB(j, i - 20), 20)) {
                        found = true;
                        System.out.println("TYT_X:" + j + ",TYT_Y:" + i);
                        return new Point(j, i);
                    }

                }
            }
        }

        if (!found) {
            System.out.println("NOT FOUND TYT...");
        }
        return point;
    }


    private static Point getDstPoint() throws Exception {
        IChimpImage iChimpImage = device.takeSnapshot();
        BufferedImage image = iChimpImage.getBufferedImage();
        int backGroundColor = image.getRGB(500, 200);

        int dstColor = 0;
        boolean start = false;
        int startX = 0;
        int startY = 0;
        int tmp;
        Map<Integer, Boolean> isTYT = new HashMap<Integer, Boolean>();
        for (int i = 500; i < 1000; i += 2) {
            for (int j = 150; j < 1000; j += 2) {
                if (null != isTYT.get(j)) continue;

                tmp = image.getRGB(j, i);
                if (isTYT(image, j, i, backGroundColor)) {
                    isTYT.put(j, true);
                    continue;
                }

                if (!isColor(backGroundColor, tmp, 20) && isTop(image, j, i)) {
                    dstColor = tmp;
                    startX = j;
                    startY = i;
                    start = true;
                    break;
                }
            }

            if (start)
                break;
        }

        int lastX = 0;
        int lastY = 0;

        int centX = 0;
        int centY = 0;
        if (start) {

            System.out.println("process:" + COUNTER);
            System.out.println("startX:" + startX + ",startY:" + startY);

            centX = startX;

            //适配瓶盖
            if (isColor(0xffffff, dstColor, 0) && (isColor(0xf5f5f5, image.getRGB(startX, startY + 24), 0) || isColor(0xffffff, image.getRGB(startX, startY + 24), 0)) && isColor(0xffffff, image.getRGB(startX, startY + 46), 0)
                    && !isColor(0xffffff, image.getRGB(startX - 70, startY + 24), 0) && !isColor(0xffffff, image.getRGB(startX + 70, startY + 24), 0)) {
                System.out.println("瓶盖...");
                centY = startY + 24;
                //棕色443
            } else if (isColor(0x927572, dstColor, 20) && isColor(0xa0766f, image.getRGB(startX, startY + 45), 30) && isColor(0x70554c, image.getRGB(startX, startY + 160), 30)) {
                System.out.println("棕色443...");
                centY = startY + 90;
            } else {
                boolean flag = false;
                for (int i = startY + 300; i > startY; i -= 2) {
                    int j;
                    for (j = startX - 50; j < startX + 50; j += 2) {
                        if (isColor(dstColor, image.getRGB(j, i), 3)
                                && isColor(dstColor, image.getRGB(j, i - 6), 3)) {
                            lastX = j;
                            lastY = i;
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        System.out.println("lastX:" + lastX + ",lastY:" + lastY);
                        centY = (startY + lastY) / 2;
                        break;
                    }
                }

                //未找到下定点
                if (!flag) {
                    System.out.println("未找到下定点，采用默认值+60");
                    centY = startY + 60;
                }

            }
        }


        System.out.println("centX:" + centX + ",centY:" + centY);

        return new Point(centX, centY);
    }

    private static boolean isTop(BufferedImage image, int x, int y) {
        int count = 0;
        int sourceColor = image.getRGB(x, y);
        if (isColor(sourceColor, image.getRGB(x - 1, y + 1), 3)) {
            count++;
        }
        if (isColor(sourceColor, image.getRGB(x, y + 1), 3)) {
            count++;
        }
        if (isColor(sourceColor, image.getRGB(x + 1, y + 1), 3)) {
            count++;
        }

        return count >= 2;
    }


    private static final int TYT_COLOR = 0x3a3a66;

    private static void sleep(long sleepMS) {
        try {
            Thread.sleep(sleepMS);
        } catch (Exception e) {

        }
    }


    private static void fail() {
        IChimpImage iChimpImage = device.takeSnapshot();
        iChimpImage.writeToFile("pic/fail.png", "PNG");
        BufferedImage image = iChimpImage.getBufferedImage();

        if (isColor(0xffffff, image.getRGB(541, 1537), 0) && isColor(0xffffff, image.getRGB(696, 1532), 0) && isColor(0xffffff, image.getRGB(560, 1634), 0)) {
            System.out.println("FAIL...");
            device.shell("input tap 978 299");
            sleep(3000);
            device.shell("input tap 540 1580");
            sleep(3000);
        }
    }


    /**
     * ADB后台进程
     */
    public static void main(String[] args) throws Exception {

        while (true) {
            sleep(3000);
            Point tytPoint = getPonitByColor(TYT_COLOR);
            Point dstPoint = getDstPoint();

            jump(tytPoint, dstPoint);

            //挂了检测重来
            fail();
        }
    }
}
