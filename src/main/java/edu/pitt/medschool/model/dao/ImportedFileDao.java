package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.ImportedFile;
import edu.pitt.medschool.model.dto.ImportedFileExample;
import edu.pitt.medschool.model.mapper.ImportedFileMapper;

/**
 * Imported file
 */
@Repository
public class ImportedFileDao {

    @Autowired
    ImportedFileMapper ifm;

    @Transactional(rollbackFor = Exception.class)
    public int insert(ImportedFile i) throws Exception {
        return ifm.insertSelective(i);
    }

    /**
     * Check based on filename and filesize(bytes)
     */
    public boolean checkHasImported(String uuid, String filename, long size) {
        ImportedFileExample ife = new ImportedFileExample();
        ife.createCriteria().andUuidEqualTo(uuid).andFilenameLikeInsensitive(filename).andFilesizeEqualTo(size);
        return ifm.selectByExample(ife).size() > 0;
    }

    /**
     * Get all imported PIDs
     * 
     * @param uuid Machine ID
     */
    public List<String> getAllImportedPid(String uuid) {
        return ifm.selectAllImportedPid(uuid);
    }

    /**
     * @return
     */
    public List<String> selectAllImportedPidPSC() {
        return ifm.selectAllImportedPidPSC();
    }

    public List<String> selectAllImportedPidOnMachine(String machineId) {
        return ifm.selectAllImportedPidOnMachine(machineId);
    }

}
