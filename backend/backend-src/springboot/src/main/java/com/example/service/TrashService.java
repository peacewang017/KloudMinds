package com.example.service;

import com.example.entity.Trash;
import com.example.mapper.TrashMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 垃圾箱信息表业务处理
 **/
@Service
public class TrashService {

    @Resource
    private TrashMapper trashMapper;

    /**
     * 新增
     */
    public void add(Trash trash) {
        trashMapper.insert(trash);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        trashMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            trashMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Trash trash) {
        trashMapper.updateById(trash);
    }

    /**
     * 根据ID查询
     */
    public Trash selectById(Integer id) {
        return trashMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Trash> selectAll(Trash trash) {
        return trashMapper.selectAll(trash);
    }

    /**
     * 分页查询
     */
    public PageInfo<Trash> selectPage(Trash trash, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Trash> list = trashMapper.selectAll(trash);
        return PageInfo.of(list);
    }

    public void deleteByFileId(Integer fileId) {
        trashMapper.deleteByFileId(fileId);
    }
}