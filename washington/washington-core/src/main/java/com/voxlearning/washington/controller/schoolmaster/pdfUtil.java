package com.voxlearning.washington.controller.schoolmaster;


import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class pdfUtil {

    private static String FILEPATH = "D:\\testpdf\\pdf\\";

    public static void imagesToPdf(String fileName, List<byte[]> images ) {
        try {
            fileName = FILEPATH+fileName+".pdf";
            File file = new File(fileName);
            // 第一步：创建一个document对象。
            Document document = new Document();
            document.setMargins(0, 0, 0, 0);
            // 第二步：
            // 创建一个PdfWriter实例，
            PdfWriter.getInstance(document, new FileOutputStream(file));
            // 第三步：打开文档。
            document.open();
            // 第四步：在文档中增加图片。
            for(int i=0;i<images.size();i++){
                Image img = Image.getInstance(images.get(i));
                img.setAlignment(Image.ALIGN_CENTER);
                // 根据图片大小设置页面，一定要先设置页面，再newPage（），否则无效
                document.setPageSize(new Rectangle(img.getWidth(), img.getHeight()));
                document.newPage();
                document.add(img);
            }

            // 第五步：关闭文档。
            document.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        String name = "20001543";
        String imagesPath = "D:\\testpdf\\归档";
        List<byte[]> images = new ArrayList();
        File files = new File(imagesPath);
        for(int i=0; i<files.listFiles().length; i++){
            File tempFile = files.listFiles()[i];
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) tempFile.length());
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(tempFile));
                int buf_size = 1024;
                byte[] buffer = new byte[buf_size];
                int len = 0;
                while (-1 != (len = in.read(buffer, 0, buf_size))) {
                    bos.write(buffer, 0, len);
                }
                images.add(bos.toByteArray());
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        imagesToPdf(name, images);
    }

}
