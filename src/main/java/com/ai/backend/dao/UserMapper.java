package com.ai.backend.dao;

import com.ai.backend.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select({
            " select  id, username, password, salt, email, status, activation_code, create_time" +
                    "        from user" +
                    "        where id = #{id}"
    })
    User selectById(int id);

    @Select({
            " select  id, username, password, salt, email, status, activation_code, create_time" +
                    "        from user" +
                    "        where username = #{name}"
    })
    User selectByName(String name);
    @Select({
            " select  id, username, password, salt, email, status, activation_code, create_time" +
                    "        from user" +
                    "        where email = #{email}"
    })
    User selectByEmail(String email);
    @Insert({
            "insert into user (username, password, salt, email, status, activation_code, create_time)" +
                    "        values(#{username}, #{password}, #{salt}, #{email}, #{status}, #{activationCode}, #{createTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update({
            " update user set status = #{status} where id = #{id}"
    })
    int updateStatus(int id, int status);

    @Update({
            "update user set password = #{password} where id = #{id}"
    })
    int updatePassword(int id, String password);
}
