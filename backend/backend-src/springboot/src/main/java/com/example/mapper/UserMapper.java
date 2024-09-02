package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作user相关数据接口
*/
public interface UserMapper {

    /**
      * 新增
    */
    int insert(User user);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(User user);

    /**
      * 根据ID查询
    */
    User selectById(Integer id);

    /**
      * 查询所有
    */
    List<User> selectAll(User user);

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);

    /**
     * 根据用户id查询用户当前容量和最大容量
     */
    @Select("select used_capacity as usedCapacity, max_capacity as maxCapacity from user where id = #{id}")
    User selectCapacityById(Integer id);
}