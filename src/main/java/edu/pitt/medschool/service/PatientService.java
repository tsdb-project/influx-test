/**
 *
 */
package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dao.PatientDao;

/**
 * service for returning column information of data
 * 
 * @author Isolachine
 */
@Service
public class PatientService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PatientDao patientDao;

    public List<MysqlColumnBean> getColumnInfo() {
        return patientDao.getColumnInfo();
    }

    public List<String> selecIdByfilter(String condition) {
        return patientDao.selecIdByfilter(condition);
    }

    public List<Patient> getAllPatientsComments() {
        return patientDao.getAllPatientsComments();
    }

    public int changePatientComment(String pid, String comment) {
        int changeCommentResult = 0;
        try {
            changeCommentResult = patientDao.changePatientComment(pid,comment);
        }catch (Exception e){
            logger.debug("PATIENT CHANGE COMMENT FAILED!");
        }
        return changeCommentResult;
    }

    // Transfer MM/DD/YY HH:MM to yyyy-MM-dd HH:mm:ss
    public String timeToStanderTime(String s){
        String date = "";
        String time = "";
        if(s.split(" ").length==1){
            date = s.split(" ")[0];
            time = "00:00:00";
        }else {
            date = s.split(" ")[0];
            time = s.split(" ")[1];
        }
        String[] date_parts = date.split("/");
        String[] time_parts = time.split(":");
        String year = date_parts[2];
        if(year.length()==2){
            year= "20"+year;
        }
        String day = date_parts[1];
        if(day.length()==1){
            day = "0"+day;
        }
        String month = date_parts[0];
        if(month.length()==1){
            month = "0"+month;
        }
        String hour = time_parts[0];
        if(hour.length()==1){
            hour = "0"+hour;
        }
        String minute = time_parts[1];
        if(minute.length()==1){
            minute = "0"+minute;
        }
        return year+"-"+month+"-"+day+" "+hour+":"+minute+":"+"00";
    }

    public Map getPatientsFromCsv(String dir){
        List<PatientWithBLOBs> patients = new ArrayList<>();
        int count = 0;
        Map<String,Object> result = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String firstline = reader.readLine();
            String line = reader.readLine();
            while ((line != null)){
                while (patients.size()<=90 && line !=null) {
                    String[] info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                    PatientWithBLOBs patient = new PatientWithBLOBs();
                    patient.setId(info[0]);
                    if (!info[1].isEmpty())
                        patient.setAge((info[1]).getBytes()[0]);
                    patient.setFemale(info[2] == "1");
                    // [3] arrestdate
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    if (!info[21].isEmpty())
                        patient.setArrestdate(LocalDate.parse(timeToStanderTime(info[21]), fmt));
                    if (!info[4].isEmpty())
                        patient.setOohca(Integer.valueOf(info[4]));
                    if (!info[5].isEmpty())
                        patient.setIhcaLoc(Integer.valueOf(info[5]));
                    if (!info[6].isEmpty())
                        patient.setEdarrest(Integer.valueOf(info[6]));
                    if (!info[7].isEmpty())
                        patient.setRhythm(Integer.valueOf(info[7]));
                    if (!info[8].isEmpty())
                        patient.setCaType(Integer.valueOf(info[8]));
                    if (!info[9].isEmpty())
                        patient.setTransfer(Integer.valueOf(info[9]));
                    if (!info[10].isEmpty())
                        patient.setIsDelayed(Integer.valueOf(info[10]));
                    patient.setReferringHospital(info[11]);
                    patient.setReferringPhysician(info[12]);
                    patient.setServiceScene(info[13]);
                    patient.setSceneProvider(info[14]);
                    patient.setService2Scene(info[15]);
                    patient.setSceneChart2(info[16]);
                    patient.setServiceInterfacility(info[17]);
                    patient.setInterfacilityProvider(info[18]);
                    patient.setReferringfollowup(info[19]);
                    patient.setEmsfollowup(info[20]);
                    if (!info[21].isEmpty())
                        patient.setArresttime(LocalDateTime.parse(timeToStanderTime(info[21]), fmt));
                    if (!info[22].isEmpty())
                        patient.setRhythmEms(Integer.valueOf(info[22]));
                    if (!info[23].isEmpty())
                        patient.setWitnessed(Integer.valueOf(info[23]));
                    if (!info[24].isEmpty())
                        patient.setBystanderCpr(Integer.valueOf(info[24]));
                    if (!info[25].isEmpty())
                        patient.setAed(Integer.valueOf(info[25]));
                    if (!info[26].isEmpty())
                        patient.setShocks(Integer.valueOf(info[26]));
                    if (!info[27].isEmpty())
                        patient.setDuration(Integer.valueOf(info[27]));
                    if (!info[28].isEmpty())
                        patient.setCprWithAls(Integer.valueOf(info[28]));
                    if (!info[29].isEmpty())
                        patient.setEpi(Integer.valueOf(info[29]));
                    if (!info[30].isEmpty())
                        patient.setBicarb(Integer.valueOf(info[30]));
                    if (!info[31].isEmpty())
                        patient.setAirway(Integer.valueOf(info[31]));
                    if (!info[32].isEmpty())
                        patient.setPrehospitalRosc(Integer.valueOf(info[32]));
                    if (!info[33].isEmpty())
                        patient.setRearrests(Integer.valueOf(info[33]));
                    if (!info[34].isEmpty())
                        patient.setPridScene(info[34]);
                    patient.setSceneChart(info[35]);
                    if (!info[36].isEmpty())
                        patient.setPridScene2(info[36]);
                    patient.setSceneChart2(info[37]);
                    if (!info[38].isEmpty())
                        patient.setPridInterfacility(info[38]);
                    patient.setInterfacilityChart(info[39]);
                    if (!info[40].isEmpty())
                        patient.setArrival(LocalDateTime.parse(timeToStanderTime(info[40]), fmt));
                    if (!info[41].isEmpty())
                        patient.setAdmitUnit(Integer.valueOf(info[41]));
                    if (!info[42].isEmpty())
                        patient.setAdmitFloor(Integer.valueOf(info[42]));
                    if (!info[43].isEmpty())
                        patient.setIcus1(Integer.valueOf(info[43]));
                    if (!info[44].isEmpty())
                        patient.setIcus2(Integer.valueOf(info[44]));
                    if (!info[45].isEmpty())
                        patient.setIcus3(Integer.valueOf(info[45]));
                    if (!info[46].isEmpty())
                        patient.setIcus4(Integer.valueOf(info[46]));
                    if (!info[47].isEmpty())
                        patient.setIcus5(Integer.valueOf(info[47]));
                    if (!info[48].isEmpty())
                        patient.setIcus6(Integer.valueOf(info[48]));
                    if (!info[49].isEmpty())
                        patient.setIcus7(Integer.valueOf(info[49]));
                    if (!info[50].isEmpty())
                        patient.setIcus8(Integer.valueOf(info[50]));
                    if (!info[51].isEmpty())
                        patient.setIcus9(Integer.valueOf(info[51]));
                    patient.setFourEye0(info[52]);
                    patient.setFourM0(info[53]);
                    patient.setPupils0(info[54]);
                    patient.setCorneals0(info[55]);
                    patient.setCough0(info[56]);
                    patient.setGag0(info[57]);
                    patient.setFourR0(info[58]);
                    patient.setTtm(info[59]);
                    if (!info[60].isEmpty())
                        patient.setPmhxUnknown(Integer.valueOf(info[60]));
                    if (!info[61].isEmpty())
                        patient.setCcimi(Integer.valueOf(info[61]));
                    if (!info[62].isEmpty())
                        patient.setCcipvd(Integer.valueOf(info[62]));
                    if (!info[63].isEmpty())
                        patient.setCcidementia(Integer.valueOf(info[63]));
                    if (!info[64].isEmpty())
                        patient.setCcicva(Integer.valueOf(info[64]));
                    if (!info[65].isEmpty())
                        patient.setCcihemi(Integer.valueOf(info[65]));
                    if (!info[66].isEmpty())
                        patient.setCcichf(Integer.valueOf(info[66]));
                    patient.setCcicvd(info[67]);
                    if (!info[68].isEmpty())
                        patient.setCcicld(Integer.valueOf(info[68]));
                    if (!info[69].isEmpty())
                        patient.setCcictd(Integer.valueOf(info[69]));
                    if (!info[70].isEmpty())
                        patient.setCcipud(Integer.valueOf(info[70]));
                    if (!info[71].isEmpty())
                        patient.setCciaids(Integer.valueOf(info[71]));
                    if (!info[72].isEmpty())
                        patient.setCcickd(Integer.valueOf(info[72]));
                    patient.setCcielsd(info[73]);
                    patient.setCcidm(info[74]);
                    patient.setCcica(info[75]);
                    if (!info[76].isEmpty())
                        patient.setCcileukemia(Integer.valueOf(info[76]));
                    if (!info[77].isEmpty())
                        patient.setCcilymphoma(Integer.valueOf(info[77]));
                    patient.setPfratio0(info[78]);
                    patient.setSofaCv0(info[79]);
                    patient.setBili0(info[80]);
                    patient.setPlt0(info[81]);
                    patient.setCr0(info[82]);
                    patient.setLactate0(info[83]);
                    patient.setCtbrain(info[84]);
                    patient.setCtbrainTime(info[85]);
                    patient.setGwr(info[86]);
                    patient.setSulci(info[87]);
                    patient.setCisterns(info[88]);
                    patient.setCath(info[89]);
                    patient.setCathdate(info[90]);
                    patient.setCathAcutelesion(info[91]);
                    patient.setCathPci(info[92]);
                    patient.setCathCabg(info[93]);
                    patient.setAlive1(info[94]);
                    patient.setFourEye1(info[95]);
                    patient.setFourM1(info[96]);
                    patient.setPupils1(info[97]);
                    patient.setCorneals1(info[98]);
                    patient.setCough1(info[99]);
                    patient.setGag1(info[100]);
                    patient.setFourR1(info[101]);
                    patient.setPfratio1(info[102]);
                    patient.setSofaCv1(info[103]);
                    patient.setBili1(info[104]);
                    patient.setPlt1(info[105]);
                    patient.setCr1(info[106]);
                    patient.setLactate1(info[107]);
                    //2
                    patient.setAlive2(info[108]);
                    patient.setFourEye2(info[109]);
                    patient.setFourM2(info[110]);
                    patient.setPupils2(info[111]);
                    patient.setCorneals2(info[112]);
                    patient.setCough2(info[113]);
                    patient.setGag2(info[114]);
                    patient.setFourR2(info[115]);
                    patient.setPfratio2(info[116]);
                    patient.setSofaCv2(info[117]);
                    patient.setBili2(info[118]);
                    patient.setPlt2(info[119]);
                    patient.setCr2(info[120]);
                    patient.setLactate2(info[121]);
                    //3
                    patient.setAlive3(info[122]);
                    patient.setFourEye3(info[123]);
                    patient.setFourM3(info[124]);
                    patient.setPupils3(info[125]);
                    patient.setCorneals3(info[126]);
                    patient.setCough3(info[127]);
                    patient.setGag3(info[128]);
                    patient.setFourR3(info[129]);
                    patient.setPfratio3(info[130]);
                    patient.setSofaCv3(info[131]);
                    patient.setBili3(info[132]);
                    patient.setPlt3(info[133]);
                    patient.setCr3(info[134]);
                    patient.setLactate3(info[135]);
                    //4
                    patient.setAlive4(info[136]);
                    patient.setFourEye4(info[137]);
                    patient.setFourM4(info[138]);
                    patient.setPupils4(info[139]);
                    patient.setCorneals4(info[140]);
                    patient.setCough4(info[141]);
                    patient.setGag4(info[142]);
                    patient.setFourR4(info[143]);
                    patient.setPfratio4(info[144]);
                    patient.setSofaCv4(info[145]);
                    patient.setBili4(info[146]);
                    patient.setPlt4(info[147]);
                    patient.setCr4(info[148]);
                    patient.setLactate4(info[149]);
                    //5
                    patient.setAlive5(info[150]);
                    patient.setFourEye5(info[151]);
                    patient.setFourM5(info[152]);
                    patient.setPupils5(info[153]);
                    patient.setCorneals5(info[154]);
                    patient.setCough5(info[155]);
                    patient.setGag5(info[156]);
                    patient.setFourR5(info[157]);
                    patient.setPfratio5(info[158]);
                    patient.setSofaCv5(info[159]);
                    patient.setBili5(info[160]);
                    patient.setPlt5(info[161]);
                    patient.setCr5(info[162]);
                    patient.setLactate5(info[163]);
                    // repeat end
                    patient.setEeg(info[164]);
                    patient.setSseps(info[165]);
                    patient.setSsepDate(info[166]);
                    patient.setN20(info[167]);
                    patient.setSsepText(info[168]);
                    patient.setRepeatctbrain(info[169]);
                    patient.setRepeatctbrainTime(info[170]);
                    patient.setRepeatgwr(info[171]);
                    patient.setRepeatcisterns(info[172]);
                    patient.setRepeatsulci(info[173]);
                    if (!info[174].isEmpty())
                        patient.setMri(Integer.valueOf(info[174]));
                    patient.setMriDate(info[175]);
                    patient.setAdc(info[176]);
                    if (!info[177].isEmpty())
                        patient.setEtiology(Integer.valueOf(info[177]));
                    patient.setEtiologyOtherunknown(info[178]);
                    if (!info[179].isEmpty())
                        patient.setFollowCom(Integer.valueOf(info[179]));
                    if (!info[180].isEmpty())
                        patient.setDateFolCom(LocalDate.parse(timeToStanderTime(info[180]), fmt));
                    if (!info[181].isEmpty())
                        patient.setSurv(Integer.valueOf(info[181]));
                    if (!info[182].isEmpty())
                        patient.setDischargedate(LocalDate.parse(timeToStanderTime(info[182]), fmt));
                    if (!info[183].isEmpty())
                        patient.setDeathdate(LocalDate.parse(timeToStanderTime(info[183]), fmt));
                    if (!info[184].isEmpty())
                        patient.setDeathCat(Integer.valueOf(info[184]));
                    if (!info[185].isEmpty())
                        patient.setIcuLos(Integer.valueOf(info[185]));
                    if (!info[186].isEmpty())
                        patient.setHospitalLos(Integer.valueOf(info[186]));
                    patient.setDefibrillator(info[187]);
                    patient.setDisposition(info[188]);
                    patient.setDispoOther(info[189]);
                    if (!info[190].isEmpty())
                        patient.setCpc(Integer.valueOf(info[190]));
                    if (!info[191].isEmpty())
                        patient.setMri(Integer.valueOf(info[191]));
                    if (!info[192].isEmpty())
                        patient.setReferredtocore(Integer.valueOf(info[192]));
                    if (!info[193].isEmpty())
                        patient.setOrgandonor(Integer.valueOf(info[193]));
                    patient.setLifelogic(info[194]);
                    if (!info[195].isEmpty())
                        patient.setNondonor(Integer.valueOf(info[195]));
                    if (!info[196].isEmpty())
                        patient.setDonortype(Integer.valueOf(info[196]));
                    if (!info[197].isEmpty())
                        patient.setProcured1(Integer.valueOf(info[197]));
                    if (!info[198].isEmpty())
                        patient.setProcured2(Integer.valueOf(info[198]));
                    if (!info[199].isEmpty())
                        patient.setProcured3(Integer.valueOf(info[199]));
                    if (!info[200].isEmpty())
                        patient.setProcured4(Integer.valueOf(info[200]));
                    if (!info[201].isEmpty())
                        patient.setProcured5(Integer.valueOf(info[201]));
                    if (!info[202].isEmpty())
                        patient.setProcured6(Integer.valueOf(info[202]));
                    if (!info[203].isEmpty())
                        patient.setProcured7(Integer.valueOf(info[203]));
                    if (!info[204].isEmpty())
                        patient.setProcured8(Integer.valueOf(info[204]));
                    if (!info[205].isEmpty())
                        patient.setTransplanted1(Integer.valueOf(info[205]));
                    if (!info[206].isEmpty())
                        patient.setTransplanted2(Integer.valueOf(info[206]));
                    if (!info[207].isEmpty())
                        patient.setTransplanted3(Integer.valueOf(info[207]));
                    if (!info[208].isEmpty())
                        patient.setProcured4(Integer.valueOf(info[208]));
                    if (!info[209].isEmpty())
                        patient.setTransplanted5(Integer.valueOf(info[209]));
                    if (!info[210].isEmpty())
                        patient.setTransplanted6(Integer.valueOf(info[210]));
                    if (!info[211].isEmpty())
                        patient.setTransplanted7(Integer.valueOf(info[211]));
                    if (!info[212].isEmpty())
                        patient.setTransplanted8(Integer.valueOf(info[212]));
                    patient.setFuDate(info[213]);
                    patient.setEmrSurv(info[214]);
                    patient.setEmrSurvDate(info[215]);
                    patient.setObit(info[216]);
                    patient.setObitDate(info[217]);
                    patient.setNdiQuery(info[218]);
                    patient.setNdihit(info[219]);
                    patient.setNdiDateofdeath(info[220]);
                    patient.setNdiAlive(info[221]);
                    if (!info[222].isEmpty())
                        patient.setStudyEnrollments1(Integer.valueOf(info[222]));
                    if (!info[223].isEmpty())
                        patient.setStudyEnrollments2(Integer.valueOf(info[223]));
                    if (!info[224].isEmpty())
                        patient.setStudyEnrollments3(Integer.valueOf(info[224]));
                    if (!info[225].isEmpty())
                        patient.setStudyEnrollments4(Integer.valueOf(info[225]));
                    if (!info[226].isEmpty())
                        patient.setStudyEnrollments5(Integer.valueOf(info[226]));
                    if (!info[227].isEmpty())
                        patient.setStudyEnrollments6(Integer.valueOf(info[227]));
                    if (!info[228].isEmpty())
                        patient.setStudyEnrollments7(Integer.valueOf(info[228]));
                    if (!info[229].isEmpty())
                        patient.setStudyEnrollments8(Integer.valueOf(info[229]));
                    if (!info[230].isEmpty())
                        patient.setStudyEnrollments9(Integer.valueOf(info[230]));
                    if (!info[231].isEmpty())
                        patient.setStudyEnrollments10(Integer.valueOf(info[231]));
                    if (!info[232].isEmpty())
                        patient.setStudyEnrollments11(Integer.valueOf(info[232]));
                    if (!info[233].isEmpty())
                        patient.setStudyEnrollments12(Integer.valueOf(info[233]));
                    if (!info[234].isEmpty())
                        patient.setStudyEnrollments13(Integer.valueOf(info[234]));
                    if (!info[235].isEmpty())
                        patient.setStudyEnrollments14(Integer.valueOf(info[235]));
                    // the second studyenrollments1 as A
                    if (!info[236].isEmpty())
                        patient.setStudyEnrollmentsA(Integer.valueOf(info[236]));
                    patient.setEarlyEegId(info[237]);
                    patient.setNmbId(info[238]);
                    if (!info[239].isEmpty())
                        patient.setCaBiomarkerId(Integer.valueOf(info[239]));
                    patient.setComicaId(info[240]);
                    patient.setAwakeId(info[241]);
                    if (!info[242].isEmpty())
                        patient.setCoreDatasetComplete(Integer.valueOf(info[242]));
                    if (!info[243].isEmpty())
                        patient.setReferralEkg(Integer.valueOf(info[243]));
                    if (!info[244].isEmpty())
                        patient.setReferralPupils(Integer.valueOf(info[244]));
                    if (!info[245].isEmpty())
                        patient.setReferralCroneals(Integer.valueOf(info[245]));
                    if (!info[246].isEmpty())
                        patient.setReferralMotor(Integer.valueOf(info[246]));
                    if (!info[247].isEmpty())
                        patient.setComplete(Integer.valueOf(info[247]));

                    patients.add(patient);
                    line = reader.readLine();
                }
                count+=insertPatients(patients);
                patients.clear();
            }
            result.put("msg","success");
            result.put("num",count);
        }catch (Exception e){
            e.printStackTrace();
            result.put("msg","fail");
            result.put("num",count);
        }
        return result;
    }

    private int insertPatients(List<PatientWithBLOBs> patients) {
        int count=0;
        try {
            for (PatientWithBLOBs p : patients) {
                count+=patientDao.insertPatinet(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
