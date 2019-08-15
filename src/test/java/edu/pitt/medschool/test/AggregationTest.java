package edu.pitt.medschool.test;

import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AggregationTest {
    @Autowired
    ExportDao exportDao;
    @Test
    public void main(){
        // add a new job
        ExportWithBLOBs export = new ExportWithBLOBs();
        export.setAr(true);
        exportDao.insertExportJob(export);
        System.out.println(export.getId());
        List<String> patientIDs;
        ImportedFileDao importedFileDao = new ImportedFileDao();
        patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");

        // generate query
        // write into csv file


    }
}
