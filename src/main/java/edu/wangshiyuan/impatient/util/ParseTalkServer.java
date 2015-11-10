package edu.wangshiyuan.impatient.util;

import java.util.List;

import edu.wangshiyuan.impatient.model.Appointment;
import retrofit.Call;
import retrofit.Response;
import retrofit.http.GET;
import retrofit.http.Headers;

public interface ParseTalkServer {
	public static String root = "https://api.parse.com";
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@GET("/1/classes/Appointment")
	Call<List<Appointment>> getAppoinments();

}
