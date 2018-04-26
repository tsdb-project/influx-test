package edu.pitt.medschool;

import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientExample;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PatientTest {

    private String uuid = "zyc-hp052";

    @Autowired
    PatientDao pd;

    @Autowired
    ImportedFileDao ifd;

    @Test
    public void multi_cretia_test() {
        PatientExample pe = new PatientExample();
        PatientExample.Criteria pec = pe.createCriteria();
        pec.andAgeBetween((byte) 10, (byte) 15);
        pec.andFemaleEqualTo(true);

        List<String> patientIDs = pd.selectIdByCustom(pe);
        assertEquals(patientIDs.size(), 1);
        assertEquals(patientIDs.get(0), "PUH-2014-103");
    }

    @Test
    public void in_test() {
        List<String> a = new ArrayList<>();
        a.add("puh-2010-002");
        a.add("PUH-2010-003");
        a.add("PUH-2010-005");
        a.add("PUh-2010-007");
        a.add("PUH-2010-012");
        a.add("PuH-2010-013");
        a.add("pUH-2010-016");
        a.add("PUH-2010-017");

        List<Patient> tmp = pd.selectByIds(a);
        assertEquals(tmp.size(), a.size());
    }

    @Test
    public void record_number_test() {
        List<String> allImpd = ifd.getAllImportedPid(uuid);
        assertEquals(allImpd.size(), 13);

        List<Patient> tmp = pd.selectByGender("m");
        assertEquals(tmp.size(), 1252);

        List<Patient> tmp1 = pd.selectByGender("F");
        List<Patient> tmp2 = pd.selectByGender("f");
        assertEquals(tmp1.size(), tmp2.size());
        assertEquals(870, tmp1.size());
    }

}
