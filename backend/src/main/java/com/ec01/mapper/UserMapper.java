package com.ec01.mapper;

import com.ec01.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(@Param("id") Long id);

    @Insert("""
    INSERT INTO users (
        username,
        password,
        nickname,
        email,
        phone,
        avatar_url,
        status,
        role
    )
    VALUES (
        #{username},
        #{password},
        #{nickname},
        #{email},
        #{phone},
        #{avatarUrl},
        #{status},
        #{role}
    )
""")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(@Param("username") String username);

    @Update("""
    UPDATE users
    SET nickname = #{nickname},
        email = #{email},
        phone = #{phone},
        avatar_url = #{avatarUrl},
        update_time = NOW()
    WHERE id = #{id}
""")
    int updateProfile(User user);

    @Update("""
    UPDATE users
    SET password = #{password},
        update_time = NOW()
    WHERE id = #{id}
""")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Select("""
    <script>
    SELECT *
    FROM users
    WHERE 1 = 1
    <if test="keyword != null and keyword != ''">
        AND (
            username LIKE CONCAT('%', #{keyword}, '%')
            OR nickname LIKE CONCAT('%', #{keyword}, '%')
            OR phone LIKE CONCAT('%', #{keyword}, '%')
        )
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    ORDER BY create_time DESC, id DESC
    LIMIT #{offset}, #{size}
    </script>
    """)
    List<User> selectAdminPage(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("offset") long offset,
            @Param("size") Integer size
    );

    @Select("""
    <script>
    SELECT COUNT(*)
    FROM users
    WHERE 1 = 1
    <if test="keyword != null and keyword != ''">
        AND (
            username LIKE CONCAT('%', #{keyword}, '%')
            OR nickname LIKE CONCAT('%', #{keyword}, '%')
            OR phone LIKE CONCAT('%', #{keyword}, '%')
        )
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    </script>
    """)
    long countAdminUsers(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

    @Update("""
    UPDATE users
    SET status = #{status},
        update_time = NOW()
    WHERE id = #{id}
    """)
    int updateStatus(@Param("id") Long id, @Param("status") int status);

}
