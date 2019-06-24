package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.FileLockUtil;
import edu.pitt.medschool.framework.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ConvertToCsvServies {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicBoolean importingLock = new AtomicBoolean(false);
    private final String outputDir = "C:\\EEGs\\";

//    @Autowired
//    ImportCsvService importCsvService;


    public void AddArrayFiles(String[] paths) {
        for (String aPath : paths) {
            this.convert(aPath);
        }
        String[] csvFiles = scanFolder();
//        importCsvService.AddArrayFiles(csvFiles);
    }

    private void convert(String path){
        String commond = String.format("PSCLI /SourceFile= %s /ExportCSV /OutputFile= %s",path,this.outputDir);

        Runtime run = Runtime.getRuntime();
        try{
            Process process = run.exec(commond);
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            StringBuffer sb = new StringBuffer();
            String message;
            while((message = br.readLine()) != null) {
                sb.append(message);
            }
            System.out.println(sb);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String[] scanFolder(){
        List<FileBean> files = Util.filesInFolder(this.outputDir);
        String[] csvFiles = new String[files.size()];
        for (int i = 0; i < csvFiles.length; i++) {
            csvFiles[i] = files.get(i).getDirectory();
        }
        return csvFiles;
    }
}
