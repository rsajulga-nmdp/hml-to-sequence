package org.nmdp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;



/**
 * Created by wwang on 8/28/16.
 */
public class QaCalculator {
    public static final int long_length = 2500;
    public double count;
    public int total;
    private PrintWriter pw;
    private static final String HML = "hml";
    private static final String SAMPLE = "sample";
    private static final String ID = "id";
    private static final String TYPING = "typing";
    private static final String SEQUENCE_BLOCK = "consensus-sequence-block";
    private static final String SEQUENCE = "sequence";
    private static final String GLString = "glstring";
    private static final String SEQ_ID= "reference-sequence-id";
    private static final String PHASE_SET = "phase-set";
    private static final String HAPLOID = "haploid";
    private static final String HAPLOID_TYPE = "type";
    private static final String HAPLOID_Locus = "locus";
    private String folderName;
    private String projectName;
    private StringBuilder nameSpace;

    public QaCalculator(String folderName){
        this.folderName = folderName;
    }


    public void run(File input, PrintWriter pw) throws IOException, SAXException, ParserConfigurationException {
        this.pw = pw;

        // Initialize doc
        Document doc = getDoc(input);
        
        // Check HML tag, obtain namespace prefix 
        NodeList hmlTag = doc.getElementsByTagNameNS("http://schemas.nmdp.org/spec/hml/1.0.1", HML);
        nameSpace = new StringBuilder(
                doc.getDocumentElement().getPrefix() != null ? doc.getDocumentElement().getPrefix() + ":" : "");
        
        // Obtain project name
        if (hmlTag.getLength() != 1) {
        	System.out.print("The file does not contain only one " + HML + " tag.");
        }
        this.projectName = getAttribute((Element) hmlTag.item(0), "project-name");

        // Get all sample nodes
        NodeList sampleList = doc.getElementsByTagName(nameSpace + SAMPLE);
        for (int i = 0; i < sampleList.getLength(); i++) {
            parseSample(sampleList.item(i));
        }
    }
    
    /**
     * Returns the text value of the provided attribute.
     *
     * @param el The element containing the provided attribute.
     * @param attribute The provided attribute.
     */
    private String getAttribute(Element el, String attribute) {
    	return el.getAttribute(attribute);
    }

    /**
     * Parse the sample node.
     *
     * @param node The sample node.
     */
    private void parseSample(Node node) {
        Element sample = (Element) node;
        String sampleID = sample.getAttribute(ID);
        // Get all typing nodes
        NodeList typingList = sample.getElementsByTagName(nameSpace + TYPING);
        for (int j = 0; j < typingList.getLength(); j++) {
            parseTyping(typingList.item(j), sampleID);
        }
    }

    /**
     * Parse the typing node.
     *
     * @param hla The typing node.
     */
    public void parseTyping(Node hla, String sampleID) {
        Element element = (Element) hla;
        if(hasGls(element)){
            //phase gls
            parseGls(element, sampleID);
        }else {
            //phase haploid
            parseHap(element, sampleID);
        }

    }

    private void parseGls(Element element, String sampleID) {
        if(twoGls(element)){
            String [] glsArray = getGlsArray(getGLString(element));
            printFirstGls(glsArray, element, sampleID);
            printSecondGls(glsArray, element, sampleID);
        }else {
            //one gl
        	printHeader(sampleID);
        	
            pw.print("alleleOne");
            pw.print(",");

            pw.print(getGLString(element));

            pw.print(",");
            printGls(element);
            pw.print(",");

            String sequence;
            NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
            if(sequenceList.getLength() == 1){
                Element seq = (Element) sequenceList.item(0);
                sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
            }else {
                sequence = printSequence(element, 0, sequenceList.getLength(), ",");
            }
            pw.println(sequence);
        }
    }



    private String[] getGlsArray(String gls) {
        if (!gls.contains("+")) {
            throw new RuntimeException("Can not get gls array if there is no plus sign");
        }
        String[] glsArray = new String[2];
        if (gls.contains("|")) {
            String[] hlaList = gls.split("\\|");
            StringBuilder hla0 = new StringBuilder();
            StringBuilder hla1 = new StringBuilder();
            for (int j = 0; j < hlaList.length; j++) {
                String[] subHla = hlaList[j].split("\\+");
                //Do not add duplicate hla string
                if (!hla0.toString().contains(subHla[0])) {
                    if (hla0.length() != 0) {
                        hla0.append("/");
                    }
                    hla0.append(subHla[0]);

                }

                //Do not add duplicate hla string
                if (!hla1.toString().contains(subHla[1])) {
                    if (hla1.length() != 0) {
                        hla1.append("/");
                    }
                    hla1.append(subHla[1]);
                }
            }
            glsArray[0] = hla0.toString();
            glsArray[1] = hla1.toString();
        }else{
            glsArray = gls.split("\\+");
        }
        return glsArray;
    }

    private boolean twoGls(Element element){
        NodeList glsElement = element.getElementsByTagName(nameSpace + GLString);
        String gls = "";
        if(glsElement.item(0) != null){
            gls = glsElement.item(0).getTextContent().trim();
        }
        return gls.contains("+");
    }


    private String getGLString(Element element){
        String gls = "";
        NodeList glsElement = element.getElementsByTagName(nameSpace + GLString);
        if(glsElement.item(0) != null){
            gls = glsElement.item(0).getTextContent().trim();
        }
        return gls;
    }
    private void printFirstGls(String[] gls, Element element, String sampleID){
    	printHeader(sampleID);

        if(gls[0].equals(gls[1])){
            pw.print("Homozygous1");
            pw.print(",");
        }else {
            pw.print("allele1");
            pw.print(",");
        }

        pw.print(gls[0]);

        pw.print(",");
        printGls(element);
        pw.print(",");

        String sequence;
        NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
        if(gls[0].equals(gls[1])){
            sequence = printSequence(element, 0, sequenceList.getLength(), ",");
        }else if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
        }else {
            sequence = printSequence(element, 0, sequenceList.getLength()/2, ",");
        }

        pw.println(sequence);

    }

    private void printSecondGls(String[] gls, Element element, String sampleID) {
    	printHeader(sampleID);

        if(gls[0].equals(gls[1])){
            pw.print("Homozygous2");
            pw.print(",");
        }else {
            pw.print("allele2");
            pw.print(",");
        }

        pw.print(gls[1]);
        
        pw.print(",");
        printGls(element);
        pw.print(",");

        String sequence;
        NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
        if(gls[0].equals(gls[1])){
            sequence = printSequence(element, 0, sequenceList.getLength(), ",");
        }else if (sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
        }else {
            sequence = printSequence(element, sequenceList.getLength()/2, sequenceList.getLength(), ",");
        }

        pw.println(sequence);
    }
    
    private void printHeader(String sampleID) {
        pw.print(folderName);
        pw.print(",");
        pw.print(projectName);
        pw.print(",");
        pw.print(sampleID);
        pw.print(",");
    }

    private void parseHap(Element element, String sampleID) {
        if(twoHap(element)){
            String [] hapArray = getHapArray(element);
            printFirstHap(hapArray, element, sampleID);
            printSecondHap(hapArray, element, sampleID);
        }else {
            //one haploid
        	printHeader(sampleID);

            pw.print("alleleOne");
            pw.print(",");

            NodeList hapElement = element.getElementsByTagName(nameSpace + HAPLOID);
            //There is one haploid
            if (hapElement.getLength() > 0) {
                Element hla = (Element) hapElement.item(0);
                String haploid = hla.getAttribute(HAPLOID_Locus) + "*" + hla.getAttribute(HAPLOID_TYPE);
                pw.print(haploid);
                pw.print(",");
                printGls(element);
                pw.print(",");
            }

            String sequence ;
            NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
            if(sequenceList.getLength() == 1){
                Element seq = (Element) sequenceList.item(0);
                sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
            }else {
                sequence = printSequence(element, 0, sequenceList.getLength(), ",");
            }
            pw.println(sequence);
        }
    }

    private void printSecondHap(String[] hapArray, Element element, String sampleID) {
    	printHeader(sampleID);

        pw.print("allele2");
        pw.print(",");
        pw.print(hapArray[1]);
        pw.print(",");
        printGls(element);
        pw.print(",");

        String sequence;
        NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
        }else {
            sequence = printSequence(element, sequenceList.getLength()/2, sequenceList.getLength(), ",");
        }

        pw.println(sequence);
    }

    private void printFirstHap(String[] hapArray, Element element, String sampleID) {
    	printHeader(sampleID);

        pw.print("allele1");
        pw.print(",");
        pw.print(hapArray[0]);
        pw.print(",");
        printGls(element);
        pw.print(",");

        String sequence;
        NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
        if(sequenceList.getLength() == 1){
            Element seq = (Element) sequenceList.item(0);
            sequence = (seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
        }else {
            sequence = printSequence(element,0, sequenceList.getLength()/2, "," );
        }

        pw.println(sequence);
    }

    /**
     * Print sequence from start index to stop index. The stop index is not included. Each sequence is divided by
     * the divider.
     * @param element
     * @param startIndex
     * @param stopIndex
     * @param divider
     * @return
     */
    private String printSequence(Element element, int startIndex, int stopIndex, String divider){
        StringBuilder sequence = new StringBuilder();
        NodeList  sequenceList = element.getElementsByTagName(nameSpace + SEQUENCE_BLOCK);
        for(int i = startIndex; i < stopIndex; i++){
            Element seq = (Element) sequenceList.item(i);
            sequence.append(seq.getElementsByTagName(nameSpace + SEQUENCE).item(0).getTextContent().trim());
            sequence.append(divider);
         }
         //remove the last divider
        //sequence.deleteCharAt(sequence.length()-1);
         return sequence.toString();
        }

    private String[] getHapArray(Element element) {
        NodeList hapElement = element.getElementsByTagName(nameSpace + HAPLOID);
        //There is two haploids
        Element hla1 = (Element) hapElement.item(0);
        Element hla2 = (Element) hapElement.item(1);
        String[] hap = new String[2];
        hap[0] = hla1.getAttribute(HAPLOID_Locus) + "*" + hla1.getAttribute(HAPLOID_TYPE);
        hap[1] = hla2.getAttribute(HAPLOID_Locus) + "*" + hla2.getAttribute(HAPLOID_TYPE);
        return  hap;
    }

    private boolean twoHap(Element element) {
        NodeList hapElement = element.getElementsByTagName(nameSpace + HAPLOID);
        return hapElement.getLength() == 2;
    }

    private void printGls(Element element) {
        if(hasGls(element)){
            NodeList glsElement = element.getElementsByTagName(nameSpace + GLString);
            String hlaString = "";
            if(glsElement.item(0) != null){
                hlaString = glsElement.item(0).getTextContent().trim();
            }
            pw.print(hlaString);

        }else {
            pw.print("haploid");
        }

    }








    private boolean hasGls(Element element){
        NodeList glsElement = element.getElementsByTagName(nameSpace + GLString);
        return glsElement.item(0) != null;
    }



    private boolean isShort(int l){
        return l< long_length;
    }

    /**
     * Get doc from input file.
     *
     * @return A document.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document getDoc(File input) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(input);
        doc.getDocumentElement().normalize();
        return doc;
    }
}
