package org.nmdp;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Created by wwang. Updated by rsajulga-nmdp 11/10/2020
 */
public class Run {
    public static void main(String[] args) {
        if(args.length == 0){
            System.out.print("The folder name is missing.");
            return;
        }
        File folder = new File(args[0]);
        File output = new File(args[1]);
        File[] inputList = folder.listFiles();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        QaCalculator qc = new QaCalculator(folder.getName());
        System.out.println("Parsing through " + inputList.length + " files.");
        for (int i = 0; i < inputList.length; i++) {
             if (i % 1000 == 0) {
                System.out.println(i);
             }
             System.out.print('.');
            String fileNameFull = inputList[i].getName().toLowerCase();
            if (fileNameFull.contains("hml") || fileNameFull.contains("xml") || fileNameFull.contains("txt")) {
                try {
                    qc.run(inputList[i], pw);
                } catch (IOException | SAXException |ParserConfigurationException e) {
                    System.out.println("Fail to parse file: " + fileNameFull);
                }
            }
        }
        pw.close();
        System.out.println("Finished!");
        //System.out.println("TE: "+ (int)qc.count);
        //System.out.println("LR: "+ (qc.total- (int)qc.count));
        //System.out.println("TE Rate: "+qc.getQa());
    }

}
