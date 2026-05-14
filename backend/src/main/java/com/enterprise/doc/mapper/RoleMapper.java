package com.enterprise.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.doc.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.deleted = 0 AND r.deleted = 0")
    List<Role> selectRolesByUserId(@Param("userId") Long userId);
}
