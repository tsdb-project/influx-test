package edu.pitt.medschool.controller.analysis;

import edu.pitt.medschool.controller.analysis.vo.ColumnVO;
import edu.pitt.medschool.controller.analysis.vo.DownsampleEditResponse;
import edu.pitt.medschool.controller.analysis.vo.ElectrodeVO;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.service.AnalysisService;
import edu.pitt.medschool.service.ColumnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Isolachine
 */

@Controller
public class AnalysisController {

    @Value("${machine}")
    private String uuid;

    @Autowired
    ColumnService columnService;
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;
    @Autowired
    AnalysisService analysisService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("analysis/export")
    public Model exportPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "export");
        List<String> trends = columnService.selectAllMeasures();
        trends.add(0, "All");
        model.addAttribute("measures", trends);
        return model;
    }

    /**
     * Generate model object for analysis service
     */
    private Model analysisGenerateModel(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "builder");
        List<Downsample> downsamples = analysisService.selectAll();
        model.addAttribute("downsamples", downsamples);
        return model;
    }

    @RequestMapping("analysis/builder")
    public Model builderPage(Model model) {
        return analysisGenerateModel(model);
    }

    @RequestMapping("analysis/create")
    public Model createPage(Model model) {
        return analysisGenerateModel(model);
    }

    @RequestMapping(value = {"analysis/edit/{id}", "analysis/edit"}, method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable Optional<Integer> id, ModelAndView modelAndView) {
        modelAndView.addObject("nav", "analysis");
        modelAndView.addObject("subnav", "builder");
        modelAndView.setViewName("analysis/edit");
        if (id.isPresent()) {
            modelAndView.addObject("edit", true);
            Downsample downsample = analysisService.selectByPrimaryKey(id.get());
            DownsampleEditResponse downsampleEditResponse = new DownsampleEditResponse(downsample);
            modelAndView.addObject("query", downsampleEditResponse);
        } else {
            modelAndView.addObject("edit", false);
            List<Downsample> downsamples = analysisService.selectAll();
            modelAndView.addObject("downsamples", downsamples);
        }
        modelAndView.addObject("measures", columnService.selectAllMeasures());
        return modelAndView;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.GET)
    @ResponseBody
    public RestfulResponse allQuery(Model model) {
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(analysisService.selectAll());
        return response;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse insert(@RequestBody(required = true) Downsample downsample) throws Exception {
        RestfulResponse response;
        if (analysisService.insertDownsample(downsample) == 1) {
            response = new RestfulResponse(1, "success");
            response.setData(downsample);
        } else {
            response = new RestfulResponse(0, "insert failed");
        }
        return response;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> update(@RequestBody(required = true) Downsample downsample) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.updateByPrimaryKey(downsample) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "update failed"));
        }
        map.put("data", analysisService.selectByPrimaryKey(downsample.getId()));
        return map;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> delete(@RequestBody(required = true) Downsample downsample) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.deleteByPrimaryKey(downsample.getId()) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "deletion failed"));
        }
        map.put("data", null);
        return map;
    }

    @RequestMapping(value = "analysis/group/{queryId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> allQueryGroup(@PathVariable Integer queryId, Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAllAggregationGroupByQueryId(queryId));
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);
        return map;
    }

    @RequestMapping(value = "analysis/group/group/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> aQueryGroup(@PathVariable Integer groupId, Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAggregationGroupByGroupId(groupId));
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);
        return map;
    }

    @RequestMapping(value = "analysis/group", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> insertQueryGroup(@RequestBody(required = true) DownsampleGroup group) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.insertAggregationGroup(group)) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "failed"));
        }
        return map;
    }

    @RequestMapping(value = "analysis/group", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> updateQueryGroup(@RequestBody(required = true) DownsampleGroup group) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.updateAggregationGroup(group) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "update failed"));
        }
        return map;
    }

    @RequestMapping(value = "analysis/group", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> deleteQueryGroup(@RequestBody(required = true) Integer groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.deleteGroupByPrimaryKey(groupId) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "delete failed"));
        }
        return map;
    }

    @RequestMapping("api/export/electrode")
    @ResponseBody
    public ElectrodeVO electrode(@RequestBody(required = true) List<String> measures, Model model) {
        return columnService.selectElectrodesByMeasures(measures);
    }

    public static class ColumnRequest {
        public List<String> measure;
        public List<String> electrode;
    }

    @RequestMapping("api/export/column")
    @ResponseBody
    public List<ColumnVO> column(@RequestBody(required = true) ColumnRequest params, Model model) {
        return columnService.selectColumnsByMeasuresAndElectrodes(params.measure, params.electrode);
    }

    public static class ExportRequest {
        // column
        public String measure;
        public String electrode;
        public String column;
        // downsample
        public String method;
        public String interval;
        public String time;
        // meta filter
        public String ar;
        public String gender;
        public String ageLower;
        public String ageUpper;
    }

    @RequestMapping("api/export/export/{qid}")
    @ResponseBody
    public void exportQuery(@PathVariable(required = true) Integer qid) throws IOException {
        // List<String> pids = importedFileDao.getAllImportedPid(uuid);
        // System.out.println(pids);
        // Downsample downsample = analysisService.selectByPrimaryKey(qid);
        // List<DownsampleGroupVO> downsampleGroups = analysisService.selectAllAggregationGroupByQueryId(qid);
        // analysisService.exportFromPatientsWithDownsamplingGroups(pids, downsample, downsampleGroups);

        // TODO: Remove or change the TestRun parameter
        analysisService.exportToFile(qid, false);
    }

    @PostMapping("api/export/patient_list/{qid}")
    @ResponseBody
    public RestfulResponse uploadPatientList(@RequestParam("plist") MultipartFile file, @PathVariable(required = true) Integer qid) {
        StringBuilder sb = new StringBuilder();
        try {
            Stream<String> stream = new BufferedReader(new InputStreamReader(file.getInputStream())).lines();
            stream.forEach(s -> {
                sb.append(s.trim());
                sb.append(',');
            });
            stream.close();
        } catch (Exception o) {
            logger.error(Util.stackTraceErrorToString(o));
            return new RestfulResponse(-1, o.getLocalizedMessage());
        }
        String lists = sb.deleteCharAt(sb.length() - 1).toString();

        Downsample ds = analysisService.selectByPrimaryKey(qid);

        // AR/noAR passed by exportVO
        // ds.setPatientlist(lists);
        ds.setUpdateTime(new java.util.Date());

        if (analysisService.updateByPrimaryKey(ds) == 1) {
            return new RestfulResponse(1, file.getOriginalFilename());
        } else {
            return new RestfulResponse(-2, "Database error");
        }
    }

    @DeleteMapping("api/export/patient_list/{qid}")
    @ResponseBody
    public RestfulResponse removePatientList(@PathVariable(required = true) Integer qid) {
        Downsample ds = analysisService.selectByPrimaryKey(qid);
        ds.setUpdateTime(new java.util.Date());

        if (analysisService.updateByPrimaryKey(ds) == 1) {
            return new RestfulResponse(1, "ok");
        } else {
            return new RestfulResponse(-2, "Database error");
        }
    }

    // TODO: Remove in production
    @PutMapping("api/debug/Export")
    @ResponseBody
    public void debugExprt() {
        try {
            //analysisService.exportToFile(34, false);
            analysisService.exportToFile(35, false);

        } catch (IOException e) {
            logger.error(Util.stackTraceErrorToString(e));
        }
    }

}
