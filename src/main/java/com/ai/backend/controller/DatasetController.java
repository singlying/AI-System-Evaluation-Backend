package com.ai.backend.controller;

import com.ai.backend.entity.Dataset;
import com.ai.backend.entity.Model;
import com.ai.backend.service.DatasetService;
import com.ai.backend.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class DatasetController {
    @Autowired
    private DatasetService datasetService;

    @RequestMapping(path = "/getDatasetTotal", method = RequestMethod.GET)
    @ResponseBody
    public int getTotal(){
        return datasetService.getTotal();
    }

    @RequestMapping(path = "/getDatasetById", method = RequestMethod.GET)
    @ResponseBody
    public Dataset getDatasetById(@RequestParam int id){
        return datasetService.findDatasetById(id);
    }

    @RequestMapping(path = "/getDataset", method = RequestMethod.GET)
    @ResponseBody
    public List<Dataset> getDataset(@RequestParam int page){
        return datasetService.findDataset(page);
    }

    @RequestMapping(path = "/searchDataset", method = RequestMethod.GET)
    @ResponseBody
    public List<Dataset> searchModel(@RequestParam String keyword){
        return datasetService.searchDataset(keyword);
    }

    @RequestMapping(path = "/deleteDataset", method = RequestMethod.GET)
    @ResponseBody
    public int deleteDataset(@RequestParam int id){
        return datasetService.deleteDataset(id);
    }

    @RequestMapping(path = "/updateDataset", method = RequestMethod.POST)
    @ResponseBody
    public int updateDataset(@RequestParam("id") int id,
                             @RequestParam("name") String name,
                             @RequestParam("description") String description,
                             @RequestParam("missionType") int type){
        return 1;
    }

    @RequestMapping(path = "/addDataset", method = RequestMethod.POST)
    @ResponseBody
    public String addDataset(@RequestPart("dataFile") MultipartFile dataFile,
                          @RequestPart("labelFile") MultipartFile labelFile,
                          @RequestParam("name") String name,
                          @RequestParam("description") String description,
                          @RequestParam("type") int type) throws IOException {
        Dataset dataset = new Dataset();
        dataset.setName(name);
        dataset.setType(type);
        dataset.setDescription(description);
        try {
            datasetService.insertDataset(dataFile, labelFile, dataset);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        return "成功！";
    }
}

