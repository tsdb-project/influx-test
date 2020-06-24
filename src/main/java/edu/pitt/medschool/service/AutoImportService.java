package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.*;
import edu.pitt.medschool.model.dto.AutoImportSetting;

@Service
public class AutoImportService {

    @Autowired
    ImportCsvService importCsvService;
    @Value("${FILE_TO_IMPORT}")
    String FILE_TO_IMPORT;

    private Boolean checked = false;
    private int hour = 5;
    private int minute = 0;
    private int second = 0;


    private Timer timer = new Timer();


    public void start() {
        if(this.checked){
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, this.hour);
            cal.set(Calendar.MINUTE, this.minute);
            cal.set(Calendar.SECOND, this.second);


            timer.schedule(new TimerTask() {
                public void run() {
                    if(! Util.filesInFolder(FILE_TO_IMPORT , "csv").isEmpty()
                            && ! Util.filesInFolder(FILE_TO_IMPORT,"txt").isEmpty()){
                        List<FileBean> csvs = Util.filesInFolder(FILE_TO_IMPORT , "csv");
                        List<FileBean> txts = Util.filesInFolder(FILE_TO_IMPORT,"txt");
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
        }else{
            timer.cancel();
            timer.purge();
        }

    }

    public AutoImportSetting getAutoImportSetting(){
        AutoImportSetting setting = new AutoImportSetting();
        setting.setChecked(this.checked);
        setting.setHour(this.hour);
        setting.setMinute(this.minute);
        setting.setSecond(this.second);
        return setting;
    }

    public Boolean setAutoImportSetting(AutoImportSetting setting){
        this.checked = setting.getChecked();
        this.hour = setting.getHour();
        this.minute = setting.getMinute();
        this.second = setting.getSecond();

        try {
            this.start();
        }catch (Exception e){
            return false;
        }
        return true;
    }
}

