import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PdfToTxt {
    private String filePath;

    public PdfToTxt(String filePath) {
        this.filePath = filePath;
    }

    public String getText()  {
        try(RandomAccessFile f = new RandomAccessFile(new File(filePath), "r")){
            PDFParser parser = new PDFParser(f);
            parser.parse();
            return extractAndProcessText(new PDDocument(parser.getDocument()));
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private String extractAndProcessText(PDDocument pdDoc) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        Map<Integer, List<String>> pages = new HashMap<>();
        Map<String, Integer> frequencies = new HashMap<>();
        for (int i = 10; i < 20; i++) {
//            for (int i = 0; i < pdDoc.getNumberOfPages(); i++) {
            pdfStripper.setStartPage(i);
            pdfStripper.setEndPage(i+1);
            String page = pdfStripper.getText(pdDoc);
            processPage(i, page, pages, frequencies);
        }

        StringBuilder sb = new StringBuilder();
        for (List<String> lines : pages.values()) {
            for (String line : lines) {
                sb.append(line).append("\r\n");
            }
        }

        return sb.toString();
    }

    public String getTextByArea()  {
        try(RandomAccessFile f = new RandomAccessFile(new File(filePath), "r")){
            PDFParser parser = new PDFParser(f);
            parser.parse();
            return extractTextByArea(new PDDocument(parser.getDocument()), 22);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }



    private void processPage(Integer i, String page, Map<Integer, List<String>> pages, Map<String, Integer> frequencies) {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(page);
        List<String> lines = new ArrayList<>();
        List<String> regexp = createRegex();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            LineMatcher lineMatcher = checkLineMatches(regexp, line, i);
            if(lineMatcher==null){
                lines.add(line);
                continue;
            }
            Integer o = frequencies.get(lineMatcher.getRegexp());
            int f = o==null? 0:o;
            if(f>0){
                frequencies.put(lineMatcher.getRegexp(), ++f);
            }else{
                frequencies.put(lineMatcher.getRegexp(), 1);
            }
        }
        pages.put(i, lines);
    }

    private LineMatcher checkLineMatches(List<String> regexps, String line, int pageNr) {
        for (String regexp : regexps) {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                return new LineMatcher(line, regexp, matcher.group(), 1, pageNr);
            }
        }
        return null;
    }

    private String extractText(PDDocument pdDoc) throws IOException {
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setStartPage(44);
        pdfStripper.setEndPage(46);
        return pdfStripper.getText(pdDoc);
    }

    private List<String> createRegex() {
        List<String> regexList = new ArrayList<>();
        // 415
        String titlePattern1 = "([1-9][0-9]*)";

        // Index Test Test 415
        String titlePattern2 = "(([A-Z][A-Za-z]* )+)([1-9][0-9]*)";

        // 415 Index Test
        String titlePattern3 = "([1-9][0-9]*)(( [A-Z][A-Za-z]*)+)";

        // 4.1.5.6 Index Test
        String titlePattern4 = "([1-9]((\\.)[0-9]*)+)(( [A-Z][A-Za-z]*)+)";

        // 415   4.1.5.6 Index Test Test
        String titlePattern5 = "([1-9][0-9]*)( [1-9]((\\.)[0-9]*)+)(( [A-Z][A-Za-z]*)+)";

        // 4.1.5.6 Index Test Test 415
        String titlePattern6 = "([1-9]((\\.)[0-9]*)+)(( [A-Z][A-Za-z]*)+)( [1-9][0-9]*)";

        // Index test
        String titlePattern7 = "([A-Z][a-z]* *)([a-zA-Z ]*)";

        // Index test test 231
        String titlePattern8 = "([A-Z][a-z]* *)([a-zA-Z ]*)([1-9][0-9]*)";

        // 231 Index test test
        String titlePattern9 = "([1-9][0-9]* *)([A-Z][a-z]* *)([a-zA-Z ]*)";

        // 4.1.5.6 Index test test
        String titlePattern10 = "([1-9]((\\.)[0-9]*)+)( [A-Z][a-z]*)( [a-zA-Z ]*)";

        // 415 4.1.5.6 Index test test
        String titlePattern11 = "([1-9][0-9]* *)([1-9]((\\.)[0-9]*)+)( [A-Z][a-z]*)( [a-zA-Z ]*)";

        // 4.1.5.6 Index test test 415
        String titlePattern12 = "([1-9]((\\.)[0-9]*)+)( [A-Z][a-z]* *)([a-zA-Z ]*)( [1-9][0-9]*)";

        // 234) test test
        String titlePattern13 = "([1-9][0-9]*\\))( *[a-zA-Z ]*)";


        regexList.add(titlePattern1);
        regexList.add(titlePattern2);
        regexList.add(titlePattern3);
        regexList.add(titlePattern4);
        regexList.add(titlePattern5);
        regexList.add(titlePattern6);
        regexList.add(titlePattern7);
        regexList.add(titlePattern8);
        regexList.add(titlePattern9);
        regexList.add(titlePattern10);
        regexList.add(titlePattern11);
        regexList.add(titlePattern12);
        regexList.add(titlePattern13);

        return regexList;
    }

    public String extractTextByArea(PDDocument pdDoc, int pageNumber) throws IOException {
        Rectangle2D region = new Rectangle2D.Double(0, 0, 791, 312);//header 70 height
        PDFTextStripperByArea stripper;
        PDPage page = pdDoc.getPage(pageNumber);
        stripper = new PDFTextStripperByArea();
        stripper.addRegion("region", region);
        stripper.extractRegions(page);
        String text = stripper.getTextForRegion("region").trim();
        return text;
    }


    public static void main(String[] args) throws IOException {
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("pdf\\txtFromPdf.txt"), StandardCharsets.UTF_8))) {
            PdfToTxt pdfManager = new PdfToTxt("pdf\\CleanCode.pdf");
            String text = pdfManager.getText();
            System.out.println(text);
            writer.write(text);
        } catch (IOException ex) {
            Logger.getLogger(PdfToTxt.class.getName()).log(Level.SEVERE, "error", ex);
        }
    }
}

