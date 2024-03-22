package com.example.demo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

interface DealerConstants{
    String SHIPPING_DOCUMENT = "Shipping List";
    String PACKING_DOCUMENT = "PACKING_DOCUMENT";

}

@Slf4j
@Service
public class SamplePdf {
    public ByteArrayInputStream printPackingModulePdf(List<PrintPackingModuleResp> printPackingModuleRespList, PrintPackingModuleReq req, String module) throws IOException {

        PDDocument document = new PDDocument();
        String moduleHeader = null;
        String firstCellHeader = null;
        String mode = null;
        Integer totalQty = 0;
        Double totalApproxNetWt = 0.0;

        if(module == DealerConstants.SHIPPING_DOCUMENT){
            moduleHeader = DealerConstants.SHIPPING_DOCUMENT;
            firstCellHeader = "Packing Module";
            mode = "SHP-"+req.getShippingDocId();
        } else if (module == DealerConstants.PACKING_DOCUMENT) {
            moduleHeader = DealerConstants.PACKING_DOCUMENT;
            firstCellHeader = "TAG No.";
            mode = req.getPackingModuleType() + " - " + req.getPackingModuleId();
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formatDateTime = LocalDate.now().format(formatter);
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        String PackingIdAndTypeBarccode = mode;
        int pageHeight =(int) page.getMediaBox().getHeight();
        int pageWidth=(int) page.getMediaBox().getWidth();

        //start of top section of page
        PDPageContentStream contentStream  =new PDPageContentStream(document,page);
        BufferedImage barcodeImage = generateBarcodeImage(PackingIdAndTypeBarccode);
        contentStream.drawImage(PDImageXObject.createFromByteArray(document, toByteArray(barcodeImage), PackingIdAndTypeBarccode), 180, 780, 120, 30);
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 790);
        contentStream.showText(moduleHeader);
        contentStream.endText();
        contentStream.setFont(font, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 780);
        contentStream.showText("( " + req.getShippingType() + " )");
        contentStream.endText();
        contentStream.setFont(font, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(200, 770);
        contentStream.showText(mode);
        contentStream.endText();
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(350, 790);
        contentStream.showText(req.getCountryCode() + " - " + req.getDealerCode());
        contentStream.endText();
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(480, 790);
        contentStream.showText(formatDateTime);
        contentStream.endText();
        //end of top section of page

        //start of table
        int initX =30;
        int initY = pageHeight-120;
        int cellHeight = 48;
        int cellWidth=78;
        // float[] cellWidth = new float[]{100f, 150f, 200f, 250f, 300f};
        int colCount=7;
        int rowCount = printPackingModuleRespList.size();
        int totalHtOfTable = cellHeight * (rowCount + 1); // +1 is for 1st header row
        Color tableHeaderColor = new Color(0,0,0);
        Color tableBorderColor = new Color(0,0,0);
        for (int i = 0; i <= rowCount; i++) { //row left to right
            for(int j=0;j<colCount;j++){
                //start of new logic
                //create cell content, top down so reduce cellHeight
                contentStream.addRect(initX, initY, cellWidth, -cellHeight);
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                if(i==0 && j==0) {//header column 1
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+2, initY-cellHeight+22);
                    contentStream.showText(firstCellHeader);
                    contentStream.endText();
                }else if(i==0 && j==1) {//header column 2
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+16, initY-cellHeight+10);
                    contentStream.showText("TU");
                    contentStream.endText();
                }else if(i==0 && j==2) {//header column 3
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+16, initY-cellHeight+10);
                    contentStream.showText("EC");
                    contentStream.endText();
                }else if(i==0 && j==3) {//header column 4
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+2, initY-cellHeight+22);
                    contentStream.showText("Approx");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+2, initY-cellHeight+6);
                    contentStream.showText("new weight");
                    contentStream.endText();
                }else if(i==0 && j==4) {//header column 5
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+16, initY-cellHeight+10);
                    contentStream.showText("Qty");
                    contentStream.endText();
                }else if(i==0 && j==5) {//header column 6
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+2, initY-cellHeight+22);
                    contentStream.showText("Subtotal");
                    contentStream.endText();
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX+2, initY-cellHeight+6);
                    contentStream.showText("weight");
                    contentStream.endText();
                }else if(i==0 && j==6) {//header column 7
                    contentStream.setNonStrokingColor(tableHeaderColor);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(initX + 2, initY - cellHeight + 10);
                    contentStream.showText("Description");
                    contentStream.endText();
                }

                //End of Header creation
                initX += cellWidth;//equally increase the width of each cell
                if(i != 0){ //ignore the 1st row as it's the header
                    contentStream.setNonStrokingColor(tableBorderColor);
                    int k = i; //store the row number and then decrease by 1 so that it matches the index count of datalist
                    k = k - 1;
                    if(j==0){ //column 1 data
                        PDImageXObject imageXObject = PDImageXObject.createFromFile(sampleBarCodeMth(generateBarcodeImage(printPackingModuleRespList.get(k).getPackingModule())),document);
                        contentStream.drawImage(imageXObject, initX + 4 - cellWidth, initY - cellHeight + 20, 60, 25);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 6 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(printPackingModuleRespList.get(k).getPackingModule());
                        contentStream.endText();
                    } else if (j == 1) { //column 2 data
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(printPackingModuleRespList.get(k).getTrackingUnit());
                        contentStream.endText();
                    }else if (j == 2) { //column 3 data
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(printPackingModuleRespList.get(k).getEc());
                        contentStream.endText();
                    }else if (j == 3) { //column 4 data
                        totalApproxNetWt = totalApproxNetWt + Double.valueOf(printPackingModuleRespList.get(k).getAproxNetWeight());
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(printPackingModuleRespList.get(k).getAproxNetWeight());
                        contentStream.endText();
                    }else if (j == 4) { //column 5 data
                        totalQty = totalQty + Integer.valueOf(printPackingModuleRespList.get(k).getQuantity());
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(String.valueOf(printPackingModuleRespList.get(k).getQuantity()));
                        contentStream.endText();
                    }else if (j == 5) { //column 6 data
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(String.valueOf(printPackingModuleRespList.get(k).getSubTotalWeight()));
                        contentStream.endText();
                    }else if (j == 6) { //column 7 data
                        contentStream.beginText();
                        contentStream.newLineAtOffset(initX + 2 - cellWidth, initY - cellHeight + 10);
                        contentStream.showText(printPackingModuleRespList.get(k).getDesc());
                        contentStream.endText();
                    }
                }
                //System.out.println("initx "+initX);
                //System.out.println("inity "+initY);
            }
            //System.out.println("====================================");
            //for next row set X position to initial, and reduce the height
            initX = 30;
            initY -= cellHeight;
        }

        //start of bottom section of page
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(initX,  pageHeight -130 -totalHtOfTable - 10);
        contentStream.showText("Grand total quantity: "+totalQty);
        contentStream.endText();
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(initX, pageHeight - 130 -totalHtOfTable - 28);
        contentStream.showText("Total net weight(approx): "+totalApproxNetWt+ " kg");
        contentStream.endText();
        contentStream.setFont(font, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(initX, pageHeight - 130 -totalHtOfTable - 46);
        contentStream.showText("Packaging Unit(s) taken over on________________________from________________________");
        contentStream.endText();
        //end of bottom section of page

        contentStream.stroke();
        contentStream.close();

        document.save("demo.pdf");
        document.close();

        System.out.println("PDF Generated");
        //document.save(outputStream);
        //document.close();

        return new ByteArrayInputStream(new ByteArrayOutputStream().toByteArray());
    }

    private BufferedImage generateBarcodeImage(String PackingIdAndTypeBarccode) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(PackingIdAndTypeBarccode, BarcodeFormat.CODE_128, 800, 200);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            log.debug("An error occurred:");
            log.debug(e.toString());
            return null;
        }
    }

    private byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
    private String sampleBarCodeMth(BufferedImage image) throws IOException {
        Path tempFilePath = Files.createTempFile("tempImage", ".png");
        File tempFile = tempFilePath.toFile();

        // Write the image to the temporary file
        ImageIO.write(image, "png", tempFile);

        // Delete the file on JVM exit
        tempFile.deleteOnExit();
        //System.out.println("aaaaaaaaaaaa " +tempFile.getAbsoluteFile().toString());
        return tempFile.getAbsoluteFile().toString();
    }

    public static void main(String[] args) throws IOException {
        List<PrintPackingModuleResp> printPackingModuleRespList = new ArrayList<>();
        PrintPackingModuleResp ppmr = new PrintPackingModuleResp();
        ppmr.setPackingModule("bcbcd");
        ppmr.setTrackingUnit("dhff");
        ppmr.setEc("ajc");
        ppmr.setAproxNetWeight("879");
        ppmr.setTrackingUnit("97");
        ppmr.setDesc("asjcba");
        ppmr.setSubTotalWeight("838");
        ppmr.setQuantity("676");
        printPackingModuleRespList.add(ppmr);

        ppmr = new PrintPackingModuleResp();
        ppmr.setPackingModule("2bcbc");
        ppmr.setTrackingUnit("2dhff");
        ppmr.setEc("2ajc");
        ppmr.setAproxNetWeight("2879");
        ppmr.setTrackingUnit("297");
        ppmr.setDesc("2asjcba");
        ppmr.setSubTotalWeight("2838");
        ppmr.setQuantity("2676");
        printPackingModuleRespList.add(ppmr);

        ppmr = new PrintPackingModuleResp();
        ppmr.setPackingModule("2bcbc");
        ppmr.setTrackingUnit("2dhff");
        ppmr.setEc("2ajc");
        ppmr.setAproxNetWeight("2879");
        ppmr.setTrackingUnit("297");
        ppmr.setDesc("2asjcba");
        ppmr.setSubTotalWeight("2838");
        ppmr.setQuantity("2676");
        printPackingModuleRespList.add(ppmr);

        ppmr = new PrintPackingModuleResp();
        ppmr.setPackingModule("2bcbc");
        ppmr.setTrackingUnit("2dhff");
        ppmr.setEc("2ajc");
        ppmr.setAproxNetWeight("2879");
        ppmr.setTrackingUnit("297");
        ppmr.setDesc("2asjcba");
        ppmr.setSubTotalWeight("2838");
        ppmr.setQuantity("2676");
        printPackingModuleRespList.add(ppmr);

        PrintPackingModuleReq req = new PrintPackingModuleReq();
        req.setCountryCode("DEU");
        req.setShippingDocId("1770991");
        req.setShippingType("Dirty Core");
        req.setDealerCode("1M180");
        req.setPackingModuleId("P868");
        req.setPackingModuleType(DealerConstants.SHIPPING_DOCUMENT);

        SamplePdf samplePdf = new SamplePdf();
        samplePdf.printPackingModulePdf(printPackingModuleRespList, req, DealerConstants.SHIPPING_DOCUMENT);
    }
}
