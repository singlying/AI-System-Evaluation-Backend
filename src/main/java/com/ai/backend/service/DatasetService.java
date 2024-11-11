package com.ai.backend.service;

import com.ai.backend.dao.DatasetMapper;
import com.ai.backend.dao.ModelMapper;
import com.ai.backend.entity.Dataset;
import com.ai.backend.entity.Model;
import com.ai.backend.entity.User;
import com.ai.backend.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

@Service
public class DatasetService {
    @Autowired
    private DatasetMapper datasetMapper;

    @Autowired
    private HostHolder hostHolder;

    public List<Dataset> findDataset(int page){
        User user = hostHolder.getUser();
        int offset = 12 * (page - 1);
        return datasetMapper.selectDataset(user.getId(), offset);
    }

    public int getTotal(){
        User user = hostHolder.getUser();
        return datasetMapper.countDataset(user.getId());
    }


    public Dataset findDatasetById(int id){return datasetMapper.selectDatasetById(id);}

    public int deleteDataset(int id){
        return datasetMapper.deleteDataset(id);
    }





    public List<Dataset> searchDataset(String keyword){
        User user = hostHolder.getUser();
        return datasetMapper.searchDataset(keyword, user.getId());}

    public int insertDataset(MultipartFile dataFile, MultipartFile labelFile, Dataset dataset) throws IOException {
        User user = hostHolder.getUser();
        dataset.setUserId(user.getId());
        datasetMapper.insertDataset(dataset);

        RestTemplate restTemplate = new RestTemplate();

        long chunkSize = 10 * 1024 * 1024; // 10MB
        int dataChunks = uploadFileInChunks(restTemplate, dataFile, "dataFile", chunkSize, dataset.getId());
        int labelChunks = uploadFileInChunks(restTemplate, labelFile, "labelFile", chunkSize, dataset.getId());

        mergeChunks(restTemplate, dataFile.getOriginalFilename(), dataChunks, "dataFile", dataset.getId());
        mergeChunks(restTemplate, labelFile.getOriginalFilename(), labelChunks, "labelFile", dataset.getId());

        try {
            Map<String, Object> validationResult = validateFiles(restTemplate, dataset.getId());
            List<Double> mean = (List<Double>) validationResult.get("mean");
            List<Double> std = (List<Double>) validationResult.get("std");
            dataset.setMean1(mean.get(0));
            dataset.setMean2(mean.get(1));
            dataset.setMean3(mean.get(2));
            dataset.setStd1(std.get(0));
            dataset.setStd2(std.get(1));
            dataset.setStd3(std.get(2));
            datasetMapper.updateDataset(dataset);
        } catch (HttpClientErrorException e) {
            // 处理来自 FastAPI 的错误响应
            String responseBody = e.getResponseBodyAsString();
            datasetMapper.deleteDataset(dataset.getId());
            throw new RuntimeException("服务器响应错误: " + responseBody, e);
        }

        return 1;
    }

    private int uploadFileInChunks(RestTemplate restTemplate, MultipartFile file, String paramName, long chunkSize, int id) throws IOException {
        String uploadUrl = "http://127.0.0.1:8000/upload_chunk/";
        byte[] buffer = new byte[(int) chunkSize];
        int bytesRead;
        int chunkNumber = 0;

        try (InputStream inputStream = file.getInputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                Path chunkFile = Files.createTempFile("chunk", String.valueOf(chunkNumber));
                try (OutputStream os = Files.newOutputStream(chunkFile, StandardOpenOption.WRITE)) {
                    os.write(buffer, 0, bytesRead);
                }
                uploadChunk(restTemplate, uploadUrl, chunkFile.toFile(), chunkNumber, file.getOriginalFilename(), id);
                Files.delete(chunkFile);
                chunkNumber++;
            }
        }

        return chunkNumber;
    }

    private void uploadChunk(RestTemplate restTemplate, String url, File chunkFile, int chunkNumber, String filename, int id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        FileSystemResource fileResource = new FileSystemResource(chunkFile);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("chunk_number", chunkNumber);
        body.add("filename", filename);
        body.add("id", id);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        System.out.println(response.getBody());
    }

    private void mergeChunks(RestTemplate restTemplate, String filename, int totalChunks, String filetype, int id) {
        String mergeUrl = "http://127.0.0.1:8000/merge_chunks/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("filename", filename);
        body.add("total_chunks", String.valueOf(totalChunks));
        body.add("filetype", filetype);
        body.add("id", String.valueOf(id));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(mergeUrl, requestEntity, String.class);
    }

    private Map<String, Object> validateFiles(RestTemplate restTemplate, int id) {
        String validateUrl = "http://127.0.0.1:8000/validate_dataset";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("id", String.valueOf(id));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(validateUrl, requestEntity, Map.class);
        Map<String, Object> result = response.getBody();
        if (!"文件验证成功！".equals(result.get("message"))) {
            throw new RuntimeException("文件验证失败");
        }

        return result;
    }
}
