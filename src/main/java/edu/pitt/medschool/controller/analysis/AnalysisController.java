/**
 * 
 */
package edu.pitt.medschool.controller.analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.pitt.medschool.bean.PatientFilterBean;
import edu.pitt.medschool.controller.analysis.vo.DownsampleEditResponse;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.service.AnalysisService;
import edu.pitt.medschool.service.ColumnService;
import edu.pitt.medschool.service.PatientFilteringService;

/**
 * @author Isolachine
 *
 */

@Controller
public class AnalysisController {
    @Autowired
    ColumnService columnService;
    @Autowired
    PatientFilteringService patientFilteringService;
    @Autowired
    AnalysisService analysisService;

    @RequestMapping("analysis/export")
    public Model exportPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "export");
        List<String> trends = columnService.selectAllMeasures();
        trends.add(0, "All");
        model.addAttribute("measures", trends);
        return model;
    }

    @RequestMapping("analysis/builder")
    public Model builderPage(Model model) {
        model.addAttribute("nav", "analysis");
        model.addAttribute("subnav", "builder");
        List<Downsample> downsamples = analysisService.selectAll();
        model.addAttribute("downsamples", downsamples);
        return model;
    }

    @RequestMapping(value = { "/analysis/edit/{id}", "/analysis/edit" }, method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable Optional<Integer> id, ModelAndView modelAndView) {
        modelAndView.addObject("nav", "analysis");
        modelAndView.addObject("subnav", "edit");
        modelAndView.setViewName("/analysis/edit");
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
        return modelAndView;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> allQuery(Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAll());
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);
        return map;
    }

    @RequestMapping(value = "analysis/query", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> insert(@RequestBody(required = true) Downsample downsample) throws Exception {
        analysisService.insert(downsample);
        Map<String, Object> map = new HashMap<>();
        map.put("data", analysisService.selectAll());
        return map;
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


    @RequestMapping("api/export/electrode")
    @ResponseBody
    public List<String> electrode(@RequestBody(required = true) List<String> measures, Model model) {
        return columnService.selectElectrodesByMeasures(measures);
    }

    public static class ColumnRequest {
        public String measure;
        public List<String> electrode;
    }

    @RequestMapping("api/export/column")
    @ResponseBody
    public List<String> column(@RequestBody(required = true) ColumnRequest params, Model model) {
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

    @RequestMapping("api/export/export")
    @ResponseBody
    public void export(@RequestBody(required = true) ExportRequest request, Model model) throws IOException {
        PatientFilterBean filter = new PatientFilterBean();
        if (request.ageLower != null && !request.ageLower.isEmpty()) {
            filter.setAgeLowerFilter(Integer.valueOf(request.ageLower));
        }
        if (request.ageUpper != null && !request.ageUpper.isEmpty()) {
            filter.setAgeUpperFilter(Integer.valueOf(request.ageUpper));
        }
        if (request.gender != null && !request.gender.isEmpty()) {
            filter.setGenderFilter(request.gender);
        }
        List<String> patientIDs = patientFilteringService.FetchResultPid(filter);
        patientIDs.retainAll(patientFilteringService.GetAllImportedPid());

        System.out.println(patientIDs);
        System.out.println(request.interval + "====" + request.time + "======" + request.method);
        analysisService.exportFromPatientsWithDownsampling(patientIDs, request.column, request.method, request.interval, request.time);
    }

}
