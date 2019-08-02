package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.RunThread;
import edu.pitt.medschool.framework.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ConvertToCsvServies {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String panel = "ActiveNotafications";
    private final String inputDir = "C:\\patientsEEG\\";
    private final String outputDir = "C:\\EEGs\\";


    public void covertAndSend(String[] paths) {
        for (String aPath : paths) {
            this.convert(aPath);
        }
        String[] csvFiles = scanFolder();
    }

    private void convert(String path){
        String commond = String.format("PSCLI /panel='%s'/SourceFile='%s' /ExportCSV",this.panel,path);
        try{
            Process process = Runtime.getRuntime().exec(commond);
            new RunThread(process.getInputStream(), "INFO").start();
            new RunThread(process.getErrorStream(),"ERR").start();
            int value = process.waitFor();
            if(value == 0){
                List<FileBean> csvs = Util.filesInFolder(path.replace("*.eeg","") , "csv");
                for (FileBean csv : csvs){
                    File file = new File(csv.getDirectory() + csv.getName());
                    if(file.renameTo(new File(this.outputDir + csv.getName()))){
                        return;
                    }else{
                        logger.error( path + ": csv file move failed");
                    }
                }
            }else{
                logger.error( path + ": command line execution failed");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String[] scanFolder(){
        List<FileBean> files = Util.filesInFolder(this.outputDir , "csv");
        String[] csvFiles = new String[files.size()];
        for (int i = 0; i < csvFiles.length; i++) {
            csvFiles[i] = files.get(i).getDirectory();
        }
        return csvFiles;
    }
}
