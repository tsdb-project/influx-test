package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AutoImportService {

    @Autowired
    ImportCsvService importCsvService;
    @Value("${FILETOIMPORT}")
    String FILETOIMPORT;


    public void initImport(){
        System.out.println("init autoImport");
        cronJob(21,0,0,FILETOIMPORT);

    }

    public void cronJob(int hour, int min, int sec, String dir) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if(! Util.filesInFolder(dir , "csv").isEmpty() && ! Util.filesInFolder(dir,"txt").isEmpty()){
                    List<FileBean> csvs = Util.filesInFolder(dir , "csv");
                    List<FileBean> txts = Util.filesInFolder(dir,"txt");
                    List<FileBean> importList = new ArrayList<>();
                    for(FileBean f: txts){
                        String name = f.getName().toLowerCase().replace(".txt","");
                        for(FileBean c: csvs){
                            if(name.toLowerCase().equals(c.getName().toLowerCase().replace(".csv",""))){
                                importList.add(c);
                            }
                        }
                    }
                    String[] path = new String[importList.size()];
                    for (int i=0;i<importList.size();i++){
                        path[i] = importList.get(i).getDirectory()+importList.get(i).getName();
                    }
                    importCsvService.AddArrayFiles(path);
                }
            }
        }, cal.getTime(), 24 * 60 * 60 * 1000);
    }
}

