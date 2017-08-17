package com.digirati.themathmos.model;

import java.io.Serializable;
import java.util.Objects;

public class Parameters implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1118550633018738922L;
    private String query;
    private String motivation;
    private String date;
    private String user;

    public Parameters(){

    }
    public Parameters(String query, String motivation, String date, String user){
	this.query = query;
	this.motivation = motivation;
	this.date = date;
	this.user = user;
    }

    public String getQuery() {
	return query;
    }
    public void setQuery(String query) {
	this.query = query;
    }
    public String getMotivation() {
	return motivation;
    }
    public void setMotivation(String motivation) {
	this.motivation = motivation;
    }
    public String getDate() {
	return date;
    }
    public void setDate(String date) {
	this.date = date;
    }
    public String getUser() {
	return user;
    }
    public void setUser(String user) {
	this.user = user;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Parameters)) {
            return false;
        }

        Parameters parameters = (Parameters) o;

        return Objects.equals(parameters.query, query) &&
        	Objects.equals(parameters.motivation, motivation) &&
        	Objects.equals(parameters.date,date) &&
        	Objects.equals(parameters.user,user);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + query.hashCode();
        result = 31 * result + motivation.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }

}
