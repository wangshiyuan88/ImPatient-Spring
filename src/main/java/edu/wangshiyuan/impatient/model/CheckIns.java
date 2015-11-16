package edu.wangshiyuan.impatient.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.wangshiyuan.impatient.model.Appointments.Appointment;


public class CheckIns {
	private List<CheckIn> results;
	
	public List<CheckIn> getCheckInList(){
		return results;
	}
	
	public List<CheckIn> getActiveCheckInList(List<Appointment> activeAppointmentList){
		Set<String> activeAppointmentId = new HashSet<String>();
		for(Appointment app : activeAppointmentList){
			activeAppointmentId.add(app.getObjectId());
		}
		List<CheckIn> activeCheckInList = new ArrayList<CheckIn>();
		for(CheckIn checkIn : results){
			if(activeAppointmentId.contains(checkIn.getAppointmentID()))
				activeCheckInList.add(checkIn);
		}
		return activeCheckInList;
	}
}
