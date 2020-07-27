package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.pitt.medschool.model.dto.AutoImportSetting;

@Service
public class AutoImportService {

    @Autowired
    ImportCsvService importCsvService;
    @Value("${FILE_TO_IMPORT}")
    String FILE_TO_IMPORT;

    private AtomicBoolean checked = new AtomicBoolean(false);
    private AtomicInteger hour = new AtomicInteger(5);
    private AtomicInteger minute = new AtomicInteger(0);
    private AtomicInteger second = new AtomicInteger(0);


    private volatile Timer timer;

    private void startAutoImport() {
        if (!Util.filesInFolder(FILE_TO_IMPORT, "csv").isEmpty()
                && !Util.filesInFolder(FILE_TO_IMPORT, "txt").isEmpty()) {
            List<FileBean> csvs = Util.filesInFolder(FILE_TO_IMPORT, "csv");
            List<FileBean> txts = Util.filesInFolder(FILE_TO_IMPORT, "txt");
            List<FileBean> importList = new ArrayList<>();
            for (FileBean f : txts) {
                String name = f.getName().toLowerCase().replace(".txt", "");
                for (FileBean c : csvs) {
                    if (name.toLowerCase().equals(c.getName().toLowerCase().replace(".csv", ""))) {
                        importList.add(c);
                    }
                }
            }
            String[] path = new String[importList.size()];
            for (int i = 0; i < importList.size(); i++) {
                path[i] = importList.get(i).getDirectory() + importList.get(i).getName();
            }
            importCsvService.AddArrayFiles(path);
        }
    }


    private void startAutoImportTimer() {
        if (this.checked.get()) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, this.hour.get());
            cal.set(Calendar.MINUTE, this.minute.get());
            cal.set(Calendar.SECOND, this.second.get());
            this.timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    startAutoImport();
                }
            }, cal.getTime(), 24 * 60 * 60 * 1000);

        } else {
            this.timer.cancel();
            this.timer.purge();
        }

    }

    public AutoImportSetting getAutoImportSetting() {
        AutoImportSetting setting = new AutoImportSetting();
        setting.setChecked(this.checked.get());
        setting.setHour(this.hour.get());
        setting.setMinute(this.minute.get());
        setting.setSecond(this.second.get());
        return setting;
    }

    public Boolean setAutoImportSetting(AutoImportSetting setting) {
        this.checked.set(setting.getChecked());
        this.hour.set(setting.getHour());
        this.minute.set(setting.getMinute());
        this.second.set(setting.getSecond());

        try {
            this.startAutoImportTimer();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


//    public static void main(String[] args) {
//        AutoImportSetting st = new AutoImportSetting();
//        st.setHour(16);
//        st.setMinute(0);
//        st.setSecond(0);
//        st.setChecked(false);
//
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.HOUR_OF_DAY, st.getHour());
//        cal.set(Calendar.MINUTE, st.getMinute());
//        cal.set(Calendar.SECOND, st.getSecond());
//
//        AutoImportService ais = new AutoImportService();
//
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            public void run() {
//                st.setChecked(!st.getChecked());
//                ais.setAutoImportSetting(st);
//            }
//        }, cal.getTime(), 30 * 1000);
//    }
}

