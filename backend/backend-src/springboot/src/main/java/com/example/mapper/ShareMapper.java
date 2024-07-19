package com.example.mapper;

import com.example.entity.Share;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 操作share相关数据接口
 */
public interface ShareMapper {

    /**
     * 新增
     */
    int insert(Share share);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Share share);

    /**
     * 根据ID查询
     */
    Share selectById(Integer id);

    /**
     * 查询所有
     */
    List<Share> selectAll(Share share);

    @Update("update share set count = count + 1 where id = #{id}")
    void updateCount(Integer id);

}