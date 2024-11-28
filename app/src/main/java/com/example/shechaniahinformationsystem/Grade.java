package com.example.shechaniahinformationsystem;

public class Grade {
    private String subject;
    private String g1;
    private String g2;
    private String g3;
    private String g4;
    private String finalGrade;

    public Grade(String subject, String g1, String g2, String g3, String g4, String finalGrade) {
        this.subject = subject;
        this.g1 = g1;
        this.g2 = g2;
        this.g3 = g3;
        this.g4 = g4;
        this.finalGrade = finalGrade;
    }

    public String getSubject() {
        return subject;
    }

    public String getG1() {
        return g1;
    }

    public String getG2() {
        return g2;
    }

    public String getG3() {
        return g3;
    }

    public String getG4() {
        return g4;
    }

    public String getFinalGrade() {
        return finalGrade;
    }
}
