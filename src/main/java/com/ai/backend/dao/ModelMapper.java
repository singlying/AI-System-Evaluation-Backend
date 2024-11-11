package com.ai.backend.dao;


import com.ai.backend.entity.Model;
import com.ai.backend.entity.Record;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ModelMapper {

    @Select({
            "select count(*)" +
                    " from model " +
                    " where user_id = #{userId}"
    })
    int countModel(int userId);

    @Select({
            "select * from model order by id desc limit 12 offset #{offset}"
    })
    List<Model> selectModel(int userId, int offset);

    @Select({
            "select * from model where id = #{id}"
    })
    Model selectModelById(int id);

    @Insert({"insert into model(name, description, user_id, mission_type) ",
            "values(#{name}, #{description}, #{userId}, #{missionType})"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertModel(Model model);

    @Delete({
            "delete from model WHERE id = #{id}"
    })
    int deleteModel(int id);

    @Select({
            "select * from model " +
                    "where (user_id = #{userId}) and (name like #{keywords} or description like #{keywords})" +
                    " order by id desc"
    })
    List<Model> searchModel(String keywords, int userId);

    @Update({
            " update model set channel = #{channel},  width = #{width}, height = #{height} " +
                    " where id = #{id}"
    })

    int updateModel(Model model);
}
