package com.softwaremarket.autoupgrade.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.io.*;

public class Base64Util {

    // Base64 编码与解码
    private static final Base64.Decoder DECODER_64 = Base64.getDecoder();
    private static final Base64.Encoder ENCODER_64 = Base64.getEncoder();

    // 编码、解码格式
    private static final String CODE_FORMATE = "UTF-8";

    // dpi越大转换后的图片越清晰，相对转换速度越慢
    private static final Integer DPI = 200;

    /**
     * 1、文件（图片、pdf） 转 Base64字符串
     *
     * @param file 需要转Base64的文件
     * @return Base64 字符串
     * 已测试
     */
    public static String fileToBase64Str(File file) throws IOException {
        String base64Str = null;
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;
        try {
            fin = new FileInputStream(file);
            bin = new BufferedInputStream(fin);
            baos = new ByteArrayOutputStream();
            bout = new BufferedOutputStream(baos);
            // io
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while (len != -1) {
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            // 刷新此输出流，强制写出所有缓冲的输出字节
            bout.flush();
            byte[] bytes = baos.toByteArray();
            // Base64字符编码
            base64Str = ENCODER_64.encodeToString(bytes).trim();
        } catch (IOException e) {
            e.getMessage();
        } finally {
            try {
                fin.close();
                bin.close();
                bout.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }
        return base64Str;
    }


    /**
     * 2、Base64字符串 转 文件（图片、pdf） -- 多用于测试
     *
     * @param base64Content Base64 字符串
     * @param filePath      存放路径
     *                      已测试
     */
    public static void base64ContentToFile(String base64Content, String filePath) throws IOException {
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            // Base64解码到字符数组
            byte[] bytes = DECODER_64.decode(base64Content);
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);
            bis = new BufferedInputStream(byteInputStream);
            File file = new File(filePath);
            File path = file.getParentFile();
            if (!path.exists()) {
                path.mkdirs();
            }
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            // io
            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length != -1) {
                bos.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            // 刷新此输出流，强制写出所有缓冲的输出字节
            bos.flush();
        } catch (IOException e) {
            e.getMessage();
        } finally {
            try {
                bis.close();
                fos.close();
                bos.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }
    }


    /**
     * 3、text明文 转 Base64字符串
     *
     * @param text 明文
     * @return Base64 字符串
     * 已测试
     */
    public static String textToBase64Str(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String encodedToStr = null;
        try {
            encodedToStr = ENCODER_64.encodeToString(text.getBytes(CODE_FORMATE));
        } catch (UnsupportedEncodingException e) {
            e.getMessage();
        }
        return encodedToStr;
    }


}