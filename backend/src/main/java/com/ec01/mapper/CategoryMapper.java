package com.ec01.mapper;

import com.ec01.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category selectById(@Param("id") Long id);

    @Select("""
            SELECT * FROM category
            WHERE parent_id IS NULL AND status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<Category> selectRootCategories();

    @Select("""
            SELECT * FROM category
            WHERE parent_id = #{parentId} AND status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    @Select("""
            SELECT id FROM category
            WHERE parent_id = #{parentId} AND status = 1
            ORDER BY sort_order ASC, id ASC
            """)
    List<Long> selectChildIds(@Param("parentId") Long parentId);

    @Select("""
            SELECT * FROM category
            ORDER BY IF(parent_id IS NULL, id, parent_id) ASC,
                     IF(parent_id IS NULL, 0, 1) ASC,
                     sort_order ASC,
                     id ASC
            """)
    List<Category> selectAllForAdmin();

    @Insert("""
            INSERT INTO category (name, parent_id, sort_order, status)
            VALUES (#{name}, #{parentId}, #{sortOrder}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("""
            UPDATE category
            SET name = #{name}, sort_order = #{sortOrder}, update_time = NOW()
            WHERE id = #{id}
            """)
    int update(Category category);

    @Update("""
            UPDATE category
            SET status = #{status}, update_time = NOW()
            WHERE id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") byte status);
}
