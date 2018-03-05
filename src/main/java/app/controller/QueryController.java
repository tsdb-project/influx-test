/**
 *
 */
package app.controller;

import java.text.ParseException;
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
import app.bean.RawDataRequestBodyBean;
import app.model.QueryResultBean;
import app.model.RawData;
import app.service.ColumnService;
import app.service.PatientFilteringService;
import app.service.QueriesService;
import app.service.RawDataService;

/**
 * @author Isolachine
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
    PatientFilteringService patientFilteringService;
    @Autowired
    RawDataService rawDataService;

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

        List<QueryResultBean> resultBeans = queriesService.TypeAQuery(request.getColumn(), (double) request.getThreshold(), request.getCount(), null, null);
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
        if (request.getColumnA() == null || request.getColumnB() == null) {
            Map<String, Object> map = new HashedMap<>();
            map.put("data", new ArrayList<>());
            return map;
        }

        List<QueryResultBean> resultBeans = queriesService.TypeBQuery(request.getColumnA(), request.getColumnB(), request.getThreshold(), request.getCount(), null, null);
        Map<String, Object> map = new HashedMap<>();
        map.put("data", resultBeans);
        return map;
    }

    @RequestMapping("raw")
    public Map<String, Object> raw(@RequestBody RawDataRequestBodyBean request, Model model) throws ParseException {
        Map<String, Object> map = new HashedMap<>();

        List<RawData> rawData = rawDataService.selectAllRawDataInColumns(request.getTableName(), request.getColumnNames());

        List<List<String[]>> rawDataResponse = new ArrayList<>();
        int columnSize = request.getColumnNames().size();

        for (int i = 0; i < columnSize; i++) {
            List<String[]> aColumnData = new ArrayList<>();
            rawDataResponse.add(aColumnData);
        }

        for (RawData data : rawData) {
            for (int i = 0; i < columnSize; i++) {
                String[] point = new String[2];
                long millisecond = data.getTime().getEpochSecond() * 1000;
                point[0] = String.valueOf(millisecond);
                point[1] = String.valueOf(data.getValues().get(i));
                rawDataResponse.get(i).add(point);
            }
        }
        map.put("raw", rawDataResponse);

        return map;
    }
}
