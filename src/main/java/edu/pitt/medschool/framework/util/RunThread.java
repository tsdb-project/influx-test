package edu.pitt.medschool.framework.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RunThread extends Thread
{
    InputStream is;
    String printType;

    public RunThread(InputStream is, String printType)
    {
        this.is = is;
        this.printType = printType;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(this.printType + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
