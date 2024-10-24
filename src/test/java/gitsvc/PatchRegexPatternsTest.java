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
package gitsvc;

import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;  
import org.junit.jupiter.api.extension.ExtendWith;  
import org.mockito.InjectMocks;  
import org.mockito.Mock;  
import org.mockito.junit.jupiter.MockitoExtension;  


import com.softwaremarket.autoupgrade.config.RexConfig;
import com.softwaremarket.autoupgrade.util.PatchRegexPatterns;

import java.io.IOException;  
import java.nio.file.Files;  
import java.nio.file.Path;  
import java.util.Arrays;  
import java.util.List;  

  
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;  

@ExtendWith(MockitoExtension.class)  
public class PatchRegexPatternsTest {  
  
    @Mock  
    private RexConfig rexConfig;  

    @InjectMocks  
    private PatchRegexPatterns patchRegexPatterns;  
  
    @BeforeEach  
    public void setUp() {  
        // 设置模拟的 RexConfig 返回值  
        List<String> regexList = Arrays.asList(".*regex1.*", ".*regex2.*");  
        when(rexConfig.getRexlist()).thenReturn(regexList); 
        patchRegexPatterns.init(); 
    }  
  
    @Test  
    public void testFetchCommitIdFromPatchFile() throws IOException {  
        // 创建一个临时文件，并写入测试数据  
        Path tempFile = Files.createTempFile("testPatchFile", ".txt");  
        Files.write(tempFile, Arrays.asList(  
                "This is a commit line matching regex1",  
                "This is a non-commit line",  
                "Another commit line matching regex2"  
        ));  
  
        // 调用被测方法  
        List<String> result = patchRegexPatterns.fetchCommitIdFromPatchFile(tempFile.toString());  
  
        // 验证结果  
        List<String> expected = Arrays.asList(  
                "This is a commit line matching regex1",  
                "Another commit line matching regex2"  
        );  
        assertEquals(expected, result);  
  
        // 删除临时文件  
        Files.deleteIfExists(tempFile);  
    }  
  
    @Test  
    public void testFetchCommitIdFromPatchFileWithEmptyFile() throws IOException {  
        // 创建一个临时空文件  
        Path tempFile = Files.createTempFile("testEmptyPatchFile", ".txt");  
  
        // 调用被测方法  
        List<String> result = patchRegexPatterns.fetchCommitIdFromPatchFile(tempFile.toString());  
  
        // 验证结果  
        assertEquals(0, result.size());  
  
        // 删除临时文件  
        Files.deleteIfExists(tempFile);  
    }  
  
    @Test  
    public void testFetchCommitIdFromPatchFileWithFileNotFound() {  
        // 调用被测方法，传入一个不存在的文件路径  
        IOException e =  assertThrows(
            IOException.class, 
            () -> patchRegexPatterns.fetchCommitIdFromPatchFile("non_existent_file.txt"));

        // 验证结果  
        assertEquals("can not open file", e.getMessage());  
        
    }  
}
