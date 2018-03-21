/**
 * 
 */
package edu.pitt.medschool.controller.export;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.pitt.medschool.bean.PatientFilterBean;
import edu.pitt.medschool.service.ColumnService;
import edu.pitt.medschool.service.ExportService;
import edu.pitt.medschool.service.PatientFilteringService;

/**
 * @author Isolachine
 *
 */

@Controller
public class ExportController {
    @Autowired
    ColumnService columnService;
    @Autowired
    PatientFilteringService patientFilteringService;
    @Autowired
    ExportService exportService;

    @RequestMapping("export/export")
    public Model page(Model model) {
        model.addAttribute("nav", "export");
        model.addAttribute("subnav", "export_sub");
        List<String> trends = columnService.selectAllMeasures();
        trends.add(0, "All");
        model.addAttribute("measures", trends);
        return model;
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
        exportService.exportFromPatientsWithDownsampling(patientIDs, request.column, request.method, request.interval, request.time);
    }

}
