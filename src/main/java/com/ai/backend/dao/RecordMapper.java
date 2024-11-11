package com.ai.backend.dao;

import com.ai.backend.entity.Record;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecordMapper {


    @Insert({
            "insert into record(status, time, model_id, dataset_id, content, type, mission_type, user_id)",
            "values(#{status}, #{time}, #{modelId}, #{datasetId}, #{content}, #{type}, #{missionType}, #{userId})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRecord(Record record);

    @Select({
            "select count(*)" +
                    " from record " +
                    " where user_id = #{userId}"
    })
    int countRecord(int userId);

    @Update({
            "update record set status = #{status}, content = #{content} where id = #{id}",
    })
    int updateRecord(int id, int status, String content);

    @Select({
            "select r.id, r.status, r.time, r.model_id, m.name as model_name, r.dataset_id, d.name as dataset_name, r.content, r.type, r.mission_type, r.user_id\n" +
                    "from eval.record r\n" +
                    "join eval.dataset d on r.dataset_id = d.id\n" +
                    "join eval.model m on r.model_id = m.id\n" +
                    "where r.user_id = #{userId} order by r.id desc " +
                    "limit 12 offset #{offset} "
    })
    List<Record> selectRecord(int userId, int offset);

    @Select({
            "select r.id, r.status, r.time, r.model_id, m.name as model_name, r.dataset_id, d.name as dataset_name, r.content, r.type, r.mission_type, r.user_id\n" +
                    "from eval.record r\n" +
                    "join eval.dataset d on r.dataset_id = d.id\n" +
                    "join eval.model m on r.model_id = m.id\n" +
                    "where r.id = #{id};"
    })
    Record selectRecordById(int id);

    @Delete({
            "delete from record where id = #{id}"
    })
    int deleteRecord(int id);

    @Select({
            "select r.id, r.status, r.time, r.model_id, m.name as model_name, r.dataset_id, d.name as dataset_name, r.content, r.type, r.mission_type, r.user_id\n" +
                    "from eval.record r\n" +
                    "join eval.dataset d on r.dataset_id = d.id\n" +
                    "join eval.model m on r.model_id = m.id\n" +
                    "where (d.name like #{keyword} or m.name like #{keyword}) and r.user_id = #{userId} order by r.id desc"
    })
    List<Record> searchRecord(String keyword, int userId);
}
