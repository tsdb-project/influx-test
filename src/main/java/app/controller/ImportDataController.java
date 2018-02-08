/**
 *
 */
package app.controller;

import app.model.JobStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Isolachine
 */

@RestController
@RequestMapping("/apis/data")
public class ImportDataController {

    private String fileLocation;

    @RequestMapping(value = "/import", method = RequestMethod.POST)
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

}
