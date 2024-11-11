package com.ai.backend.controller;

import com.ai.backend.entity.Model;
import com.ai.backend.entity.User;
import com.ai.backend.service.EvaluationService;
import com.ai.backend.util.EvaluationUtil;
import com.ai.backend.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class EvaluationController {



    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/accuracy", method = RequestMethod.POST)
    @ResponseBody
    public String evalAccuracy(@RequestBody Map<String, Object> requestBody){
        User user = hostHolder.getUser();
        Map<String, Object> form = (Map<String, Object>) requestBody.get("form");

        int modelId = (int) requestBody.get("modelId");
        int datasetId = (int) requestBody.get("datasetId");
        int missionType = (int) requestBody.get("missionType");


        evaluationService.getAccuracyReport(modelId, datasetId, missionType, form, user.getId());


        return EvaluationUtil.getJSONString(0, "successful");
    }

    @RequestMapping(path = "/robustness", method = RequestMethod.POST)
    @ResponseBody
    public String evalRobustness(@RequestBody Map<String, Object> requestBody){
        User user = hostHolder.getUser();

        Map<String, Object> form = (Map<String, Object>) requestBody.get("form");
        Map<String, Object> attackForm = (Map<String, Object>) requestBody.get("attackForm");
        int missionType = (int) requestBody.get("missionType");

        int modelId = (int) requestBody.get("modelId");
        int datasetId = (int) requestBody.get("datasetId");


        evaluationService.getRobustnessReport(modelId, datasetId, missionType, form, attackForm, user.getId());


        return EvaluationUtil.getJSONString(0, "successful");
    }

    @RequestMapping(path = "/fairness", method = RequestMethod.POST)
    @ResponseBody
    public String evalFairness(@RequestBody Map<String, Object> requestBody){
        User user = hostHolder.getUser();
        Map<String, Object> form = (Map<String, Object>) requestBody.get("form");
        Map<String, Object> featureForm = (Map<String, Object>) requestBody.get("featureForm");
        int missionType = (int) requestBody.get("missionType");

        int modelId = (int) requestBody.get("modelId");
        int datasetId = (int) requestBody.get("datasetId");


        evaluationService.getFairnessReport(modelId, datasetId, missionType, form, featureForm, user.getId());


        return EvaluationUtil.getJSONString(0, "successful");
    }


}
