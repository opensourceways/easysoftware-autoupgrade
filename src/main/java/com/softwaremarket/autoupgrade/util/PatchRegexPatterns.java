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

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.softwaremarket.autoupgrade.config.RexConfig;

import jakarta.annotation.PostConstruct;

import java.util.regex.Matcher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class PatchRegexPatterns {
    /**
     * logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PatchRegexPatterns.class);
    /**
     * rexConfig.
     */
    @Autowired
    private RexConfig rexConfig;

    /**
     * rex patters list.
     */
    private List<Pattern> rexPatters;

    @PostConstruct
    public void init() {
        List<String> rexList = rexConfig.getRexlist();
        rexPatters = new ArrayList<>();
        for (String rex : rexList) {
            Pattern pattern = Pattern.compile(rex, Pattern.DOTALL);
            rexPatters.add(pattern);
        }
    }

    private boolean IsMatchChainRex(String input) {
        for (Pattern pattern : rexPatters) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public List<String> fetchCommitIdFromPatchFile(String filePath) {

        List<String> res = new ArrayList<>();
        try {
            // 读取文件的所有行到一个List中  
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 对每行数据执行正则
            for (String line : lines) {
                boolean isCommit = IsMatchChainRex(line);
                if (isCommit) {
                    res.add(line);
                }

            }
        } catch (IOException e) {
            // 处理文件读取错误  
            LOGGER.error("can not open file");
        }
        return res;
    }

}
