package com.TeamProject.Kdemy.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.TeamProject.Kdemy.interceptor.SessionNames;
import com.TeamProject.Kdemy.model.admin.dto.AdminDTO;
import com.TeamProject.Kdemy.model.alarm.dto.AlarmDTO;
import com.TeamProject.Kdemy.model.lecture.dto.LectureBoxDTO;
import com.TeamProject.Kdemy.model.member.dto.MemberDTO;
import com.TeamProject.Kdemy.service.alarm.AlarmService;
import com.TeamProject.Kdemy.service.chat.ChatRoomService;
import com.TeamProject.Kdemy.service.lecture.LectureService;

public class KdemyWebSocketHandler extends TextWebSocketHandler{
	
	
	List<WebSocketSession> sessions =new ArrayList<>();
	  
	Map<String,WebSocketSession> userSessions =new HashMap<>();
	Map<String,WebSocketSession> AdminSessions =new HashMap<>();
	Map<String,Object> chatroomNum =new HashMap<>();
	
	@Inject
	ChatRoomService chatRoomService;
	
	@Inject
	AlarmService alarmService;
	
	@Inject 
	LectureService lectureService;
	
	 // connection이 맺어진 후 실행된다
	@Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Map<String, Object> httpSession=session.getAttributes();
		AdminDTO adminUser = (AdminDTO)httpSession.get(SessionNames.ADMINLOGIN);
		if(adminUser != null) {
			String adminId=getAdminId(session);
			AdminSessions.put(adminId, session);
		}else {
			String senderId=getId(session);
			userSessions.put(senderId,session);	   
		}
    }
    // 메세지 수신
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    	 System.out.println("hadelText Message : " +session+ message);
    	 String msg=message.getPayload();
    	 String alarmmsg=null;
    	 if(StringUtils.isNotEmpty(msg)) {
    		 String[] strs = msg.split(",");
    		 if(strs != null && strs.length == 3) {
    			String cmd= strs[0];
     			String sender=strs[1];
     			String target=strs[2];
     			WebSocketSession adminTargetSession = AdminSessions.get(target);
     			WebSocketSession teacherTargetSession = userSessions.get(target);
     			AlarmDTO dto=new AlarmDTO();
     			dto.setSender(sender);
     			dto.setTarget(target);
     			if("teaching".equals(cmd)) {
    				if(adminTargetSession != null) {
    					alarmmsg=sender + "님이"
    							+"<a type='external' href=\'/Kdemy/member/list.do?keyword="+sender+"&location=request\'> 강사 신청을 하였습니다.</a>";
    					TextMessage tmpMsg = new TextMessage(alarmmsg);
    					adminTargetSession.sendMessage(tmpMsg);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}else {
    					alarmmsg=sender + "님이"
						+"<a href='/Kdemy/member/list.do?keyword="+sender+"&location=request'> 강사 신청을 하였습니다.</a>";
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}
    			}else if("teacherSuccess".equals(cmd)) {
    				if(teacherTargetSession != null) {
    					alarmmsg="강사 승인이 완료되었습니다.";
    					TextMessage tmpMsg = new TextMessage(alarmmsg);
    					teacherTargetSession.sendMessage(tmpMsg);
    					System.out.println("asfasdfasdfasdfasdfasdfasdfasdfasdf"+dto);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}else {
    					alarmmsg="강사 승인이 완료되었습니다.";
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}
    			}else if("buyAlarm".equals(cmd)) {
    				int lecture_idx=Integer.parseInt(target);
    				String makeUserid=lectureService.buyAlarm(lecture_idx);
    				WebSocketSession makeUseridSession= userSessions.get(makeUserid);
    				System.out.println("makeUseridmakeUseridmakeUseridmakeUseridmakeUseridmakeUseridmakeUserid"+makeUserid);
    				if(makeUseridSession != null) {
    					alarmmsg=sender+"님이"+lecture_idx+"번의 강의를 구매하였습니다.";
    					TextMessage tmpMsg = new TextMessage(alarmmsg);
    					makeUseridSession.sendMessage(tmpMsg);
    					dto.setTarget(makeUserid);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}else {
    					alarmmsg=sender+"님이"+lecture_idx+"번의 강의를 구매하였습니다.";
    					dto.setTarget(makeUserid);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}
    				
    			}
    		 }
    		 if(strs != null && strs.length == 4) {
    			String cmd= strs[0];
    			String sender=strs[1];
    			String target=strs[2];
    			String num=strs[3];
    			AlarmDTO dto=new AlarmDTO();
     			dto.setSender(sender);
     			dto.setTarget(target);
     			
    			WebSocketSession boardWriterSession = userSessions.get(target);
    			if("reply".equals(cmd)) {
    				if(boardWriterSession != null) {
    					alarmmsg=sender + "님이"
    							+"<a href='/Kdemy/review/view.do?bno="+num+"'>"+ num +"번 게시글에 댓글을 달았습니다.";
    					TextMessage tmpMsg = new TextMessage(alarmmsg);
    					boardWriterSession.sendMessage(tmpMsg);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}else {
    					alarmmsg=sender + "님이"
    							+"<a href='/Kdemy/review/view.do?bno="+num+"'>"+ num +"번 게시글에 댓글을 달았습니다.";
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}
    			}else if("lectureDelete".equals(cmd)) {
    				if(boardWriterSession!=null) {
    					alarmmsg=num+"번 강의글이 부적절한 컨텐츠로 판단되어 관리자의 권한으로 삭제되었습니다.";
    					TextMessage tmpMsg = new TextMessage(alarmmsg);
    					boardWriterSession.sendMessage(tmpMsg);
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    					List<LectureBoxDTO> lecturebuyList=new ArrayList<>();
    					int lecture_idx=Integer.parseInt(num);
    					lecturebuyList=lectureService.lecturebuyList(lecture_idx);
    					System.out.println("lecturebuyList"+lecturebuyList);
    					if(lecturebuyList != null) {
    						for(int i=0;i<lecturebuyList.size(); i++) {
    							String userid=lecturebuyList.get(i).getUserid();
    							WebSocketSession buysession=userSessions.get(userid);
    							System.out.println("buysession"+buysession);
    							if(buysession != null) {
    								alarmmsg=num+"번 강의글이 부적절한 컨텐츠로 판단되어 관리자의 권한으로 삭제되었습니다.<br>컨텐츠를 구매하신 금액은 환불되었습니다.";
    								TextMessage deleteMsg = new TextMessage(alarmmsg);
    								buysession.sendMessage(deleteMsg);
    								dto.setAlarmmsg(alarmmsg);
    								alarmService.insertAlarm(dto);
    							}else {
    								alarmmsg=num+"번 강의글이 부적절한 컨텐츠로 판단되어 관리자의 권한으로 삭제되었습니다.<br>컨텐츠를 구매하신 금액은 환불되었습니다.";
    								dto.setAlarmmsg(alarmmsg);
    								alarmService.insertAlarm(dto);
    							}
    						}
    					}
    				}else {
    					alarmmsg=num+"번 강의글이 부적절한 컨텐츠로 판단되어 관리자의 권한으로 삭제되었습니다.";
    					dto.setAlarmmsg(alarmmsg);
    					alarmService.insertAlarm(dto);
    				}
    			}
    		 }
    		 if(strs != null && strs.length == 5) {
    			String cmd= strs[0];
     			String sender=strs[1];
     			String target=strs[2];
     			String num=strs[3];
     			String Msg=strs[4];
     			
     			String roomCNum="chat"+num;
     			String roomSNum=sender+num;
     			String roomTNum=target+num;
  
     			WebSocketSession chatWTargetSession = AdminSessions.get(target);
     			WebSocketSession userWTargetSession = userSessions.get(target);
     			
     			if(roomCNum.equals(cmd)){
     				if(chatWTargetSession != null) {
     					TextMessage adminSendMsg = new TextMessage(sender+"님이 문의를 하였습니다.");
     					chatWTargetSession.sendMessage(adminSendMsg);
     				}else {
     					
     				}
    			}else if(roomTNum.equals(cmd)) {
    				if(userWTargetSession != null) {
    					String usermsg = "<li class='bg-warning text-left ml-auto mb-1'><b>관리자 :</b><br>"+Msg+"</li>";
     					TextMessage adminSendMsg = new TextMessage(cmd+","+sender+","+target+","+usermsg+","+num);
     					int chatroom_id=Integer.parseInt(num);
     					String chatmsg=usermsg;
     					chatRoomService.insertMessage(chatroom_id,chatmsg);
     					userWTargetSession.sendMessage(adminSendMsg);
     					
     				}else {
     				}
    			} 				
    			else if(roomSNum.equals(cmd)) {
    		    	if(chatWTargetSession != null) {
    		    		String adminmsg = "<li class='bg-light text-left mr-auto mb-1'><b>"+sender+" :</b><br>"+Msg+"</li>";
     					TextMessage adminSendMsg = new TextMessage(cmd+","+sender+","+target+","+adminmsg+","+num);
     					int chatroom_id=Integer.parseInt(num);
     					String chatmsg=adminmsg;
     					chatRoomService.insertMessage(chatroom_id,chatmsg);
     					chatWTargetSession.sendMessage(adminSendMsg);
     				}else {
     					
     				}
    			}
    		 }
    	 }
    }
    // transport 중 error
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("transport error =" + session +", exception =" + exception);
    }
    // connection close
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.err.println("session close -=" + session);
    }
    
    private String getId(WebSocketSession session) {
    	Map<String, Object> httpSession=session.getAttributes();
    	MemberDTO loginUser = (MemberDTO)httpSession.get(SessionNames.LOGIN);
    	if(null == loginUser) {
    		return session.getId();
    	}else {
    		return loginUser.getUserid();
    	}
    }
    
    private String getAdminId(WebSocketSession session) {
		Map<String, Object> httpSession=session.getAttributes();
		AdminDTO adminUser = (AdminDTO)httpSession.get(SessionNames.ADMINLOGIN);	  
		if(adminUser != null) {
			return adminUser.getAdmin_id();
		}else {
			return "admin";
		}
	}
}
