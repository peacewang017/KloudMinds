package com.example.k8s_springboot.mapper;

import com.example.k8s_springboot.domain.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-07-09
 */
public interface UsersMapper extends BaseMapper<User> {

    @Select("SELECT * FROM k8sminio.users WHERE username = #{username}")
    User selectByUsername(String username);

}
