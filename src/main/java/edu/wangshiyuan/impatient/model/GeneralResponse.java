package edu.wangshiyuan.impatient.model;

public class GeneralResponse {
	String updatedAt;
	
	public GeneralResponse(String updateAt){
		this.updatedAt = updateAt;
	}
	
	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	
}
