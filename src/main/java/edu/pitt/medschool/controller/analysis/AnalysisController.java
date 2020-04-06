package edu.pitt.medschool.controller.analysis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;


import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.WrongPatientsNum;
import edu.pitt.medschool.model.Wrongpatients;
import edu.pitt.medschool.model.dto.*;
import edu.pitt.medschool.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import edu.pitt.medschool.controller.analysis.vo.ColumnVO;
import edu.pitt.medschool.controller.analysis.vo.DownsampleEditResponse;
import edu.pitt.medschool.controller.analysis.vo.ElectrodeVO;
import edu.pitt.medschool.controller.analysis.vo.MedicalDownsampleEditResponse;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.Util;

/**
 * @author Isolachine
 */

@Controller
public class AnalysisController {

    @Value("${machine}")
    private String uuid;

    @Autowired
    PatientService PatientService;
    @Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    PatientMedInfoService patientMedInfoService;

    @Autowired
    ColumnService columnService;
    @Autowired
    AnalysisService analysisService;
    @Autowired
    ExportService exportService;
    @Autowired
    ExportPostProcessingService exportPostProcessingService;
    @Autowired
    UsersService usersService;

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
        return model;
    }

    @RequestMapping("analysis/builder")
    public Model builderPage(Model model) {
        return analysisGenerateModel(model);
    }

    @RequestMapping("analysis/medicalbuilder")
    public Model medicalBuilderPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "medicalbuilder");
        return model;
    }

    @RequestMapping("analysis/job")
    public Model jobPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "job");
        return model;
    }

    @RequestMapping("analysis/chart")
    @ResponseBody
    public Model chartPage(Model model) {
        model.addAttribute("nav", "management");
        model.addAttribute("subnav", "chart");
        return model;
    }


    @RequestMapping("analysis/userchart")
    @ResponseBody
    public Model userchartPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "userchart");
        return model;
    }

    @RequestMapping("analysis/wrongpatients")
    @ResponseBody
    public  Model wrongPatinetsPage(Model model){
        model.addAttribute("nav","management");
        model.addAttribute("subnav","wrongpatients");
        return model;
    }


    // now it only return the data of the latest version
    @RequestMapping("analysis/getPatientTimelines")
    @ResponseBody
    public RestfulResponse getPatientTimelines(Model model) {
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(validateCsvService.getLatestVersionPatientTimeLines("realpsc"));
        return response;
    }

    // for usr to get data by version
    @RequestMapping(value = "analysis/getPatientTimelinesByVersion/{username}")
    @ResponseBody
    public RestfulResponse getPatientTimelinesByVersion(@PathVariable String username){
        System.out.println(username.trim());
        RestfulResponse response = new RestfulResponse(1,"success");
        int version = usersService.selectByUserName(username.trim()).get(0).getDatabaseVersion();
        response.setData(validateCsvService.getPatientTimeLinesByVersion("realpsc",version));
        return response;
    }


    @RequestMapping(value = {"analysis/getWrongPatients"},method = RequestMethod.GET)
    @ResponseBody
    public RestfulResponse getWrongPatients(Model model){
        RestfulResponse response = new RestfulResponse(1,"success");
        List<Wrongpatients> wrongPatients = validateCsvService.getWrongPatients(validateCsvService.getPatientTimeLines("realpsc"));
        response.setData(wrongPatients);
        return response;
    }

    @RequestMapping(value = {"analysis/getWrongPatientsNum"},method = RequestMethod.GET)
    @ResponseBody
    public RestfulResponse getWrongPatientsNum(Model model){
        RestfulResponse response = new RestfulResponse(1,"success");
        WrongPatientsNum wrongPatientsNum = validateCsvService.getWrongPatientsNum(validateCsvService.getPatientTimeLines("realpsc"));
        response.setData(wrongPatientsNum);
        return response;
    }

//    @RequestMapping(value = { "analysis/getPatientTimelines" }, method = RequestMethod.POST)
//    @ResponseBody
//    public String getFilteredPatientTimelines(@RequestBody(required = true) GraphFilter filter) throws Exception {
//        return validateCsvService.getFilteredtPatientTimeLines("realpsc", filter);
//    }

    @RequestMapping(value = { "analysis/selecIdByfilter/{condition}" })
    @ResponseBody
    public List<String> selecIdByfilter(@PathVariable String condition) throws Exception {
        return PatientService.selecIdByfilter(condition);
    }

    @RequestMapping("analysis/getAllPatientMedInfo")
    @ResponseBody
    public Map<String, Object> getAllMedInfo(Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", patientMedInfoService.getAllMedInfo("realpsc"));
        return map;
    }

    @RequestMapping("analysis/getPatientMedInfoById/{id}")
    @ResponseBody
    public Map<String, Object> getMedInfoById(Model model, @PathVariable String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", patientMedInfoService.getMedInfoById("realpsc", id));
        return map;
    }

    /*
    add version control to this function, select files under selected version and add those fileNames into query.
    for admin, use latest version without unpublished data
     */
    @RequestMapping(value = { "analysis/eegChart" }, method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> geteegChart(@RequestBody(required = true) EEGChart eegChart) {
        List<PatientTimeLine> files = validateCsvService.getPatientTimeLinesByVersionID("realpsc",usersService.getVersionByUserName(eegChart.getUsername()),eegChart.getPatientID());
        Map<String, Object> map = new HashMap<>();
        ArrayList<List<Object>> rows = analysisService.getEEGChartData(eegChart,files);
        map.put("data", rows);
        return map;
    }

    @RequestMapping("analysis/getAllMedicine")
    @ResponseBody
    public Map<String, Object> getAllMedicine(Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAllMedicine());
        return map;
    }

    @RequestMapping(value = { "analysis/medInfo/{id}", "analysis/medInfo" }, method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView medInfoPage(@PathVariable Optional<String> id, ModelAndView modelAndView) {
        modelAndView.addObject("nav", "analysis");
        modelAndView.addObject("subnav", "chart");
        modelAndView.setViewName("analysis/medInfo");
        if (id.isPresent()) {
            modelAndView.addObject("patientId", id.get());
        } else {
            modelAndView.addObject("patientId", "Not Found");
        }
        modelAndView.addObject("measures", columnService.selectAllMeasures());
        return modelAndView;
    }

    @RequestMapping("analysis/create")
    public Model createPage(Model model) {
        return analysisGenerateModel(model);
    }

    @RequestMapping("analysis/medical")
    public Model medical(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "medicalbuilder");
        return model;
    }

    @RequestMapping(value = { "analysis/edit/{id}", "analysis/edit" }, method = RequestMethod.GET)
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

    @RequestMapping(value = { "analysis/medicaledit/{id}", "analysis/medicaledit" }, method = RequestMethod.GET)
    public ModelAndView medicaledit(@PathVariable Optional<Integer> id, ModelAndView modelAndView) {
        modelAndView.addObject("nav", "analysis");
        modelAndView.addObject("subnav", "medicalbuilder");
        modelAndView.setViewName("analysis/medicaledit");
        if (id.isPresent()) {
            modelAndView.addObject("medicaledit", true);
            MedicalDownsample downsample = analysisService.selectmedicalByPrimaryKey(id.get());
            MedicalDownsampleEditResponse downsampleEditResponse = new MedicalDownsampleEditResponse(downsample);
            modelAndView.addObject("medicalquery", downsampleEditResponse);
        } else {
            modelAndView.addObject("medicaledit", false);
            List<MedicalDownsample> downsamples = analysisService.selectmedicalAll();
            modelAndView.addObject("medicalDownsamples", downsamples);
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

    @RequestMapping(value = "analysis/medicalquery", method = RequestMethod.GET)
    @ResponseBody
    public RestfulResponse allmedicalQuery(Model model) {
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(analysisService.selectmedicalAll());
        return response;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse insert(@RequestBody(required = true) Downsample downsample) throws Exception {
        RestfulResponse response;
        if (downsample.getPeriod() == 0) {
            downsample.setPeriod(1);
        }
        if (analysisService.insertDownsample(downsample) == 1) {
            response = new RestfulResponse(1, "success");
            response.setData(downsample);
        } else {
            response = new RestfulResponse(0, "insert failed");
        }
        return response;
    }

    @RequestMapping(value = "analysis/medicalquery", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse medicalInsert(@RequestBody(required = true) MedicalDownsample downsample) throws Exception {
        RestfulResponse response;
        if (downsample.getPeriod() == 0) {
            downsample.setPeriod(1);
        }
        if (analysisService.insertMedicalDownsample(downsample) == 1) {
            response = new RestfulResponse(1, "success");
            response.setData(downsample);
        } else {
            response = new RestfulResponse(0, "insert failed");
        }
        return response;
    }

    @PutMapping(value = "analysis/query")
    @ResponseBody
    public Map<String, Object> update(@RequestBody(required = true) Downsample downsample) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (downsample.getPeriod() == 0) {
            downsample.setPeriod(1);
        }
        if (analysisService.updateByPrimaryKey(downsample) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "update failed"));
        }
        map.put("data", analysisService.selectByPrimaryKey(downsample.getId()));
        return map;
    }

    @PutMapping(value = "analysis/medicalquery")
    @ResponseBody
    public Map<String, Object> medicalUpdate(@RequestBody(required = true) MedicalDownsample medicalDownsample)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (medicalDownsample.getPeriod() == 0) {
            medicalDownsample.setPeriod(1);
        }
        if (analysisService.updatemedicalByPrimaryKey(medicalDownsample) == 1) {
            map.put("res", new RestfulResponse(1, "success"));
        } else {
            map.put("res", new RestfulResponse(0, "update faild"));
        }
        map.put("data", analysisService.selectmedicalByPrimaryKey(medicalDownsample.getId()));
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

    @RequestMapping(value = "analysis/medicalquery", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String,Object> delete(@RequestBody(required = true) MedicalDownsample medicalDownsample) throws Exception{
        Map<String,Object> map = new HashMap<>();
        if (analysisService.deleteMedicalByPrimaryKey(medicalDownsample.getId())==1) {
            map.put("res", new RestfulResponse(1,"success"));
        } else {
            map.put("res", new RestfulResponse(0,"deletion failed"));
        }
        map.put("data",null);
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

    @RequestMapping(value = "analysis/medicalgroup/{queryId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> allmedicalQueryGroup(@PathVariable Integer queryId, Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAllmedicalAggregationGroupByQueryId(queryId));
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

    @RequestMapping(value = "analysis/medicalgroup/medicalgroup/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> amedicalQueryGroup(@PathVariable Integer groupId, Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectmedicalAggregationGroupById(groupId));
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

    @RequestMapping(value = "analysis/medicalgroup", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> insertmedicalQueryGroup(@RequestBody(required = true) MedicalDownsampleGroup group)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.insertmedicalAggregationGroup(group)) {
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

    @RequestMapping(value = "analysis/medicalgroup", method = RequestMethod.PUT)
    @ResponseBody
    public Map<String, Object> updatemedicalQueryGroup(@RequestBody(required = true) MedicalDownsampleGroup group)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.updatemedicalAggregationGroup(group) == 1) {
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

    @RequestMapping(value = "analysis/medicalgroup", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> deletemedicalQueryGroup(@RequestBody(required = true) Integer groupId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if (analysisService.deletemedicalGroupByPrimaryKey(groupId) == 1) {
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

    @PostMapping("api/export/export")
    @ResponseBody
    public RestfulResponse exportQuery(@RequestBody(required = true) ExportWithBLOBs job, RestfulResponse response)
            throws JsonProcessingException {
        if (exportService.completeJobAndInsert(job) == 1) {
            if (analysisService.addOneExportJob(job.getId())) {
                response.setCode(1);
                response.setMsg("Successfully added job.");
            } else {
                response.setCode(2);
                response.setMsg("Failed to add job into queue.");
            }
        } else {
            response.setCode(0);
            response.setMsg("Database error!");
        }
        return response;
    }

    @PostMapping("api/medicalexport/medicalexport")
    @ResponseBody
    public RestfulResponse exportmedicalQuery(@RequestBody(required = true) ExportWithBLOBs job, RestfulResponse response)
            throws JsonProcessingException {
        if (exportService.completeMedicalJobAndInsert(job) == 1) {
            if (analysisService.addOneExportJob(job.getId())) {
                response.setCode(1);
                response.setMsg("Successfully added job.");
            } else {
                response.setCode(2);
                response.setMsg("Failed to add job into queue.");
            }
        } else {
            response.setCode(0);
            response.setMsg("Database error!");
        }
        return response;
    }

    @DeleteMapping("api/export/export/{id}")
    @ResponseBody
    public RestfulResponse deleteExportQuery(@PathVariable(value = "id", required = true) Integer jobId,
            RestfulResponse response) {
        if (exportService.deleteExportJobById(jobId) == 1) {
            response.setCode(1);
            response.setMsg("Successfully deleted job.");
        } else {
            response.setCode(0);
            response.setMsg("Database error!");
        }
        return response;
    }

    @DeleteMapping("api/export/stop/{id}")
    @ResponseBody
    public RestfulResponse stopExportQuery(@PathVariable(value = "id", required = true) Integer jobId,
            RestfulResponse response) {
        int res = analysisService.removeOneExportJob(jobId);
        if (res == 1) {
            response.setCode(1);
            response.setMsg("Successfully stopped job.");
        } else if (res == -1) {
            response.setCode(1);
            response.setMsg("Job already finished or canceled!");
        } else {
            response.setCode(0);
            response.setMsg("Database error!");
        }
        return response;
    }

    @PostMapping("api/export/patient_list/")
    @ResponseBody
    public RestfulResponse uploadPatientList(@RequestParam("plist") MultipartFile file) {
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
        RestfulResponse response = new RestfulResponse(1, file.getOriginalFilename());
        response.setData(lists);
        return response;
    }

    @GetMapping("api/analysis/job")
    @ResponseBody
    public RestfulResponse uploadPatientList() {
        RestfulResponse response = new RestfulResponse(1, "");
        response.setData(analysisService.selectAllExportJobOnLocalMachine());
        return response;
    }

    @GetMapping(value = "download", params = { "path", "id" })
    public StreamingResponseBody getSteamingFile(HttpServletResponse response, @RequestParam("path") String path,
            @RequestParam("id") Integer id) throws IOException {
        response.setContentType("application/zip");
        Path p = Paths.get(".", path);
        response.setContentLengthLong(Files.size(p));
        if(path.contains("split")){
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "output_split_" + id + ".zip"));
        }else{
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", "output_" + id + ".zip"));
        }
        InputStream inputStream = Files.newInputStream(p, StandardOpenOption.READ);
        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, nRead);
            }
            inputStream.close();
        };
    }

    @GetMapping("api/analysis/post-processing/{columnGroun}")
    @ResponseBody
    public RestfulResponse postProcessing(@PathVariable Integer columnGroun) throws IOException {
        RestfulResponse response = new RestfulResponse(1, "Finished");
        String msg = exportPostProcessingService.transform(columnGroun);
        response.setData(msg);
        return response;
    }

}
