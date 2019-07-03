package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dto.Version;
import edu.pitt.medschool.model.dto.VersionExample;
import edu.pitt.medschool.model.mapper.VersionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class VersionDao {

    @Autowired
    VersionMapper versionMapper;

    public int setNewVersion(Version version){
        return versionMapper.insert(version);
    }

    public int getLatestVersion(){
        return versionMapper.getlatestID();
    }

    public List<Version> getAllVersions() {
        VersionExample versionExample = new VersionExample();
        VersionExample.Criteria criteria = versionExample.createCriteria();
        return versionMapper.selectByExample(versionExample);
    }

    public Version selectById(int id){
        VersionExample versionExample = new VersionExample();
        VersionExample.Criteria criteria = versionExample.createCriteria();
        criteria.andVersionIdEqualTo(id);
        return versionMapper.selectByExample(versionExample).get(0);
    }

    public int setComment(Version version) {
        VersionExample versionExample = new VersionExample();
        VersionExample.Criteria criteria = versionExample.createCriteria();
        criteria.andVersionIdEqualTo(version.getVersionId());
        Version version1 = new Version();
        version1.setComment(version.getComment());
        return versionMapper.updateByExampleSelective(version1,versionExample);
    }

    public String getVersionCondition(List<PatientTimeLine> files) {
        String query = "fileName=~/";
        for(PatientTimeLine p:files){
            query=query+p.getFilename().replace(".csv","")+"|";
        }
        query=query.substring(0,query.length()-1);
        query=query+"/";
        return query;
    }
}
