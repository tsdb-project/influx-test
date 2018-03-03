/**
 *
 */
package app.controller;

import app.bean.FileBean;
import app.bean.Response;
import app.bean.SearchFileForm;
import app.service.ImportCsvService;
import app.service.util.ImportProgressService;
import app.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isolachine
 */
@Controller
@RestController
public class DataController {

    @Autowired
    ImportCsvService importCsvService = new ImportCsvService();

    private String fileLocation;

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
    public Map<String, Object> searchfile(@RequestBody(required = false) SearchFileForm dir, @RequestParam(value = "dir", required = false, defaultValue = "") String dirString, Model model) {
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
            map.put("res", new Response(1, "no such directory"));
        } else {
            if (files.size() > 0) {
                map.put("res", new Response(1, "success"));
            } else {
                map.put("res", new Response(0, "empty folder"));
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
    public Map<String, Object> importDir(@RequestBody(required = false) SearchFileForm dir, @RequestParam(value = "dir", required = false, defaultValue = "") String dirString, Model model) {
        // String directory;
        Map<String, Object> map = new HashMap<>();

        for (String ee : dir.getFiles()) {
            System.out.println(ee);
        }

        // if (dir != null) {
        // directory = dir.getDir();
        // } else {
        // directory = dirString;
        // }
        //
        // if (!directory.equals("")) {

        String[] allAR = new String[dir.getFiles().size()];
        for (int i = 0; i < allAR.length; i++) {
            allAR[i] = dir.getFiles().get(i);
        }
        importCsvService.AddArrayFiles(allAR);
        //TODO: Should let user choose LoadFactor
        importCsvService.DoImport(0.01);
        // }
        //
        // map.put("dir", directory);
        return map;
    }

    @RequestMapping(value = "api/data/progress")
    @ResponseBody
    public Map<String, Object> importProgress(Model model) {

        Map<String, Object> map = new HashMap<>();

        String uuid = importCsvService.GetUUID();

        map.put("uuid", uuid);

        //TODO: Check the content of allstat
        Map<String, List<Object>> allstat = ImportProgressService.GetTaskAllFileProgress(uuid);

        map.put("file", "Shoud be a list here");
        map.put("progress", "Shoud be a list here");
        map.put("total", ImportProgressService.GetTaskOverallProgress(uuid));

        return map;
    }
}
