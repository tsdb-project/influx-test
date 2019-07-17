package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AutoImportService {

    @Autowired
    ImportCsvService importCsvService;


    public void initImport(){
        String dir = "D:/fileFromWin";
        System.out.println("init autoImport");
        cronJob(0,0,0,dir);

    }

    public void cronJob(int shi, int fen, int miao, String dir) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, shi);
        cal.set(Calendar.MINUTE, fen);
        cal.set(Calendar.SECOND, miao);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if(! Util.filesInFolder(dir).isEmpty() && ! Util.filesInFolderTxt(dir).isEmpty()){
                    List<FileBean> csvs = Util.filesInFolder(dir);
                    List<FileBean> txts = Util.filesInFolderTxt(dir);
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

