package edu.wangshiyuan.impatient.util;

import java.util.List;

import com.squareup.okhttp.ResponseBody;

import edu.wangshiyuan.impatient.model.Appointments;
import edu.wangshiyuan.impatient.model.Appointments.Appointment;
import edu.wangshiyuan.impatient.model.CheckIn;
import edu.wangshiyuan.impatient.model.CheckIns;
import edu.wangshiyuan.impatient.model.PostResponse;
import edu.wangshiyuan.impatient.model.UpdateAppointmentRequest;
import edu.wangshiyuan.impatient.model.UpdateCheckInTimeRequest;
import edu.wangshiyuan.impatient.model.GeneralResponse;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface ParseTalkServer {
	public static String root = "https://api.parse.com";
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@GET("/1/classes/Appointment")
	Call<Appointments> getAppoinments();
		
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@POST("/1/classes/CheckIn")
	Call<PostResponse> postCheckIn(@Body CheckIn checkin);
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@GET("/1/classes/CheckIn")
	Call<CheckIns> getCheckIns();
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@PUT("/1/classes/Appointment/{appointmentID}")
	Call<GeneralResponse> updateAppointment(@Path("appointmentID") String appointmentID, @Body UpdateAppointmentRequest body);
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@PUT("/1/classes/CheckIn/{id}")
	Call<GeneralResponse> updateCheckIn(@Path("id") String id, @Body UpdateCheckInTimeRequest body);
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@DELETE("/1/classes/CheckIn/{id}")
	Call<GeneralResponse> deleteCheckIn(@Path("id") String id);
	
	@Headers({
	    "X-Parse-Application-Id: BlvP9seVEeiy5kP95UbV7GnJ1PFbUdchTa3HHzlF",
	    "X-Parse-REST-API-Key: 1SxdQlMya4CSRPkSm4LVhiAEfu9zTssoHjOLq4gM"
	})
	@DELETE("/1/classes/Appointment/{id}")
	Call<GeneralResponse> deleteAppointment(@Path("id") String id);
	
}
