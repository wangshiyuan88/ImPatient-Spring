package edu.wangshiyuan.impatient.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import edu.wangshiyuan.impatient.model.Appointment;
import edu.wangshiyuan.impatient.model.CheckIn;
import edu.wangshiyuan.impatient.util.ParseTalkServer;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value="/patient")
public class PatientController {
	
	
	
	private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
	private static final List<CheckIn> patientQ = new ArrayList<CheckIn>();
	private Retrofit retrofit;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	
	
	
	@PostConstruct
	public void initIt() throws Exception {
		logger.info("===================================");
		logger.info("=================Start==============");
		retrofit = new Retrofit.Builder().baseUrl(ParseTalkServer.root).addConverterFactory(GsonConverterFactory.create()).build();
		ParseTalkServer service = retrofit.create(ParseTalkServer.class);
		Call<List<Appointment>> appointmentCall = service.getAppoinments();
		Response<List<Appointment>> appointments = appointmentCall.execute();
		logger.info(appointments.toString());
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	
	@RequestMapping(value = "/checkin", method = RequestMethod.POST)
	public @ResponseBody String checkin(final @RequestBody CheckIn checkIn) {
		logger.info("Check In");
		String msg = null;
		synchronized(this){
			if(patientQ.contains(checkIn)){
				msg =  "You have already checked in at" + convertTimeStampToString(checkIn.getCheckInTime());
			}else{
				patientQ.add(checkIn);
				Collections.sort(patientQ);
				msg = "You are just added to wait queue by checking time at " + convertTimeStampToString(checkIn.getCheckInTime());
			}
			msg = new Gson().toJson(msg, String.class);
		}
		return msg;
	}
	
	@RequestMapping(value = "/delay/{id}", method = RequestMethod.GET)
	public @ResponseBody String delay(@PathVariable("id") String userId) {
		logger.info("Delay");
		CheckIn checkIn = this.getCheckInByUserId(userId);
		String msg = null;
		if(checkIn==null){
			msg = "You haven't checked in yet";
		}else{
			synchronized(this){
				int index = patientQ.indexOf(checkIn);
				if(index==patientQ.size()-1){
					checkIn.setCheckInTime(checkIn.getCheckInTime()+5);
				}else{
					CheckIn next = patientQ.get(index+1);
					next.setCheckInTime(checkIn.getCheckInTime());
					checkIn.setCheckInTime(checkIn.getCheckInTime()+5);
					Collections.sort(patientQ);
				}
			}
			msg = "You have successfully delayed your appoinment";
		}
		logger.info(msg);
		msg = new Gson().toJson(msg, String.class);
		return msg;
	}
	
	@RequestMapping(value = "/checkStatus/{id}", method = RequestMethod.GET)
	public @ResponseBody String checkStatus(@PathVariable("id") String userId) {
		logger.info("Check status");
		String waitingTime = null;
		CheckIn checkIn = getCheckInByUserId(userId);
		if(checkIn!=null){
			int timestamp = patientQ.indexOf(checkIn)*15;
			waitingTime = this.convertTimeStampToString(timestamp);
		}
		logger.info("user "+userId+" will wait for "+waitingTime);
		waitingTime = new Gson().toJson(waitingTime, String.class);
		return waitingTime;
	}
	
	private String convertTimeStampToString(int timestamp){
		String hour = timestamp/60>=10? ""+timestamp/60:"0"+timestamp/60;
		String min = timestamp%60>=10? ""+timestamp%60:"0"+timestamp%60;
		return hour +":"+ min;
	}
	
	private CheckIn getCheckInByUserId(String userID){
		for(CheckIn checkIn : patientQ){
			if(checkIn.getUserID().equals(userID))
				return checkIn;
		}
		return null;
	}
	
	
	
}
