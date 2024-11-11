package com.ai.backend.service;

import com.ai.backend.dao.ModelMapper;
import com.ai.backend.entity.Model;
import com.ai.backend.entity.User;
import com.ai.backend.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private HostHolder hostHolder;

    public int getTotal(){
        User user = hostHolder.getUser();
        return modelMapper.countModel(user.getId());
    }

    public List<Model> findModel(int page) {
        int offset = 12 * (page - 1);
        User user = hostHolder.getUser();
        return modelMapper.selectModel(user.getId(), offset);
    }

    public Model findModelById(int id) {
        return modelMapper.selectModelById(id);
    }

    public List<Model> searchModel(String keyword) {
        keyword = "%" + keyword + "%";
        User user = hostHolder.getUser();
        return modelMapper.searchModel(keyword, user.getId());
    }

    public int deleteModel(int id) {
        return modelMapper.deleteModel(id);
    }

    public int updateModel(Model model) {
        return modelMapper.updateModel(model);
    }
/*
    public int insertModel(MultipartFile def, MultipartFile weight, Model model) throws IOException {
        User user = hostHolder.getUser();
        model.setUserId(user.getId());
        modelMapper.insertModel(model);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        HttpHeaders defFileHeaders = new HttpHeaders();
        defFileHeaders.setContentDispositionFormData("defFile", def.getOriginalFilename());
        HttpEntity<InputStreamResource> defFileEntity = new HttpEntity<>(new InputStreamResource(def.getInputStream()), defFileHeaders);

        HttpHeaders weightFileHeaders = new HttpHeaders();
        weightFileHeaders.setContentDispositionFormData("weightFile", weight.getOriginalFilename());
        HttpEntity<InputStreamResource> weightFileEntity = new HttpEntity<>(new InputStreamResource(weight.getInputStream()), weightFileHeaders);

        body.add("defFile", defFileEntity);
        body.add("weightFile", weightFileEntity);
        body.add("id", new HttpEntity<>(String.valueOf(model.getId())));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://127.0.0.1:8000/uploadModel",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            // 捕获来自 FastAPI 的错误响应，并处理
            modelMapper.deleteModel(model.getId());
            throw new RuntimeException(e.getResponseBodyAsString());

        }

        return 1;
    }

*/
public int insertModel(MultipartFile def, MultipartFile weight, Model model) throws IOException {
    User user = hostHolder.getUser();
    model.setUserId(user.getId());
    modelMapper.insertModel(model);

    RestTemplate restTemplate = new RestTemplate();

    long chunkSize = 10 * 1024 * 1024; // 10MB
    int defChunks = uploadFileInChunks(restTemplate, def, "defFile", chunkSize, model.getId());
    int weightChunks = uploadFileInChunks(restTemplate, weight, "weightFile", chunkSize, model.getId());

    mergeChunks(restTemplate, def.getOriginalFilename(), defChunks, "defFile", model.getId());
    mergeChunks(restTemplate, weight.getOriginalFilename(), weightChunks, "weightFile", model.getId());

    try {
        Map<String, Integer> inputSize = validateFiles(restTemplate, model.getId());
        int channel = inputSize.get("channels");
        int width = inputSize.get("width");
        int height = inputSize.get("height");
        model.setChannel(channel);
        model.setHeight(height);
        model.setWidth(width);
        modelMapper.updateModel(model);

    } catch (HttpClientErrorException e) {
        // 处理来自 FastAPI 的错误响应
        String responseBody = e.getResponseBodyAsString();
        modelMapper.deleteModel(model.getId());
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

    private Map<String, Integer> validateFiles(RestTemplate restTemplate, int id) {
        String validateUrl = "http://127.0.0.1:8000/validate_files/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("id", String.valueOf(id));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(validateUrl, requestEntity, Map.class);
        return (Map<String, Integer>) response.getBody().get("input_size");

    }
}










