package com.softwaremarket.autoupgrade.dto;

import com.gitee.sdk.gitee5j.model.TreeEntry;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class TreeEntryExpandDto extends TreeEntry {
    private List<TreeEntryExpandDto> next;
    private File file;

    public TreeEntryExpandDto(TreeEntry treeEntry) {
        this.setMode(treeEntry.getMode());
        this.setPath(treeEntry.getPath());
        this.setSha(treeEntry.getSha());
        this.setType(treeEntry.getType());

    }
}
