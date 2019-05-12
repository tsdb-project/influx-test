package edu.pitt.medschool.model.dao;

import java.time.LocalDateTime;
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
        ife.createCriteria().andUuidEqualTo(uuid).andFilenameLikeInsensitive(filename).andFilesizeEqualTo(size);
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
        criteria.andUuidEqualTo(machineId);
        criteria.andDeletedEqualTo(false);

        ImportedFile importedFile = new ImportedFile();
        importedFile.setDeleted(true);
        importedFile.setDeleteTime(LocalDateTime.now());

        int deleteResult = importedFileMapper.updateByExampleSelective(importedFile, importedFileExample);
        try {
            if (deleteResult == 0) {
                // throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No imported file record available!");
            throw e;
        }
        return deleteResult;
    }

}
