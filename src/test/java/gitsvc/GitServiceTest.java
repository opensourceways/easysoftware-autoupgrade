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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import com.softwaremarket.autoupgrade.config.GitConfig;
import com.softwaremarket.autoupgrade.service.impl.GitService;

@ExtendWith(MockitoExtension.class) 
public class GitServiceTest {

    @Mock  
    private GitConfig config;  
  
    @Mock  
    private Git git;  
  
    @InjectMocks  
    private GitService gitService;  
  
    @Captor  
    private ArgumentCaptor<File> fileCaptor;  
  
    @Captor  
    private ArgumentCaptor<String> stringCaptor;  
  
    @BeforeEach  
    public void setUp() {  
        MockitoAnnotations.openMocks(this);  
    }  
  
    @Test  
    public void testCloneRepo_Success() throws Exception {  
        // Arrange  
        String remotePath = "https://github.com/user/repo.git";  
        String expectedLocalPath = "/local/path/repo";  
        when(config.getStorePath()).thenReturn("/local/path");  
  
        UsernamePasswordCredentialsProvider provider = mock(UsernamePasswordCredentialsProvider.class);  
        doReturn(provider).when(gitService).getProvider();  
  
        doNothing().when(git).cloneRepository()  
                .setURI(remotePath)  
                .setDirectory(fileCaptor.capture())  
                .setCredentialsProvider(any())  
                .setCloneSubmodules(true)  
                .call();  
  
        // Act  
        gitService.cloneRepo(remotePath);  
  
        // Assert  
        File capturedLocalPath = fileCaptor.getValue();  
        assertEquals(new File(expectedLocalPath), capturedLocalPath);  
        verify(git, times(1)).cloneRepository();  
    }  
    
}
