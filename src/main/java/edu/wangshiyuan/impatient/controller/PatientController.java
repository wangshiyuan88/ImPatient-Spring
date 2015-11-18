package edu.wangshiyuan.impatient.controller;

import java.io.IOException;
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
import java.util.Timer;
import java.util.TimerTask;

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

import edu.wangshiyuan.impatient.model.Appointments.Appointment;
import edu.wangshiyuan.impatient.model.Appointments;
import edu.wangshiyuan.impatient.model.CheckIn;
import edu.wangshiyuan.impatient.model.CheckIns;
import edu.wangshiyuan.impatient.model.GeneralResponse;
import edu.wangshiyuan.impatient.model.ImPatientResponse;
import edu.wangshiyuan.impatient.model.PostResponse;
import edu.wangshiyuan.impatient.model.UpdateAppointmentRequest;
import edu.wangshiyuan.impatient.model.UpdateCheckInTimeRequest;
import edu.wangshiyuan.impatient.util.ParseTalkServer;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value="/")
public class PatientController {
	
    public static final String APP_STATE_NOT_START = "nostart";
    public static final String APP_STATE_CHECK_IN = "checkin";
    public static final String APP_STAET_IN_TRETMENT = "intreatment";
    public static final String APP_STAET_READY_FOR_TRETMENT = "readyfortreatment";
    public static final String APP_STATE_FINISH = "finish";
	
	private static final Logger logger = LoggerFactory.getLogger(PatientController.class);
	private static List<CheckIn> patientQ;
	private Retrofit retrofit;
	ParseTalkServer service;
	private Treatment currentTreatment;
	List<Appointment> activeApps;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	
	
	
	@PostConstruct
	public void initIt() throws Exception {
		logger.info("===================================");
		logger.info("=================Start==============");
		retrofit = new Retrofit.Builder().baseUrl(ParseTalkServer.root).addConverterFactory(GsonConverterFactory.create()).build();
		service = retrofit.create(ParseTalkServer.class);
		
		//Recover all active check in to appointment
		Call<Appointments> appointmentCall = service.getAppoinments();
		Appointments appointments = appointmentCall.execute().body();
		Call<CheckIns> checkInCall = service.getCheckIns();
		CheckIns checkIns = checkInCall.execute().body();
		activeApps = appointments.getActiveAppointments();
		patientQ = checkIns.getActiveCheckInList(appointments.getActiveAppointments());
		Collections.sort(patientQ);
		if(!patientQ.isEmpty()){
			CheckIn firstCheckIn = patientQ.get(0);
			for(Appointment app: activeApps){
				if(app.getObjectId().equals(firstCheckIn.getAppointmentID())&&app.getState().equals(APP_STAET_IN_TRETMENT))
					currentTreatment = new Treatment(firstCheckIn);
			}
		}
		
	}
	
	@RequestMapping(value = "/patient/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	
	@RequestMapping(value = "/patient/checkin", method = RequestMethod.POST)
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
				postCheckInToParse(checkIn);
			}
			msg = new Gson().toJson(msg, String.class);
		}
	
		return msg;
	}
	
	private void postCheckInToParse(final CheckIn checkIn) {
		Call<PostResponse> postResponse = service.postCheckIn(checkIn);
		postResponse.enqueue(new Callback<PostResponse>(){

			@Override
			public void onResponse(Response<PostResponse> response) {
				//If patient is the first one when added to the queue, then change his/her appointment status to readyfortreatment
				//then check 
				int index = patientQ.indexOf(checkIn);
				String newState = null;
				if(index==0){
					newState = PatientController.APP_STAET_READY_FOR_TRETMENT;
				}else{
					newState = PatientController.APP_STATE_CHECK_IN;
				}
				try {
					service.updateAppointment(checkIn.getAppointmentID(), new UpdateAppointmentRequest(newState)).execute();
					if(patientQ.size()>1){
						//If the patient queue size greater than 1, which means the new check in patient A has a check in time and take first 
						// place which belongs to patient B, we need change state of patient B from readyfortreatment back to checkin
						service.updateAppointment(patientQ.get(1).getAppointmentID(), new UpdateAppointmentRequest(PatientController.APP_STATE_CHECK_IN)).execute();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable t) {
				
			}
			
		});
		
	}

	@RequestMapping(value = "/patient/delay/{id}", method = RequestMethod.GET)
	public @ResponseBody String delay(@PathVariable("id") String userId) {
		logger.info("Delay");
		CheckIn checkIn = this.getCheckInByUserId(userId);
		String msg = null;
		if(checkIn==null){
			msg = "You haven't checked in yet";
		}else{
			synchronized(this){
				int index = patientQ.indexOf(checkIn);
				//If patient is the first one in the queue, set his/her appointment status from readyfortreatment to checkin
				if(index==0){
					try {
						if(patientQ.size()>1){
							service.updateAppointment(checkIn.getAppointmentID(), new UpdateAppointmentRequest(APP_STATE_CHECK_IN)).execute();
							service.updateAppointment(patientQ.get(1).getAppointmentID(), new UpdateAppointmentRequest(APP_STAET_READY_FOR_TRETMENT)).execute();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//If the patient is the last one in the queue, increase his check in time by 5 minutes, if not, swap his/her check in
				// time with the patient directly after him/her in the queue, and then add his/her new check in time by 5 minutes
				if(index==patientQ.size()-1){
					checkIn.setCheckInTime(checkIn.getCheckInTime()+5);
					try {
						service.updateCheckIn(checkIn.getObjectId(), new UpdateCheckInTimeRequest(checkIn.getCheckInTime())).execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					CheckIn next = patientQ.get(index+1);
					next.setCheckInTime(checkIn.getCheckInTime());
					checkIn.setCheckInTime(checkIn.getCheckInTime()+5);
					Collections.sort(patientQ);
					try {
						GeneralResponse r = service.updateCheckIn(checkIn.getObjectId(), new UpdateCheckInTimeRequest(checkIn.getCheckInTime())).execute().body();
						service.updateCheckIn(next.getObjectId(), new UpdateCheckInTimeRequest(next.getCheckInTime())).execute();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			msg = "You have successfully delayed your appoinment";
		}
		logger.info(msg);
		msg = new Gson().toJson(msg, String.class);
		return msg;
	}
	
	@RequestMapping(value = "/patient/treatment/{id}", method = RequestMethod.GET)
	public @ResponseBody ImPatientResponse treatment(@PathVariable("id") String userId) {
		logger.info("Treatment Requst from "+userId);
		CheckIn checkIn = this.getCheckInByUserId(userId);
		ImPatientResponse response = new ImPatientResponse();
		String msg = null;
		if(checkIn==null){
			response.setStatus(false);
			msg = "You haven't checked in yet";
		}else if(patientQ.indexOf(checkIn)!=0){
			response.setStatus(false);
			msg = "Sorry, it's not turn yet";
		}else{
			synchronized(this){
				response.setStatus(true);
				msg = "You are in Treatment now";
				try {
					service.updateAppointment(checkIn.getAppointmentID(), new UpdateAppointmentRequest(APP_STAET_IN_TRETMENT)).execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.info(checkIn.getUserID()+" will get treatment");
				currentTreatment = new Treatment(checkIn);
			}
		}
		response.setMsg(msg);
		return response;
	}
	
	
	@RequestMapping(value = "/admin/getcheckins", method = RequestMethod.GET)
	public @ResponseBody String getCheckIns() {
		logger.info("Get Check In");
		logger.info(new Gson().toJson(patientQ, List.class));
		return new Gson().toJson(patientQ, List.class);
	}
	
    
	@RequestMapping(value = "/admin/adjustorder/{id}/{direction}", method = RequestMethod.POST)
	public @ResponseBody ImPatientResponse adjustOrder(@PathVariable("id") String checkInID, @PathVariable("direction") String direction) {
		logger.info("Swap Order");
		ImPatientResponse response = new ImPatientResponse();
		final CheckIn checkIn = getCheckInByObjectId(checkInID);
		if(checkIn!=null){
			if(patientQ.indexOf(checkIn)==0&&currentTreatment!=null&&currentTreatment.running){
				response.setStatus(false);
				response.setMsg("Patient "+getPatientName(checkIn)+" is in the treatment now, please don't swap the order");
			}else if(direction.equals("UP")&&patientQ.indexOf(checkIn)==1&&currentTreatment!=null&&currentTreatment.running){
					response.setStatus(false);
					response.setMsg("Patient "+getPatientName(checkIn)+" is in the treatment now, please don't swap the order");
			}else{
				CheckIn other = swapCheckIn(checkIn, direction);
				logger.info(getPatientName(checkIn)+" and "+getPatientName(other)+" get swapped.");
				if(other!=null){
					Call<GeneralResponse> checkInUpate = service.updateCheckIn(checkIn.getObjectId(), new UpdateCheckInTimeRequest(checkIn.getCheckInTime()));
					checkInUpate.enqueue(new EmptyCallBack());
					Call<GeneralResponse> otherCheckInUpate = service.updateCheckIn(other.getObjectId(), new UpdateCheckInTimeRequest(other.getCheckInTime()));
					otherCheckInUpate.enqueue(new EmptyCallBack());
				}
				response.setStatus(true);
				response.setMsg(getPatientName(checkIn)+" and "+getPatientName(other)+" get swapped.");
			}
		}
		return response;
	}
	
	public class EmptyCallBack implements Callback<GeneralResponse>{

		@Override
		public void onResponse(Response<GeneralResponse> response) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFailure(Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
    private CheckIn swapCheckIn(CheckIn checkIn, String direction){
        int index = patientQ.indexOf(checkIn);
        CheckIn other = null;
        if(direction.equals("UP")&&index!=0){
            other = patientQ.get(index-1);
            int temp = other.getCheckInTime();
            other.setCheckInTime(checkIn.getCheckInTime());
            checkIn.setCheckInTime(temp);
            Collections.sort(patientQ);
        }else if(direction.equals("DOWN")&&index!=patientQ.size()-1){
            other = patientQ.get(index+1);
            int temp = other.getCheckInTime();
            other.setCheckInTime(checkIn.getCheckInTime());
            checkIn.setCheckInTime(temp);
            Collections.sort(patientQ);
        }
        return other;
    }
	
	private String getPatientName(CheckIn checkIn){
		return checkIn.getFirstName()+" "+checkIn.getLastName();
	}
	
	@RequestMapping(value = "/admin/deletecheckins/{id}", method = RequestMethod.POST)
	public @ResponseBody List<CheckIn> deleteCheckin(@PathVariable("id") String checkInID) {
		logger.info("Delete Check In");
		final CheckIn checkIn = getCheckInByObjectId(checkInID);
		if(checkIn!=null){
			if(patientQ.indexOf(checkIn)==0&&currentTreatment!=null){
				currentTreatment.stopTreatement();
			}
			patientQ.remove(checkIn);
			Call<GeneralResponse> deleteCheckInCall = service.deleteCheckIn(checkIn.getObjectId());
			deleteCheckInCall.enqueue(new Callback<GeneralResponse>(){

				@Override
				public void onResponse(Response<GeneralResponse> response) {
					try {
						service.deleteAppointment(checkIn.getAppointmentID()).execute();
						Appointment app = PatientController.this.getAppointmentByID(checkIn.getAppointmentID());
						if(app!=null){
							activeApps.remove(app);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Throwable t) {
					// TODO Auto-generated method stub
				}
				
			});
		}
		return patientQ;
	}

	
	private CheckIn getCheckInByObjectId(String checkInID) {
		for(CheckIn checkIn : patientQ){
			if(checkIn.getObjectId().equals(checkInID))
				return checkIn;
		}
		return null;
	}

	@RequestMapping(value = "/patient/checkStatus/{id}", method = RequestMethod.GET)
	public @ResponseBody String checkStatus(@PathVariable("id") String userId) {
		logger.info("Check status");
		String waitingTime = null;
		CheckIn checkIn = getCheckInByUserId(userId);
		if(checkIn!=null){
			int timestamp = 0;
			if(currentTreatment!=null&&!currentTreatment.isFinish()){
				timestamp = (patientQ.indexOf(checkIn)-1)*15 + 15 - currentTreatment.elapsedTime()/60000;
			}else{
				timestamp = patientQ.indexOf(checkIn)*15;
			}
			waitingTime = this.convertTimeStampToString(timestamp);
			if(timestamp==0){
				try {
					service.updateAppointment(checkIn.getAppointmentID(), new UpdateAppointmentRequest(APP_STAET_READY_FOR_TRETMENT)).execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			//TODO: if no checkIn object found, that means this guy haven't checked in, or treatment have already finished
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
	
	public List<Appointment> getActiveAppointments(){
		return activeApps;
	}
	
	public Appointment getAppointmentByID(String objectID){
		for(Appointment appointment: activeApps){
			if(appointment.getObjectId().equals(objectID))
				return appointment;
		}
		return null;
	}
	  class Treatment extends TimerTask {
		  private CheckIn checkIn;
		  public int timeunit = 60 * 1000;
		  //public int timeunit = 60;
		  public int counter = 0;
		  public int maxCounter = 5;
		  private Timer timer; 
		  private boolean finish = false;
		  private boolean running = false;
		  public Treatment(CheckIn checkIn){
			  this.checkIn = checkIn;
			  timer = new Timer();
			  timer.schedule(this, 0, timeunit);
		  }
		  public void run() {
			  running = true;
			  counter++;
			  logger.info("Current Counter: "+counter);
			  
			  logger.info(checkIn.toUserString()+" has been treated for "+counter*timeunit/60+" minutes");
			  if(counter==maxCounter){
				  logger.info(checkIn.toUserString()+" has finished treatment");
				  synchronized(this){
					  patientQ.remove(this.checkIn);
				  }
				  logger.info("Remove "+checkIn.toUserString()+" from patient queue.");
				  try {
					service.updateAppointment(checkIn.getAppointmentID(), new UpdateAppointmentRequest(APP_STATE_FINISH)).execute();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				  finish = true;
				  running = false;
				  timer.cancel();
			  }
		  }
		  
		  public boolean isFinish(){
			  return finish;
		  }
		  public int elapsedTime(){
			  return counter * timeunit;
		  }
		  public CheckIn getCheckIn(){
			  return checkIn;
		  }
		  
		  public void newTreatment(CheckIn checkIn){
			  reset();
			  this.checkIn = checkIn;
			  timer.schedule(this, 0, timeunit);
		  }
		  
		  private void reset(){
			  counter = 0;
			  finish = false;
			  timer = new Timer();
			  running = false;
		  }
		  
		  public void stopTreatement(){
			  if(running)
				  timer.cancel();
			  reset();
		  }
	  }
}
