package edu.pitt.medschool.model.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.ImportedFile;
import edu.pitt.medschool.model.dto.ImportedFileExample;
import edu.pitt.medschool.model.dto.ImportedFileExample.Criteria;
import edu.pitt.medschool.model.mapper.ImportedFileMapper;

/**
 * Imported file
 */
@Repository
public class ImportedFileDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ImportedFileMapper importedFileMapper;

    @Value("${machine}")
    private String machineId;

    @Transactional(rollbackFor = Exception.class)
    public int insert(ImportedFile i) throws Exception {
        return importedFileMapper.insertSelective(i);
    }

    /**
     * Check based on filename and filesize(bytes)
     */
    public boolean checkHasImported(String uuid, String filename, long size) {
        ImportedFileExample ife = new ImportedFileExample();
        ife.createCriteria().andUuidEqualTo(uuid).andFilenameLikeInsensitive(filename).andFilesizeEqualTo(size)
                .andDeletedEqualTo(false);
        return importedFileMapper.selectByExample(ife).size() > 0;
    }

    /**
     * Get all imported PIDs
     * 
     * @param uuid Machine ID
     */
    public List<String> getAllImportedPid(String uuid) {
        return importedFileMapper.selectAllImportedPid(uuid);
    }

    /**
     * @return
     */
    public List<String> selectAllImportedPidPSC() {
        return importedFileMapper.selectAllImportedPidPSC();
    }

    public List<String> selectAllImportedPidOnMachine(String machineId) {
        return importedFileMapper.selectAllImportedPidOnMachine(machineId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deletePatientDataByFile(CsvFile file) throws Exception {
        ImportedFileExample importedFileExample = new ImportedFileExample();
        Criteria criteria = importedFileExample.createCriteria();
        criteria.andPidEqualTo(file.getPid());
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andIsarEqualTo(file.getAr());
        criteria.andUuidEqualTo(machineId);

        ImportedFile importedFile = new ImportedFile();
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        importedFile.setDeleted(true);
        importedFile.setDeleteTime(americaDateTime);

        return importedFileMapper.updateByExampleSelective(importedFile, importedFileExample);
    }

    public List<ImportedFile> selectByFileNameDeleted(ImportedFile file){
        ImportedFileExample importedFileExample = new ImportedFileExample();
        Criteria criteria = importedFileExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andDeletedEqualTo(false);
        return importedFileMapper.selectByExample(importedFileExample);
    }

    public int updateImportedSize(ImportedFile file){
        ImportedFileExample importedFileExample = new ImportedFileExample();
        Criteria criteria = importedFileExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andDeletedEqualTo(false);
        ImportedFile newFile = new ImportedFile();
        newFile.setFilesize(file.getFilesize());
        newFile.setTimestamp(file.getTimestamp());
        return importedFileMapper.updateByExampleSelective(file,importedFileExample);
    }

}
