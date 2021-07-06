package com.blr19c.common.mybatisWrapper.wrapper.update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.blr19c.common.mybatisWrapper.SqlWrapperUtils;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * 合并
 *
 * @author blr
 */
public interface MergeWrapper extends SqlWrapper {

    /**
     * 合并
     *
     * @param modelClass 实体类class
     * @param modelList  合并的数据
     */
    default <T> int merge(Class<T> modelClass, Collection<T> modelList) {
        initMappedStatement(UpdateWrapper.UpdateMethod.instance, modelClass, Integer.class);
        TableInfo tableInfo = TableInfoHelper.getTableInfo(modelClass);
        String key = tableInfo.getKeyProperty();
        if (StringUtils.isBlank(key))
            throw new IllegalArgumentException("keyProperty does not exist");
        String keyMethod = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
        int count = 0;
        for (T o : modelList) {
            Object invoke;
            try {
                invoke = o.getClass().getMethod(keyMethod).invoke(o);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            T one = SqlWrapperUtils.one(modelClass, l -> Wrappers.<T>query().eq(tableInfo.getKeyColumn(), invoke).lambda());
            count += one == null ? SqlWrapperUtils.insert(o) : SqlWrapperUtils.update(modelClass, o);
        }
        return count;
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        throw new IllegalStateException("The sql generation method should not be used here!");
    }
}
