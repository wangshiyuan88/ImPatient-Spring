package edu.wangshiyuan.impatient.model;

import java.util.ArrayList;
import java.util.List;

import edu.wangshiyuan.impatient.controller.PatientController;
import edu.wangshiyuan.impatient.util.Utils;

public class Appointments{
	
	private List<Appointment> results;
	
	public List<Appointment> getAppointments(){
		return results;
	}
	
	public List<Appointment> getActiveAppointments(){
		List<Appointment> ret = new ArrayList<Appointment>();
		String currentDate = Utils.getCurrentDateString();
		for(Appointment app : results){
			String appState = app.getState();
			if(appState!=null && app.getDate().equals(currentDate)){
				if(appState.equals(PatientController.APP_STAET_IN_TRETMENT)||appState.equals(PatientController.APP_STAET_READY_FOR_TRETMENT)
						||appState.equals(PatientController.APP_STATE_CHECK_IN))
				ret.add(app);
			}
		}
		return ret;
	}
	
	
	public class Appointment {
		
		String objectId;
		String date;
		int hour;
		int minute;
		String patient;
		int timestamp;
		String state;
		
		public String getObjectId() {
			return objectId;
		}
		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public int getHour() {
			return hour;
		}
		public void setHour(int hour) {
			this.hour = hour;
		}
		public int getMinute() {
			return minute;
		}
		public void setMinute(int minute) {
			this.minute = minute;
		}
		public String getPatient() {
			return patient;
		}
		public void setPatient(String patient) {
			this.patient = patient;
		}
		public int getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(int timestamp) {
			this.timestamp = timestamp;
		}
		
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String toString(){
			return "[ Patient: " + this.patient+", date: " + this.date + ", hour: "+hour+", minute: "+minute+" ]";
		}
	
	}
}
