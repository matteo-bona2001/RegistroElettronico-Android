package com.sharpdroid.registroelettronico.Interfaces.Client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Subject implements Serializable {
    private int id;
    private String name;
    private float target;
    private String professor, classroom, notes;
    private List<Integer> teacherCode;

    public Subject(int id, String name, float target, String professor, String classroom, String notes, String[] teacherCode) {
        this.id = id;
        this.name = name;
        this.target = target;
        this.professor = professor;
        this.classroom = classroom;
        this.notes = notes;
        this.teacherCode = new ArrayList<>();
        for (String s : teacherCode) {
            this.teacherCode.add(Integer.valueOf(s));
        }
    }

    public String getClassroom() {
        return classroom;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public String getProfessor() {
        if (getProfessors().length > 0)
            return getProfessors()[0];
        else
            return "";
    }

    public void setProfessor(String professor) {
        this.professor = professor;
    }

    public String[] getProfessors() {
        if (professor != null)
            return professor.split(",");
        else return null;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public List<Integer> getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(List<Integer> teacherCode) {
        this.teacherCode = teacherCode;
    }
}
