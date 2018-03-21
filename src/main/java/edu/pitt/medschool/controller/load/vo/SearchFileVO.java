/**
 * 
 */
package edu.pitt.medschool.controller.load.vo;

import java.util.List;

/**
 * @author Isolachine
 *
 */
public class SearchFileVO {
    private String dir;
    private List<String> files;

    /**
     * @return the dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * @param dir
     *            the dir to set
     */
    public void setDir(String dir) {
        this.dir = dir;
    }

    /**
     * @return the files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * @param files
     *            the files to set
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }
}
