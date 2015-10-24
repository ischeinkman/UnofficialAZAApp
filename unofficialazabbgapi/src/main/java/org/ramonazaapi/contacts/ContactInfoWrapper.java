package org.ramonazaapi.contacts;

import org.ramonazaapi.rides.PersonInfoWrapper;

import java.util.Calendar;

/**
 * Created by Ilan Scheinkman on 1/12/15.
 */
public class ContactInfoWrapper implements PersonInfoWrapper {
    private String name;
    private String email;
    private String school;
    private String phoneNumber;
    private String address;
    private String gradYear;
    private int grade;
    private String year;
    private int idNum;
    private int area;
    private boolean present;
    private double latitude;
    private double longitude;

    public ContactInfoWrapper() {
        this.setPresent(false);
        this.setArea(-1);
    }

    /**
     * Returns the year string (Freshman, Sophomore, etc) from a given grade;
     *
     * @param grade the grade integer;
     * @return the year string
     */
    private static String yearStringFromGrade(int grade) {
        switch (grade) {
            case -1:
                return "Advisor";
            case 6:
                return "6th Grader";
            case 7:
                return "7th Grader";
            case 8:
                return "8th Grader";
            case 9:
                return "Freshman";
            case 10:
                return "Sophomore";
            case 11:
                return "Junior";
            case 12:
                return "Senior";
            default:
                return "College";

        }
    }

    /**
     * Gets a graduation year from a given grade;
     *
     * @param grade the grade integer
     * @return the graduation year;
     */

    private static int gradYearFromGrade(int grade) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        if (month > 8) year--;
        return year + (12 - grade);
    }

    private static int gradeFromGradYear(int gradYear) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        if (month > 8) year++;
        return 12 - (gradYear - year);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = Double.valueOf(latitude);
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = Double.valueOf(longitude);
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int inArea) {
        if (this.area == -1) this.area = inArea;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (this.name == null) this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        if (this.email == null) this.email = email;
    }

    public String getSchool() {
        return this.school;
    }

    public void setSchool(String school) {
        if (this.school == null) this.school = school;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this.phoneNumber == null) this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        if (this.address == null) this.address = address;
    }

    public String getGradYear() {
        return this.gradYear;
    }

    /**
     * Sets graduation year, year string, and grade from graduation year
     *
     * @param gradYear the graduation year to calculate from
     */
    public void setGradYear(String gradYear) {
        if (this.gradYear == null) {
            this.gradYear = gradYear;
            try {
                int gYear = Integer.parseInt(gradYear);
                this.grade = gradeFromGradYear(gYear);
            } catch (NumberFormatException e) {
                if (gradYear.equals("Advisor")) {
                    grade = -1;
                }
            }
            this.year = yearStringFromGrade(grade);
        }
    }

    public int getGrade() {
        return grade;
    }

    /**
     * Sets graduation year, year string, and grade from grade integer
     *
     * @param grade the grade to calculate from
     */
    public void setGrade(int grade) {
        if (this.grade == 0) {
            this.grade = grade;
            this.gradYear = "" + gradYearFromGrade(grade);
            this.year = yearStringFromGrade(grade);
        }
    }

    public String getYear() {
        return year;
    }

    public int getId() {
        return idNum;
    }

    public void setId(int inputId) {
        this.idNum = inputId;
    }

    @Override
    public boolean equals(Object inObj) {
        if (inObj instanceof ContactInfoWrapper) {
            if (((ContactInfoWrapper) inObj).getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return idNum * name.hashCode() + idNum;
    }
}
        




