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

import edu.pitt.medschool.bean.FileBean;
import edu.pitt.medschool.controller.load.vo.ProgressVO;
import edu.pitt.medschool.controller.load.vo.SearchFileVO;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.service.ImportCsvService;
import edu.pitt.medschool.service.ImportProgressService;

/**
 * @author Isolachine
 */
@RestController
public class DataController {

    @Autowired
    ImportCsvService importCsvService;
    @Autowired
    ImportProgressService importProgressService;

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

    @RequestMapping(value = "data/searchfile")
    @ResponseBody
    public Map<String, Object> searchfile(@RequestBody(required = false) SearchFileVO dir, @RequestParam(value = "dir", required = false, defaultValue = "") String dirString, Model model) {
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

    @RequestMapping(value = "api/data/progress")
    @ResponseBody
    public Map<String, Object> importProgress(@RequestParam(value = "file", required = false, defaultValue = "") String file, Model model) {
        Map<String, Object> map = new HashMap<>();

        String uuid = importCsvService.GetUUID();
        List<ProgressVO> list = importProgressService.GetTaskAllFileProgress(uuid);
        map.put("progress", list);
        map.put("finished", null);
        map.put("total", importProgressService.GetTaskOverallProgress(uuid));
        // TODO: adjust the code START
        /*
         * Map<String, List<Object>> allstat = ImportProgressService.GetTaskAllFileProgress(uuid); for (String key : allstat.keySet()) { map.put(key, allstat.get(key)); }
         * 
         * String total = String.format("%.2f", ImportProgressService.GetTaskOverallProgress(uuid) * 100); map.put("total", total);
         */
        // TODO: adjust the code END

        // if (!allstat.get("filename").contains(file)) {
        // map.put("finished", false);
        // } else {
        // for (Object status : allstat.get("status")) {
        // if (!status.toString().equals("STATUS_FINISHED")) {
        // map.put("finished", false);
        // return map;
        // }
        // }
        // }
        // map.put("finished", true);
        return map;
    }
}
