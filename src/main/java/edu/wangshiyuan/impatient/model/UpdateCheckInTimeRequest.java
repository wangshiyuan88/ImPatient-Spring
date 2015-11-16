package edu.wangshiyuan.impatient.model;

public class UpdateCheckInTimeRequest {
	int checkInTime;
	public UpdateCheckInTimeRequest(int checkInTime){
		this.checkInTime = checkInTime;
	}
	public int getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(int checkInTime) {
		this.checkInTime = checkInTime;
	}
	
}
