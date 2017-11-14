package com.zczg.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SipUser implements Comparable<SipUser>{
	// Each incoming call goes through the following states:
	private static Logger logger = LoggerFactory.getLogger(SipUser.class);
	
	public final static String IDLE = "IDLE";
	public final static String INIT_BRIDGE = "INIT_BRIDGE";
	public final static String WAITING_FOR_MEDIA_SERVER = "WAITING_FOR_MEDIA_SERVER";
	public final static String WAITING_FOR_ACK = "WAITING_FOR_ACK";
	public final static String WAITING_FOR_BRIDGE = "WAITING_FOR_BRIDGE";
	public final static String CALLING = "CALLING";
	public final static String HOLDON_HOST = "HOLDON_HOST";
	public final static String HOLDON_GUEST = "HOLDON_GUEST";
	public final static String ONCALL_INVITE = "ONCALL_INVITE";
	public final static String PRIORITY_CHECK = "PRIORITY_CHECK";
	public final static String END = "END";
	public final static String ANSWER_BRIDGE = "ANSWER_BRIDGE";
	
	private Integer priority;
	private ReentrantLock r;
	public String name;
	public String sipadd;
	public String preforwardAlways;
	public String preforwardBusy;
	public String preforwardTimeout;
	public String ip;
	public String port;
	public Address contact;
	public Boolean wait;
	private Boolean busy;
	
	public Map<String, PriorityQueue<SipUser> > linkUser;
	public Map<String, String> realUser;
	public Map<String, SipSession> sessions;
	public Map<String, SipUser> users;
	public String oppo;
	
	public SipUser(String _name, String _sipadd, String _ip, String _port, Integer _priority, Boolean _wait, 
			String _preforwardAlways, String _preforwardBusy, String _preforwardTimeout, Map<String, SipUser> _users, Address _contact){
		name = _name;
		sipadd = _sipadd;
		priority = _priority;
		wait = _wait;
		preforwardAlways = _preforwardAlways;
		preforwardBusy = _preforwardBusy;
		preforwardTimeout = _preforwardTimeout;
		contact = _contact;
		ip = _ip;
		port = _port;

		busy = false;
		oppo = null;
		r = new ReentrantLock();
		linkUser = new HashMap<String, PriorityQueue<SipUser> >();
		realUser = new HashMap<String, String>();
		sessions = new HashMap<String, SipSession>();
		users = _users;
	}
	
	public SipUser(String _name, String realm, Address _contact, Map<String, SipUser> _users) {
		name = _name;
		sipadd = "sip:" + _name + "@" + realm;
		priority = 0;
		wait = false;
		contact = _contact;
		ip = realm;
		port = "5080";

		busy = false;
		oppo = null;
		r = new ReentrantLock();
		linkUser = new HashMap<String, PriorityQueue<SipUser> >();
		realUser = new HashMap<String, String>();
		sessions = new HashMap<String, SipSession>();
		users = _users;
	}

	@Override
	public int compareTo(SipUser o) {
		// -1为更优先
		if(o == null)
			return -1;
		
		boolean w1 = HOLDON_GUEST.equals(sessions.get(o.name).getAttribute("STATE"));
		boolean w2 = HOLDON_GUEST.equals(o.sessions.get(name).getAttribute("STATE"));
		
		if(w1 && !w2)
			return -1;
		else if(w2 && !w1) 
			return 1;
		
		int dis = 0;
		dis = this.priority - o.priority;
		
		if(dis > 0) return 1;
		else if(dis == 0) return 0;
		else return -1;
	}
	
	public boolean compareState(String name, String st) {
		r.lock();
		try{
			return st.equals(sessions.get(name).getAttribute("STATE"));
		} catch (Exception e) {
			logger.warn("No user");
			return false;
		} finally {
			r.unlock();
		}
	}

	public void setState(String _name, String st) {
		r.lock();
		try{
			SipSession session = sessions.get(_name);
			if(session != null && session.getAttribute("STATE") == null)
				session.setAttribute("STATE", "START");
				
			System.out.println("change from " + sessions.get(_name).getAttribute("STATE") + " to " + st);
			sessions.get(_name).setAttribute("STATE", st);
			
			if(st.equals(END) || st.equals(IDLE) || st.equals(HOLDON_HOST)) {
				busy = false;
				oppo = null;
			} else {
				busy = true;
				oppo = _name;
			}
			
			SipUser user = users.get(_name);
			if(linkUser.containsKey(_name)) {
				for(Map.Entry<String, PriorityQueue<SipUser> > entry : linkUser.entrySet()) {
					if(entry.getValue().contains(user)) {
						entry.getValue().remove(user);
						entry.getValue().add(user);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			r.unlock();
		}
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public SipUser getAvaliable(String _name) {
		if(linkUser.containsKey(_name)) {
			SipUser cur = linkUser.get(_name).peek();
			if(cur != null)
				return cur;
			else 
				return null;
		} else {
			return null;
		}
	}
	
	public void clean(String _name) {
		if(_name == null)
			return;
		
		r.lock();
		try{
			sessions.remove(_name);
			realUser.remove(realUser.get(_name));
			realUser.remove(_name);
			linkUser.remove(_name);
			
			SipUser user = users.get(_name);
			Iterator<Map.Entry<String, PriorityQueue<SipUser>> > it = linkUser.entrySet().iterator(); 
		    while(it.hasNext()){ 
		        Map.Entry<String, PriorityQueue<SipUser> > entry= it.next(); 
		        
		        while(entry.getValue().contains(user))
					entry.getValue().remove(user);
		        
		        if(entry.getValue().isEmpty()){ 
		            it.remove(); 
		        } 
		    } 
		    
			logger.info(name + " clean " + _name);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			r.unlock();
		}
	}
	
	public void replace(String fromName, String toName) {
		if(fromName == null || toName == null)
			return;
		
		r.lock();
		try{
			PriorityQueue<SipUser> q = linkUser.get(fromName);
			if(q != null) {
				linkUser.remove(fromName);
				SipUser fromUser = users.get(fromName);
				SipUser toUser = users.get(toName);
				for(Map.Entry<String, PriorityQueue<SipUser> > entry : linkUser.entrySet()) {
					if(entry.getValue().contains(fromUser)) {
						entry.getValue().remove(fromUser);
						entry.getValue().add(toUser);
					}
					
					while(entry.getValue().contains(fromUser))
						entry.getValue().remove(fromUser);
				}
				linkUser.put(toName, q);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			r.unlock();
		}
	}
	
	public void dealBye(String fromName, String toName) {
		if(fromName == null || toName == null)
			return;
		
		r.lock();
		try{
			linkUser.remove(fromName);
			PriorityQueue<SipUser> q = linkUser.get(toName);
			if(q != null) {
				SipUser fromUser = users.get(fromName);
				SipUser toUser = users.get(toName);
				q.remove(fromUser);
				for(Map.Entry<String, PriorityQueue<SipUser> > entry : linkUser.entrySet()) {
					if(entry.getValue().contains(fromUser)) {
						entry.getValue().remove(fromUser);
						entry.getValue().add(toUser);
					}
					
					while(entry.getValue().contains(fromUser))
						entry.getValue().remove(fromUser);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			r.unlock();
		}
	}
}