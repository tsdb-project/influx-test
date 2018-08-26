/**
 *
 */
package edu.pitt.medschool.controller.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import edu.pitt.medschool.controller.load.vo.SearchFileVO;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.ImportProgress;
import edu.pitt.medschool.service.ImportCsvService;
import edu.pitt.medschool.service.ImportProgressService;
import edu.pitt.medschool.service.PatientService;

/**
 * @author Isolachine
 */
@RestController
public class DataController {

    @Autowired
    ImportCsvService importCsvService;
    @Autowired
    ImportProgressService importProgressService;
    @Autowired
    PatientService patientService;

    @RequestMapping("data/import")
    @ResponseBody
    public Model importData(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "import");
        return model;
    }

    @RequestMapping("data/status")
    @ResponseBody
    public Model dataStatus(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "status");
        return model;
    }

    @RequestMapping("data/patients")
    @ResponseBody
    public ModelAndView patients(ModelAndView model) {
        model.addObject("nav", "data");
        model.addObject("subnav", "patients");
        model.addObject("columns", patientService.getColumnInfo());
        return model;
    }

    @RequestMapping("data/activity")
    @ResponseBody
    public Model dataActivity(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "activity");
        return model;
    }

    @RequestMapping(value = "data/searchfile")
    @ResponseBody
    public Map<String, Object> searchfile(@RequestBody(required = false) SearchFileVO dir,
            @RequestParam(value = "dir", required = false, defaultValue = "") String dirString, Model model) {
        Map<String, Object> map = new HashMap<>();

        String directory;
        if (dir != null) {
            directory = dir.getDir();
        } else {
            directory = dirString;
        }

        List<FileBean> files = Util.filesInFolder(directory);
        map.put("data", files);

        if (files == null) {
            map.put("res", new RestfulResponse(1, "no such directory"));
        } else {
            if (files.size() > 0) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "empty folder"));
            }
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        map.put("dir", directory);
        return map;
    }

    @RequestMapping(value = "api/data/import")
    @ResponseBody
    public Map<String, Object> importDir(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model) {
        Map<String, Object> map = new HashMap<>();

        String[] allAR = new String[dir.getFiles().size()];

        for (int i = 0; i < allAR.length; i++) {
            allAR[i] = dir.getFiles().get(i);
        }

        importCsvService.AddArrayFiles(allAR);

        return map;
    }

    @RequestMapping(value = "/api/data/activity/list")
    @ResponseBody
    public RestfulResponse activityList(RestfulResponse response) {
        response.setData(importProgressService.getActivityList(importCsvService.GetUUID()));
        return response;
    }

    @RequestMapping(value = "api/data/progress")
    @ResponseBody
    public Map<String, Object> importProgress(@RequestParam(value = "file", required = false, defaultValue = "") String file, Model model) {
        Map<String, Object> map = new HashMap<>();

        String uuid = importCsvService.GetUUID();
        String batchId = importCsvService.getBatchId();
        List<ImportProgress> list = importProgressService.GetTaskAllFileProgress(uuid, batchId);
        map.put("progress", list);
        map.put("total", importProgressService.GetTaskOverallProgress(uuid, batchId));
        return map;
    }
}
