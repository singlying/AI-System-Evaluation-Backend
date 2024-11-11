package com.ai.backend.service;

import com.ai.backend.dao.RecordMapper;
import com.ai.backend.entity.Model;
import com.ai.backend.entity.Record;
import com.ai.backend.entity.User;
import com.ai.backend.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableAsync
public class EvaluationService {



    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RecordMapper recordMapper;

    @Autowired
    private ModelService modelService;

    @Autowired
    private HostHolder hostHolder;

    @Async
    public void getAccuracyReport(int modelId, int datasetId, int missionType, Map<String, Object> form, int userId){
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        if (missionType == 1) {

            String pythonApiUrl = "http://127.0.0.1:8000/classification_report";
            int id = addRecord(modelId, datasetId, 1, 1, userId);
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }
        } else if (missionType == 2){
            String pythonApiUrl = "http://127.0.0.1:8000/retrieval_report";
            int id = addRecord(modelId, datasetId, 1, 2, userId);
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            // 将请求体和请求头包装在 HttpEntity 中
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }
        }
    }


    @Async
    public void getRobustnessReport(int modelId, int datasetId, int missionType, Map<String, Object> form, Map<String, Object> attackForm, int userId){
        HttpHeaders headers = new HttpHeaders();
        User user = hostHolder.getUser();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Model model = modelService.findModelById(modelId);
        attackForm.put("channels", model.getChannel());
        attackForm.put("width", model.getWidth());
        attackForm.put("height", model.getHeight());
        if (missionType == 1){
            String pythonApiUrl = "http://127.0.0.1:8000/robustness_report";
            int id = addRecord(modelId, datasetId, 2, 1, userId);
            // 创建请求体
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            input.put("attackForm", attackForm);
            // 将请求体和请求头包装在 HttpEntity 中
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }
        } else if (missionType == 2){
            String pythonApiUrl = "http://127.0.0.1:8000/retrieval_robustness_report";
            int id = addRecord(modelId, datasetId, 2, 2, userId);

            // 创建请求体
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            input.put("attackForm", attackForm);

            // 将请求体和请求头包装在 HttpEntity 中
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }
        }


    }

    public int addRecord(int modelId, int datesetId, int type, int missionType, int userId){
        Record record = new Record();

        record.setUserId(userId);
        record.setContent(null);
        record.setModelId(modelId);
        record.setDatasetId(datesetId);
        record.setType(type);
        record.setStatus(0);
        record.setTime(new Date());
        record.setMissionType(missionType);
        recordMapper.insertRecord(record);
        return record.getId();
    }

    public int deleteRecord(int id){
        return recordMapper.deleteRecord(id);
    }

    public int modifyRecord(int id, int status, String content){
        recordMapper.updateRecord(id, status, content);
        return 1;
    }

    public List<Record> findRecord(int page){
        int offset = 12 * (page - 1);
        User user = hostHolder.getUser();
        return recordMapper.selectRecord(user.getId(), offset);
    }
    public int getTotal(){
        User user = hostHolder.getUser();
        return recordMapper.countRecord(user.getId());
    }

    public List<Record> searchRecord(String keyword){
        User user = hostHolder.getUser();
        return recordMapper.searchRecord(keyword, user.getId());
    }

    public Record findRecordById(int id){
        return recordMapper.selectRecordById(id);
    }

    @Async
    public void getFairnessReport(int modelId, int datasetId, int missionType, Map<String, Object> form, Map<String, Object> featureForm, int userId) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        if (missionType == 1){
            String pythonApiUrl = "http://127.0.0.1:8000/fairness_report";
            int id = addRecord(modelId, datasetId, 3, 1, userId);
            // 创建请求体
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            input.put("featureForm", featureForm);

            // 将请求体和请求头包装在 HttpEntity 中
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }


        } else if (missionType == 2){
            String pythonApiUrl = "http://127.0.0.1:8000/retrieval_fairness_report";
            int id = addRecord(modelId, datasetId, 3, 2, userId);

            // 创建请求体
            Map<String, Object> input = new HashMap<>();
            input.put("modelId", modelId);
            input.put("datasetId", datasetId);
            input.put("form", form);
            input.put("featureForm", featureForm);

            // 将请求体和请求头包装在 HttpEntity 中
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(input, headers);
            // 使用 RestTemplate 的 postForObject 方法发出 POST 请求
            try {
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(pythonApiUrl, requestEntity, String.class);
                String responseBody = responseEntity.getBody();
                modifyRecord(id, 1, responseBody);
                System.out.println(responseBody);
            } catch (HttpServerErrorException e){
                modifyRecord(id, 2, "null");
            }
        }

    }


}
