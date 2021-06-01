package com.blr19c.common.code;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * 提供正确顺序的BigDecimal运算
 *
 * @author blr
 */
public class MathOperationUtil {
    public static final String ADDITION = "+";
    public static final String SUBTRACTION = "-";
    public static final String MULTIPLICATION = "*";
    public static final String DIVISION = "/";
    public static final String LEFT_PARENT = "(";
    public static final String RIGHT_PARENT = ")";

    /**
     * 字符拼接顺序运算 (顺序指输入顺序并非运算顺序)
     *
     * @param roundingMode 除数处理
     * @param maths        运算字符
     * @see MathOperationUtil#mathNormal(RoundingMode, Object...)
     */
    public static BigDecimal math(RoundingMode roundingMode, Object... maths) {
        return mathToArray(roundingMode, getArray(maths));
    }

    /**
     * 字符拼接运算按照正常运算顺序进行运算 () -> /* -> +-
     * 如果出现-9*-9*-9此类运算使用mathNormal("-9", "*", "-9", "*", "-9")
     * 而不是 mathNormal(generateArray("-9*-9*-9"))
     *
     * @param roundingMode 除数处理
     * @param maths        运算字符
     */
    public static BigDecimal mathNormal(RoundingMode roundingMode, Object... maths) {
        return new MathCalculation(roundingMode, getArray(maths)).mathNormal();
    }

    public static String checkMath(Object o) {
        String value;
        if (o instanceof Number) {
            value = new DecimalFormat("0.0000000000").format(((Number) o).doubleValue());
        } else {
            value = String.valueOf(o).trim();
        }
        return "null".equals(value) ? new DecimalFormat("0.0000000000").format(0) : value;
    }

    public static BigDecimal getBigDecimal(Object o) {
        return new BigDecimal(checkMath(o));
    }

    public static Object[] getArray(Object... maths) {
        Object[] objects = new Object[maths.length];
        System.arraycopy(maths, 0, objects, 0, objects.length);
        return objects;
    }

    public static BigDecimal mathToArray(RoundingMode roundingMode, Object[] maths) {
        BigDecimal re = null;
        for (int i = 0; i < maths.length; i++) {
            if (i == maths.length - 1) {
                break;
            }
            if (re == null) {
                try {
                    re = getBigDecimal(maths[i]);
                } catch (NumberFormatException ignored) {
                }
                continue;
            }
            String asmd = String.valueOf(maths[i]);
            String math = checkMath(maths[i + 1]);
            if (StringUtils.isBlank(asmd)) {
                continue;
            }
            re = asmd(asmd, math, re, roundingMode);
        }
        return re == null ? getBigDecimal(0) : re;
    }

    public static BigDecimal asmd(String asmd, String math, BigDecimal re, RoundingMode roundingMode) {
        switch (asmd.trim()) {
            case ADDITION:
                re = re.add(getBigDecimal(math));
                break;
            case SUBTRACTION:
                re = re.subtract(getBigDecimal(math));
                break;
            case MULTIPLICATION:
                re = re.multiply(getBigDecimal(math));
                break;
            case DIVISION:
                BigDecimal bigDecimal = getBigDecimal(math);
                if (checkZero(re)) {
                    re = getBigDecimal(BigDecimal.ZERO);
                } else {
                    re = getBigDecimal(re.divide(bigDecimal, roundingMode));
                }
                break;
        }
        return re;
    }

    public static boolean checkZero(BigDecimal bigDecimal) {
        return bigDecimal == null || BigDecimal.ZERO.equals(bigDecimal) || bigDecimal.doubleValue() == 0D;
    }

    public static String[] generateArray(String s) {
        String split = ",";
        s = s.replaceAll("\\+", getSymbolSplit(ADDITION, split));
        s = s.replaceAll("-", getSymbolSplit(SUBTRACTION, split));
        s = s.replaceAll("\\*", getSymbolSplit(MULTIPLICATION, split));
        s = s.replaceAll("/", getSymbolSplit(DIVISION, split));
        s = s.replaceAll("\\(", getSymbolSplit(LEFT_PARENT, split));
        s = s.replaceAll("\\)", getSymbolSplit(RIGHT_PARENT, split));
        s = s.replaceAll("\\s", "");
        s = s.replaceAll(split + split, split);
        if (s.startsWith(split)) {
            s = s.substring(1);
        }
        return s.split(split);
    }

    public static String getSymbolSplit(String symbol, String split) {
        return split + symbol + split;
    }

    public static boolean checkSymbol(String s) {
        return checkAs(s) || checkMd(s);
    }

    public static boolean checkSymbolCBracket(String s) {
        return checkSymbol(s) || checkBracket(s);
    }

    public static boolean checkAs(String s) {
        switch (s) {
            case ADDITION:
            case SUBTRACTION:
                return true;
        }
        return false;
    }

    public static boolean checkMd(String s) {
        switch (s) {
            case MULTIPLICATION:
            case DIVISION:
                return true;
        }
        return false;
    }

    public static boolean checkBracket(String s) {
        switch (s) {
            case LEFT_PARENT:
            case RIGHT_PARENT:
                return true;
        }
        return false;
    }

    public static class MathCalculation {
        private final RoundingMode roundingMode;
        private Object[] maths;

        public MathCalculation(RoundingMode roundingMode, Object[] maths) {
            this.roundingMode = roundingMode;
            this.maths = maths;
        }

        public static boolean checkBracket(Object[] objects) {
            return Arrays.toString(objects).contains(LEFT_PARENT);
        }

        public BigDecimal mathNormal() {
            bracket();
            return getBigDecimalToMaths(maths);
        }

        /**
         * * / > + -
         */
        private BigDecimal getSpecial(Object[] maths) {
            ArrayList<BigDecimal> arrayList = new ArrayList<>();
            Integer symbol = null;
            int mathArraySize = 0;
            // 乘除标记索引
            boolean[] mathArray = new boolean[maths.length];
            for (int i = 0; i < maths.length; i++) {
                String math = checkMath(maths[i]);
                switch (math) {
                    case MULTIPLICATION:
                    case DIVISION:
                        mathArray[i] = true;
                        mathArray[i + 1] = true;
                        mathArraySize += 2;
                        BigDecimal re;
                        String m = checkMath(maths[i + 1]);
                        if (symbol == null || i - 2 > symbol) {// 不连续的 */
                            mathArray[i - 1] = true;
                            mathArraySize++;
                            re = getBigDecimal(maths[i - 1]);
                            if (i - 2 >= 0) {// 向前
                                String lastRe = checkMath(maths[i - 2]);
                                if (checkAs(lastRe) && (i - 3 < 0 || checkSymbol(checkMath(maths[i - 3])))) {
                                    re = getBigDecimal(lastRe + re.toString());
                                    mathArray[i - 2] = true;
                                }
                            }
                        } else {// 连续的 */
                            re = arrayList.remove(arrayList.size() - 1);
                        }
                        if (checkSymbol(math) && checkAs(m)) {// 向后
                            m += checkMath(maths[++i + 1]);// 向后增加i避免不必要的轮询
                            mathArray[i + 1] = true;
                        }
                        arrayList.add(asmd(math, m, re, roundingMode));
                        symbol = i;
                        break;
                }
            }
            Object[] objects = new Object[mathArray.length - mathArraySize];
            int objectsSize = 0;
            for (int i = 0; i < mathArray.length; i++) {// 得出只有 + -的objects
                if (!mathArray[i]) {
                    objects[objectsSize++] = maths[i];
                }
            }
            if (objectsSize == 0) {// 全部的连续 * /
                return arrayList.isEmpty() ? getBigDecimal(0) : arrayList.get(0);
            }
            return mathToArray(roundingMode, reArray(arrayList, objects, objectsSize, mathArray));
        }

        /**
         * 防止出现单个Number运算
         */
        private BigDecimal getBigDecimalToMaths(Object[] objects) {
            BigDecimal bigDecimal;
            if (objects.length > 2) {
                bigDecimal = getSpecial(objects);
            } else {
                String s = "";
                for (Object object : objects) {
                    s = s.intern() + checkMath(object);
                }
                bigDecimal = getBigDecimal(checkMath(s));
            }
            return bigDecimal;
        }

        /**
         * 拼合结果集
         */
        private Object[] reArray(ArrayList<BigDecimal> arrayList, Object[] objects, int objectsSize, boolean[] mathArray) {
            Object[] objs = new Object[arrayList.size() + objectsSize];
            int listIndex = 0;
            int arrayIndex = 0;
            int objsIndex = 0;
            boolean continuous = false;
            for (boolean b : mathArray) {
                if (continuous && b) {// 连续的 * /
                    continue;
                }
                // 归位
                objs[objsIndex++] = b ? arrayList.get(listIndex++) : objects[arrayIndex++];
                continuous = b;
            }
            return objs;
        }

        /**
         * () 优先
         */
        private void bracket() {
            if (checkBracket(maths)) {
                Integer bracketsIndex = null;
                for (int i = 0; i < maths.length; i++) {
                    String math = checkMath(maths[i]);
                    switch (math) {
                        case LEFT_PARENT:
                            bracketsIndex = i;
                            break;
                        case RIGHT_PARENT:
                            Objects.requireNonNull(bracketsIndex, "缺失括号");
                            Object[] objects = new Object[i - bracketsIndex - 1];
                            int bracketsIndex2 = bracketsIndex;
                            ArrayList<Object> arrayList = new ArrayList<>(Arrays.asList(maths));
                            for (int j = 0; j < objects.length; j++) {// 拿到()里的数据
                                objects[j] = maths[bracketsIndex2++ + 1];
                                arrayList.remove(bracketsIndex.intValue());
                            }
                            arrayList.remove(bracketsIndex.intValue());// 删掉( )
                            arrayList.remove(bracketsIndex.intValue());
                            arrayList.add(bracketsIndex, getBigDecimalToMaths(objects));
                            maths = arrayList.toArray();
                            bracket();// 有括号继续寻找括号
                            return;
                    }
                }
            }
        }
    }
}
