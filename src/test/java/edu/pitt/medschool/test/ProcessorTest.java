/**
 * 
 */
package edu.pitt.medschool.test;

/**
 * @author Isolachine
 *
 */
public class ProcessorTest {
    public static void main(String[] args) {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println(cores);
    }
}
