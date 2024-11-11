package com.ai.backend.controller;


import com.ai.backend.entity.Model;
import com.ai.backend.entity.Record;
import com.ai.backend.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class ModelController {
    @Autowired
    private ModelService modelService;

    @RequestMapping(path = "/getModelTotal", method = RequestMethod.GET)
    @ResponseBody
    public int getTotal(){
        return modelService.getTotal();
    }

    @RequestMapping(path = "/getModel", method = RequestMethod.GET)
    @ResponseBody
    public List<Model> getModel(@RequestParam int page){
        return modelService.findModel(page);
    }

    @RequestMapping(path = "/searchModel", method = RequestMethod.GET)
    @ResponseBody
    public List<Model> searchModel(@RequestParam String keyword){
        return modelService.searchModel(keyword);
    }

    @RequestMapping(path = "/deleteModel", method = RequestMethod.GET)
    @ResponseBody
    public int deleteModel(@RequestParam int id){
        return modelService.deleteModel(id);
    }

    @RequestMapping(path = "/updateModel", method = RequestMethod.POST)
    @ResponseBody
    public int updateModel(@RequestParam("id") int id,
                           @RequestParam("name") String name,
                           @RequestParam("description") String description,
                           @RequestParam("missionType") int missionType){
        return 1;
    }

    @RequestMapping(path = "/addModel", method = RequestMethod.POST)
    @ResponseBody
    public String addModel(@RequestPart("def") MultipartFile def,
                        @RequestPart("weight") MultipartFile weight,
                        @RequestParam("name") String name,
                        @RequestParam("description") String description,
                        @RequestParam("missionType") int missionType) throws IOException {
        Model model = new Model();
        model.setName(name);
        model.setDescription(description);
        model.setMissionType(missionType);
        try {
            modelService.insertModel(def, weight, model);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
        return "成功！";
    }




}
