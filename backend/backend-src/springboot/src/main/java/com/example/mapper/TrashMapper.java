package com.example.mapper;

import com.example.entity.Trash;
import java.util.List;

/**
 * 操作trash相关数据接口
 */
public interface TrashMapper {

    /**
     * 新增
     */
    int insert(Trash trash);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Trash trash);

    /**
     * 根据ID查询
     */
    Trash selectById(Integer id);

    /**
     * 查询所有
     */
    List<Trash> selectAll(Trash trash);

    void deleteByFileId(Integer fileId);
}