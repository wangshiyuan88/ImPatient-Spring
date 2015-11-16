package edu.wangshiyuan.impatient.model;

public class UpdateAppointmentRequest {
	String state;
	
	public UpdateAppointmentRequest(String state) {
		this.state = state;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
