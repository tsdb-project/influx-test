/**
 * 
 */
package app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.bean.DifferRequestBodyBean;
import app.bean.ExceedRequestBodyBean;
import app.model.QueryResultBean;
import app.service.ColumnService;
import app.service.PatientService;
import app.service.QueriesService;

/**
 * @author Isolachine
 *
 */

@Controller
@RestController
@RequestMapping("query")
public class QueryController {
    @Autowired
    QueriesService queriesService;
    @Autowired
    ColumnService columnService;
    @Autowired
    PatientService patientService;

    @RequestMapping("exceed")
    @ResponseBody
    public Model exceed(Model model) {
        model.addAttribute("columns", columnService.selectAllColumn());
        model.addAttribute("nav", "queries");
        model.addAttribute("subnav", "exceed");
        return model;
    }

    @RequestMapping("exceed/query")
    public Map<String, Object> exceedQuery(@RequestBody ExceedRequestBodyBean request, Model model) {
        if (request.getColumn() == null) {
            Map<String, Object> map = new HashedMap<>();
            map.put("data", new ArrayList<>());
            return map;
        }
        // List<Patient> patients = patientService.SelectAll();
        // List<OccurenceBean> occurenceBeans = new ArrayList<>();

        List<QueryResultBean> resultBeans = queriesService.TypeAQuery(request.getColumn(), (double) request.getThreshold(), request.getCount());
        //
        // System.out.println(request.getColumn());
        // for (Patient patient : patients) {
        // OccurenceBean occurenceBean = new OccurenceBean();
        // occurenceBean.setOccurence("N/A");
        // occurenceBean.setPatient(patient);
        // occurenceBeans.add(occurenceBean);
        // }
        Map<String, Object> map = new HashedMap<>();
        map.put("data", resultBeans);
        return map;
    }

    @RequestMapping("differ")
    @ResponseBody
    public Model differ(Model model) {
        model.addAttribute("columns", columnService.selectAllColumn());
        model.addAttribute("nav", "queries");
        model.addAttribute("subnav", "differ");
        return model;
    }
    
    @RequestMapping("differ/query")
    public Map<String, Object> differQuery(@RequestBody DifferRequestBodyBean request, Model model) {
        if (request.getColumnA() == null || request.getColumnB() == null ) {
            Map<String, Object> map = new HashedMap<>();
            map.put("data", new ArrayList<>());
            return map;
        }

        List<QueryResultBean> resultBeans = queriesService.TypeBQuery(request.getColumnA(), request.getColumnB(), request.getThreshold(), request.getCount());
        Map<String, Object> map = new HashedMap<>();
        map.put("data", resultBeans);
        return map;
    }
}
