package com.blr19c.common.collection;

/**
 * map分页方法
 */
public interface MapPageInterface extends MapGetInterface {

    /**
     * 获取pageNum
     */
    default int getPageNum() {
        return getIntValue(Page.getPageNumName());
    }

    /**
     * 获取pageSize
     */
    default int getPageSize() {
        return getIntValue(Page.getPageSizeName());
    }

    /**
     * 设置分页
     */
    class Page {
        static final String DEFAULT_PAGE_NUM_NAME = "pageNum";
        static final String DEFAULT_PAGE_SIZE_NAME = "pageSize";
        static String globalPageNumName = DEFAULT_PAGE_NUM_NAME;
        static String globalPageSizeName = DEFAULT_PAGE_SIZE_NAME;
        volatile static boolean modifyLock = false;

        static synchronized void setPage(String numName, String sizeName, boolean ml) {
            if (modifyLock) {
                throw new IllegalStateException("Has been set as non editable!");
            }
            globalPageNumName = numName;
            globalPageSizeName = sizeName;
            modifyLock = ml;
        }

        static String getPageNumName() {
            return globalPageNumName;
        }

        static String getPageSizeName() {
            return globalPageSizeName;
        }
    }
}
