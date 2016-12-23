package com.sharpdroid.registro.Interfaces.API;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileTeacher implements Serializable {
    private String name;
    private List<Folder> folders = new ArrayList<>();

    public FileTeacher(String name, List<Folder> folders) {
        this.name = name;
        this.folders = folders;
    }

    public String getName() {
        return name;
    }

    public List<Folder> getFolders() {
        return folders;
    }
}