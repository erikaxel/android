package io.lucalabs.expenses.models;

import com.google.firebase.database.IgnoreExtraProperties;

import io.lucalabs.expenses.models.annotations.Arg;

@IgnoreExtraProperties
public class ExpenseReport {

    private String name;
    private String reference;
    @Arg(name="expense_report[firebase_ref]")
    private String firebase_ref;
    private String project_code;
    private boolean billable;
    private String comment;
    @Arg(name="expense_report[finalized]")
    private boolean finalized;

    // Travel specific attributes
    private boolean travel;
    private String arrival_at;
    private String departure_at;
    private String destination;
    private String source;

    public ExpenseReport(){}

    public String getNameString(){
        if(name == null || name.isEmpty())
            return reference;
        else return name;
    }

    public String getSubtitleString() {
        if(name == null)
            return "";
        else return reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getFirebase_ref() {
        return firebase_ref;
    }

    public void setFirebase_ref(String firebase_ref) {
        this.firebase_ref = firebase_ref;
    }

    public String getProject_code() {
        return project_code;
    }

    public void setProject_code(String project_code) {
        this.project_code = project_code;
    }

    public boolean isBillable() {
        return billable;
    }

    public void setBillable(boolean billable) {
        this.billable = billable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isTravel() {
        return travel;
    }

    public void setTravel(boolean travel) {
        this.travel = travel;
    }

    public String getArrival_at() {
        return arrival_at;
    }

    public void setArrival_at(String arrival_at) {
        this.arrival_at = arrival_at;
    }

    public String getDeparture_at() {
        return departure_at;
    }

    public void setDeparture_at(String departure_at) {
        this.departure_at = departure_at;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isFinalized(){
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public boolean equals(ExpenseReport other){
        return false;
//        return getName().equals(other.getName())
//                && isBillable() == other.isBillable()
//                && getComment() != null && getComment().equals(other.getComment())
//                && isTravel() == other.isTravel()
//                && getArrival_at().equals(other.getArrival_at())
//                && getDeparture_at().equals(other.getDeparture_at())
//                && getDestination().equals(other.getDestination())
//                && getSource().equals(other.getSource());
    }
}
