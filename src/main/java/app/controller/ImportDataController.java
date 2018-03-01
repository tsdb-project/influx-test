/**
 *
 */
package app.controller;

import app.bean.FileBean;
import app.bean.Response;
import app.bean.SearchFileForm;
import app.model.JobStatus;
import app.service.ImportCsvService;
import app.util.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Isolachine
 */
@Controller
@RestController
public class ImportDataController {

    @Autowired
    ImportCsvService importCsvService;

    private String fileLocation;

    @RequestMapping(value = "/apis/data/import", method = RequestMethod.POST)
    @ResponseBody
    public JobStatus importData(@RequestParam("file") MultipartFile file) {
        JobStatus rst = new JobStatus();
        rst.setStatusCode(200);
        rst.setInfo("File import OK!");

        try {
            InputStream in = file.getInputStream();
            File currDir = new File(".");
            String path = currDir.getAbsolutePath();
            fileLocation = path.substring(0, path.length() - 1) + file.getOriginalFilename();
            FileOutputStream f = new FileOutputStream(fileLocation);
            int ch = 0;
            while ((ch = in.read()) != -1) {
                f.write(ch);
            }
            f.flush();
            f.close();

        } catch (IOException e) {
            rst.setStatusCode(500);
            rst.setInfo(e.getLocalizedMessage());
        } catch (Exception ee) {
            rst.setStatusCode(500);
            rst.setInfo(ee.getLocalizedMessage());
        }

        return rst;
    }

    @RequestMapping("data/import")
    @ResponseBody
    public Model importData(Model model) {
        model.addAttribute("nav", "import");
        model.addAttribute("subnav", "");
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
        importCsvService.ImportByList(allAR, true, "ART");
        // }
        //
        // map.put("dir", directory);
        return map;
    }

    @RequestMapping(value = "api/data/progress")
    @ResponseBody
    public Map<String, Object> importProgress(Model model) {

        Map<String, Object> map = new HashMap<>();

        String uuid = importCsvService.currentUUID;

        map.put("uuid", uuid);
        map.put("file", importCsvService.currentFile);
        if (uuid.equals("")) {
            map.put("progress", 0.0);
        } else {
            String progress = String.format("%.2f", importCsvService.CheckProgressWithUUID(uuid) * 100);
            map.put("progress", progress);
        }
        String totalProgress = String.format("%.2f", importCsvService.totalProgress * 100);
        map.put("total", totalProgress);
        map.put("finished", importCsvService.progressState);

        return map;
    }
}
