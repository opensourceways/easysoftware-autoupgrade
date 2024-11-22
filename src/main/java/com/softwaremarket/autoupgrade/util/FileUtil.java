/* Copyright (c) 2024 openEuler Community
 EasySoftwareInput is licensed under the Mulan PSL v2.
 You can use this software according to the terms and conditions of the Mulan PSL v2.
 You may obtain a copy of Mulan PSL v2 at:
     http://license.coscl.org.cn/MulanPSL2
 THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 See the Mulan PSL v2 for more details.
*/

package com.softwaremarket.autoupgrade.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public final class FileUtil {
    final static String CHARSET_NAME = "UTF-8";

    // Private constructor to prevent instantiation of the utility class
    private FileUtil() {
        // private constructor to hide the implicit public one
        throw new AssertionError("ClientUtil class cannot be instantiated.");
    }

    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    /**
     * list sub menus.
     *
     * @param dir dir.
     * @return sub menus.
     */
    public static List<String> listSubMenus(String dir) {
        if (StringUtils.isBlank(dir)) {
            LOGGER.error("no dir: {}", dir);
            return Collections.emptyList();
        }

        File f = new File(dir);
        if (!f.isDirectory()) {
            LOGGER.error("no dir: {}", dir);
            return Collections.emptyList();
        }

        File[] files = f.listFiles();
        if (files == null || files.length == 0) {
            LOGGER.error("no file in dir: {}", dir);
            return Collections.emptyList();
        }

        List<String> fileList = new ArrayList<>();
        for (File fi : files) {
            String fp = validFile(fi, dir);
            if (!StringUtils.isBlank(fp)) {
                fileList.add(fp);
            }
        }
        return fileList;
    }

    /**
     * if the repo does not exist, mkdir.
     *
     * @param repo repo.
     */
    public static void mkdirIfUnexist(File repo) {
        if (repo.exists() && repo.isDirectory()) {
            return;
        }

        try {
            FileUtils.mkdirs(repo, true);
        } catch (IOException e) {
            LOGGER.error("fail to create dir: {}", repo.toString());
        }
    }

    /**
     * valid the file.
     *
     * @param f   file.
     * @param dir menu.
     * @return the valid file.
     */
    public static String validFile(File f, String dir) {
        String filePath;
        try {
            filePath = f.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("fail to valid file: {}", f.toString());
            return null;
        }

        if (!filePath.startsWith(dir)) {
            LOGGER.error("unpermitted file path: {}", f.toString());
            return null;
        }
        return filePath;
    }

    /**
     * parse file name.
     *
     * @param filePath filename.
     * @return strign array..
     */
    public static String[] parseFileName(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return new String[0];
        }
        String[] pathSplits = filePath.split(Pattern.quote(File.separator));
        String filename = pathSplits[pathSplits.length - 1];
        return filename.split("_a_");
    }

    /**
     * extract file name without extension.
     *
     * @param filePath filepath.
     * @return list of file piece.
     */
    public static String[] extractFileNameWithoutExt(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        String[] splits = FileUtil.parseFileName(filePath);
        if (splits == null || splits.length == 0) {
            return null;
        }
        return Arrays.copyOfRange(splits, 0, splits.length - 1);
    }

    /**
     * delete files.
     *
     * @param files files.
     * @return return true if delete all files.
     */
    public static boolean deleteFiles(List<String> files) {
        if (files == null || files.isEmpty()) {
            return true;
        }
        return files.stream().map(FileUtil::deleteFile).allMatch(
                s -> Boolean.TRUE.equals(s)
        );
    }

    /**
     * delete file.
     *
     * @param file file name.
     * @return boolean.
     */
    public static boolean deleteFile(String file) {
        if (StringUtils.isBlank(file)) {
            return true;
        }
        File myFile = new File(file);
        return myFile.delete();
    }

    public static List<String> getFileContetList(String filePath) {
        List<String> content = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), CHARSET_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    public static boolean downloadFile(String fileURL, String savePath) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(fileURL).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(savePath);
             ReadableByteChannel readableByteChannel = Channels.newChannel(in)) {

            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            return Boolean.TRUE;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }


    public static String downloadFileToString(String fileURL) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(fileURL);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }


}
