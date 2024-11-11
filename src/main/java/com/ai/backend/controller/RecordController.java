package com.ai.backend.controller;

import com.ai.backend.entity.Record;
import com.ai.backend.service.EvaluationService;
import com.ai.backend.util.EvaluationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class RecordController {


    @Autowired
    private EvaluationService evaluationService;

    @RequestMapping(path = "/getRecordTotal", method = RequestMethod.GET)
    @ResponseBody
    public int getTotal(){
        return evaluationService.getTotal();
    }

    @RequestMapping(path = "/getRecord", method = RequestMethod.GET)
    @ResponseBody
    public List<Record> getRecord(@RequestParam int page){

        return evaluationService.findRecord(page);
    }

    @RequestMapping(path = "/getRecordById", method = RequestMethod.GET)
    @ResponseBody
    public Record getRecordById(@RequestParam int id){
        return evaluationService.findRecordById(id);
    }

    @RequestMapping(path = "/searchRecord", method = RequestMethod.GET)
    @ResponseBody
    public List<Record> searchRecord(@RequestParam String keywords){
        return evaluationService.searchRecord(keywords);
    }

    @RequestMapping(path = "/deleteRecord", method = RequestMethod.GET)
    @ResponseBody
    public int deleteRecord(@RequestParam int id){
        return evaluationService.deleteRecord(id);
    }
}
