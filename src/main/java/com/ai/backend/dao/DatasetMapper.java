package com.ai.backend.dao;

import com.ai.backend.entity.Dataset;
import com.ai.backend.entity.Model;
import com.ai.backend.entity.Record;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface DatasetMapper {

    @Select({
            "select count(*)" +
                    " from dataset " +
                    " where user_id = #{userId}"
    })
    int countDataset(int userId);

    @Select({
            "select * from dataset" +
                    " where user_id = #{userId} order by id desc" +
                    " limit 12 offset #{offset}"
    })
    List<Dataset> selectDataset(int userId, int offset);
    @Select({
            "select * from dataset where id = #{id}"
    })
    Dataset selectDatasetById(int id);

    @Insert({
            "insert into dataset(name, description, user_id, type) ",
            "values(#{name}, #{description}, #{userId}, #{type})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDataset(Dataset dataset);

    @Delete({
            "delete from dataset WHERE id = #{id}"
    })
    int deleteDataset(int id);

    @Select({
            "select * from dataset" +
                    " where (user_id = #{userId}) and (name like #{keywords} or description like #{keywords}) " +
                    "order by id desc"
    })
    List<Dataset> searchDataset(String keywords, int userId);

    @Update({
            " update dataset set mean1 = #{mean1},  mean2 = #{mean2}, mean3 = #{mean3},  std1 = #{std1}, std2 = #{std2}, std3 = #{std3} " +
                    " where id = #{id}"
    })
    int updateDataset(Dataset dataset);


}
