package com.enterprise.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.doc.entity.DirectoryPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DirectoryPermissionMapper extends BaseMapper<DirectoryPermission> {

    @Select("SELECT dp.* FROM doc_directory_permission dp " +
            "WHERE dp.target_type = #{targetType} AND dp.target_id = #{targetId} " +
            "AND dp.deleted = 0")
    List<DirectoryPermission> selectByTarget(
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);

    @Select("SELECT dp.* FROM doc_directory_permission dp " +
            "WHERE ((dp.subject_type = 0 AND dp.subject_id = #{userId}) " +
            "OR (dp.subject_type = 1 AND dp.subject_id IN " +
            "(SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId} AND ur.deleted = 0))) " +
            "AND dp.target_type = #{targetType} AND dp.target_id = #{targetId} " +
            "AND dp.deleted = 0")
    List<DirectoryPermission> selectByUserAndTarget(
            @Param("userId") Long userId,
            @Param("targetType") Integer targetType,
            @Param("targetId") Long targetId);
}
