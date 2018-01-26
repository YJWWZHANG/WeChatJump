package com.supersingledog.wechatjump;

import android.graphics.Bitmap;

import com.google.common.primitives.Ints;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class CalcUtils {

    public static final int R_TARGET = 40;
    public static int G_TARGET = 43;
    public static int B_TARGET = 86;

    public static final int BOTTLE_TARGET = 255;

    public static int[] findMyPos(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int[] ret = {0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = height / 4; j < height * 3 / 4; j++) {
                int pixel = bitmap.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (match(r, g, b, R_TARGET, G_TARGET, B_TARGET, 16)) {
                    maxX = Ints.max(maxX, i);
                    minX = Ints.min(minX, i);
                    maxY = Ints.max(maxY, j);
                    minY = Ints.min(minY, j);
                }
            }
        }
        ret[0] = (maxX + minX) / 2 + 3;
        ret[1] = maxY;
        return ret;
    }

    public static int[] findNextCenter(Bitmap bitmap, int[] myPos) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixel = bitmap.getPixel(0, 200);
        int r1 = (pixel & 0xff0000) >> 16;
        int g1 = (pixel & 0xff00) >> 8;
        int b1 = (pixel & 0xff);
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < width; i++) {
            pixel = bitmap.getPixel(i, height - 1);
            if (map.containsKey(pixel)) {
                map.put(pixel, map.get(pixel) + 1);
            } else {
                map.put(pixel, 1);
            }
        }
        int max = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                pixel = entry.getKey();
                max = entry.getValue();
            }
        }
        int r2 = (pixel & 0xff0000) >> 16;
        int g2 = (pixel & 0xff00) >> 8;
        int b2 = (pixel & 0xff);

        int t = 16;

        int minR = Ints.min(r1, r2) - t;
        int maxR = Ints.max(r1, r2) + t;
        int minG = Ints.min(g1, g2) - t;
        int maxG = Ints.max(g1, g2) + t;
        int minB = Ints.min(b1, b2) - t;
        int maxB = Ints.max(b1, b2) + t;

        System.out.println(minR + ", " + minG + ", " + minB);
        System.out.println(maxR + ", " + maxG + ", " + maxB);

        int[] ret = new int[6];
        int targetR = 0, targetG = 0, targetB = 0;
        boolean found = false;
        for (int j = height / 4; j < myPos[1]; j++) {
            for (int i = 0; i < width; i++) {
                int dx = Math.abs(i - myPos[0]);
                int dy = Math.abs(j - myPos[1]);
                if (dy > dx) {
                    continue;
                }
                pixel = bitmap.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (r < minR || r > maxR || g < minG || g > maxG || b < minB || b > maxB) {
                    j = j + 2;
                    ret[0] = i;
                    ret[1] = j;
                    System.out.println("top, x: " + i + ", y: " + j);
                    for (int k = 0; k < 5; k++) {
                        pixel = bitmap.getPixel(i, j + k);
                        System.out.println(pixel);
                        targetR += (pixel & 0xff0000) >> 16;
                        targetG += (pixel & 0xff00) >> 8;
                        targetB += (pixel & 0xff);
                    }
                    targetR /= 5;
                    targetG /= 5;
                    targetB /= 5;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        if (targetR == BOTTLE_TARGET && targetG == BOTTLE_TARGET && targetB == BOTTLE_TARGET) {
            return findBottle(bitmap, ret[0], ret[1]);
        }

        boolean[][] matchMap = new boolean[width][height];
        boolean[][] vMap = new boolean[width][height];
        ret[2] = Integer.MAX_VALUE;
        ret[3] = Integer.MAX_VALUE;
        ret[4] = Integer.MIN_VALUE;
        ret[5] = Integer.MAX_VALUE;

        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(ret);
        while (!queue.isEmpty()) {
            int[] item = queue.poll();
            int i = item[0];
            int j = item[1];
//            int dx = Math.abs(i - myPos[0]);
//            int dy = Math.abs(j - myPos[1]);
//            if (dy > dx) {
//                continue;
//            }
            if (j >= myPos[1]) {
                continue;
            }

            if (i < Ints.max(ret[0] - 300, 0) || i >= Ints.min(ret[0] + 300, width) || j < Ints.max(0, ret[1] - 400) || j >= Ints.max(height, ret[1] + 400) || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            pixel = bitmap.getPixel(i, j);
            int r = (pixel & 0xff0000) >> 16;
            int g = (pixel & 0xff00) >> 8;
            int b = (pixel & 0xff);
            matchMap[i][j] = match(r, g, b, targetR, targetG, targetB, 16);
//            if (i == ret[0] && j == ret[1]) {
//                System.out.println(matchMap[i][j]);
//            }
            if (matchMap[i][j]) {
                if (i < ret[2]) {
                    ret[2] = i;
                    ret[3] = j;
                } else if (i == ret[2] && j < ret[3]) {
                    ret[2] = i;
                    ret[3] = j;
                }
                if (i > ret[4]) {
                    ret[4] = i;
                    ret[5] = j;
                } else if (i == ret[4] && j < ret[5]) {
                    ret[4] = i;
                    ret[5] = j;
                }
                if (j < ret[1]) {
                    ret[0] = i;
                    ret[1] = j;
                }
                queue.add(buildArray(i - 1, j));
                queue.add(buildArray(i + 1, j));
                queue.add(buildArray(i, j - 1));
                queue.add(buildArray(i, j + 1));
            }
        }
        return ret;
    }

    private static int[] findBottle(Bitmap bitmap, int i, int j) {
        int[] ret = new int[6];
        ret[0] = i;
        ret[1] = j;
        ret[2] = Integer.MAX_VALUE;
        ret[3] = Integer.MAX_VALUE;
        ret[4] = Integer.MIN_VALUE;
        ret[5] = Integer.MAX_VALUE;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        boolean[][] vMap = new boolean[width][height];
        Queue<int[]> queue = new ArrayDeque<>();
        int[] pos = {i, j};
        queue.add(pos);

        while (!queue.isEmpty()) {
            pos = queue.poll();
            i = pos[0];
            j = pos[1];
            if (i < 0 || i >= width || j < 0 || j > height || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            int pixel = bitmap.getPixel(i, j);
            int r = (pixel & 0xff0000) >> 16;
            int g = (pixel & 0xff00) >> 8;
            int b = (pixel & 0xff);
            if (r == BOTTLE_TARGET && g == BOTTLE_TARGET && b == BOTTLE_TARGET) {
                //System.out.println("("+i+", "+j+")");
                if (i < ret[2]) {
                    ret[2] = i;
                    ret[3] = j;
                } else if (i == ret[2] && j < ret[3]) {
                    ret[2] = i;
                    ret[3] = j;
                }
                if (i > ret[4]) {
                    ret[4] = i;
                    ret[5] = j;
                } else if (i == ret[4] && j < ret[5]) {
                    ret[4] = i;
                    ret[5] = j;
                }
                if (j < ret[1]) {
                    ret[0] = i;
                    ret[1] = j;
                }
                queue.add(buildArray(i - 1, j));
                queue.add(buildArray(i + 1, j));
                queue.add(buildArray(i, j - 1));
                queue.add(buildArray(i, j + 1));
            }
        }
        return ret;
    }

    public static int[] findWhitePoint(Bitmap bitmap, int x1, int y1, int x2, int y2) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        x1 = Ints.max(x1, 0);
        x2 = Ints.min(x2, width - 1);
        y1 = Ints.max(y1, 0);
        y2 = Ints.min(y2, height - 1);

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                int pixel = bitmap.getPixel(i, j);
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                if (r == BOTTLE_TARGET && g == BOTTLE_TARGET && b == BOTTLE_TARGET) {
                    boolean[][] vMap = new boolean[width][height];
                    Queue<int[]> queue = new ArrayDeque<>();
                    int[] pos = {i, j};
                    queue.add(pos);
                    int maxX = Integer.MIN_VALUE;
                    int minX = Integer.MAX_VALUE;
                    int maxY = Integer.MIN_VALUE;
                    int minY = Integer.MAX_VALUE;
                    while (!queue.isEmpty()) {
                        pos = queue.poll();
                        int x = pos[0];
                        int y = pos[1];
                        if (x < x1 || x > x2 || y < y1 || y > y2 || vMap[x][y]) {
                            continue;
                        }
                        vMap[x][y] = true;
                        pixel = bitmap.getPixel(x, y);
                        r = (pixel & 0xff0000) >> 16;
                        g = (pixel & 0xff00) >> 8;
                        b = (pixel & 0xff);
                        if (r == BOTTLE_TARGET && g == BOTTLE_TARGET && b == BOTTLE_TARGET) {
                            maxX = Ints.max(maxX, x);
                            minX = Ints.min(minX, x);
                            maxY = Ints.max(maxY, y);
                            minY = Ints.min(minY, y);
                            queue.add(buildArray(x - 1, y));
                            queue.add(buildArray(x + 1, y));
                            queue.add(buildArray(x, y - 1));
                            queue.add(buildArray(x, y + 1));
                        }
                    }

                    System.out.println("whitePoint: " + maxX + ", " + minX + ", " + maxY + ", " + minY);
                    if (maxX - minX <= 45 && maxX - minX >= 35 && maxY - minY <= 30 && maxY - minY >= 20) {
                        int[] ret = {(minX + maxX) / 2, (minY + maxY) / 2};
                        return ret;
                    } else {
                        return null;
                    }

                }
            }
        }
        return null;
    }

    public static int[] buildArray(int i, int j) {
        int[] ret = {i, j};
        return ret;
    }

    public static boolean match(int r, int g, int b, int rt, int gt, int bt, int t) {
        return r > rt - t && r < rt + t && g > gt - t && g < gt + t && b > bt - t && b < bt + t;
    }
}
