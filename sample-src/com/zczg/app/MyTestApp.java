package com.zczg.app;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.networkconnection.SdpPortManagerEvent;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zczg.util.CurEnv;
import com.zczg.util.JDBCUtils;
import com.zczg.util.RandomCharUtil;

import testing.DlgcTest;

public class MyTestApp extends DlgcTest {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(MyTestApp.class);
	boolean bTestInvalidMediaTarget = false;
	Boolean ipmsMediaServerType = false;
	Integer loopCount = null;

	public MyTestApp() {
		// TODO Auto-generated constructor stub
	}

	Integer loopInterval = null;
	Boolean bTestReinvite = false;
	public MediaSession mediaSession = null;
	private SipFactory sipFactory;
	private TimerService timerService;
	private CurEnv cur_env = new CurEnv();
	private Map<String, String> authMap;
	private Map<String, SipUser> users;

	private static final String CONTACT_HEADER = "Contact";
	private static final String SUPPORT_HEADER = "Supported";
	private static final String VIA_HEADER = "Via";
	private static final String STANDARD = "standard";
	private static final String WEBRTC = "webrtc";
	private static Map<String, String> clientType;
	private static Map<String, Date> lastCallTime;
	private String realmId;
	
	// TODO
	private static final String AUDIO = "AUDIO";
	private static final String VEDIO = "VEDIO";
	private static Map<String, String> callType;

	@Override
	public void servletInitialized(SipServletContextEvent evt) {

		String sName = evt.getSipServlet().getServletName();

		if ((platform != null) && (platform.compareToIgnoreCase(DlgcTest.TELESTAX_PLATFORM) == 0)
				|| (platform != null) && (platform.compareToIgnoreCase(DlgcTest.ORACLE_PLATFORM) == 0)) {

			if (sName.equalsIgnoreCase("DlgcSipServlet")) {
				dlgcSipServletLoaded = true;
			} else if (sName.equalsIgnoreCase("SipAppP2PServlet")) {
				playerServletInitCalled = true;
			}

			if (playerServletInitCalled && dlgcSipServletLoaded) {
				if (servletInitializedFlag == false) {

					servletInitializedFlag = true;
					initDriver();

					sipFactory = (SipFactory) getServletContext().getAttribute("javax.servlet.sip.SipFactory");
					timerService = (TimerService) getServletContext().getAttribute(TIMER_SERVICE);

					try {
						mediaSession = mscFactory.createMediaSession();
					} catch (MsControlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					authMap = new HashMap<String, String>();
					users = new HashMap<String, SipUser>();
					clientType = new HashMap<String, String>();
					lastCallTime = new HashMap<>();

					// 从数据库中读出来自己的realm_id
					Map<String, Object> map = JDBCUtils.queryForMap(
							"select * from realm where sip_servlet_ip = '" + cur_env.getSettings().get("realm") + "'");
					realmId = String.valueOf(map.get("id"));
					logger.info("本域的id为:" + realmId);

					myServletInitialized(evt);
				} else {

				}
			}
		}
	}

	@Override
	protected void doRegister(SipServletRequest req) throws ServletException, IOException {
		logger.info("Received register request: " + req.getTo());
		logger.info(req.toString());

		String from = req.getFrom().getURI().toString();
		Address contact = req.getAddressHeader(CONTACT_HEADER);
		// String[] ss = contact.split("[@:;]");
		String username = (from.split("[@:;]"))[1];
		String ip = req.getInitialRemoteAddr();
		String port = Integer.toString(req.getInitialRemotePort());

		logger.info("Register User " + username + ":" + contact);

		String auth = req.getHeader("Authorization");
		if (auth == null) {
			SipServletResponse resp = req.createResponse(SipServletResponse.SC_UNAUTHORIZED);

			String nonce = authMap.get(username);
			if (nonce == null) {
				nonce = RandomCharUtil.getRandomNumberUpperLetterChar(32);
				authMap.put(username, nonce);
			}

			// resp.addHeader("Proxy-Authenticate", "Digest realm=\"" +
			// cur_env.getSettings().get("realm") + "\""
			// + ",nonce=\"" + nonce + "\",algorithm=MD5");

			resp.addHeader("WWW-Authenticate", "Digest realm=\"" + cur_env.getSettings().get("realm") + "\""
					+ ",nonce=\"" + nonce + "\",algorithm=MD5");

			resp.send();
			logger.info("Request authenticate for " + from);
		} else {
			Map<String, Object> map = JDBCUtils.queryForMap("select * from user where name = '" + username + "'");

			int st = auth.indexOf("response=\"") + 10;
			int ed = auth.indexOf("\"", st);
			String digest = auth.substring(st, ed);
			st = auth.indexOf("uri=\"") + 5;
			ed = auth.indexOf("\"", st);
			String uri = auth.substring(st, ed);
			String method = req.getMethod();

			String check = cur_env.myDigest(username, cur_env.getSettings().get("realm"), (String) map.get("passwd"),
					authMap.get(username), method, uri);

			if (digest.equals(check)) {
				SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);

				Address address = req.getAddressHeader(CONTACT_HEADER);

				resp.setAddressHeader(CONTACT_HEADER, address);

				int expires = address.getExpires();
				if (expires < 0) {
					expires = req.getExpires();
				}

				if (expires == 0) {
					users.remove(username);
					logger.info("User " + from + " logout!");

				} else {
					users.put(username,
							new SipUser(username, "sip:" + username + '@' + cur_env.getSettings().get("realm"), ip,
									port, (Integer) map.get("priority"), (Boolean) map.get("wait"),
									(String) map.get("preforward_always"), (String) map.get("preforward_busy"),
									(String) map.get("preforward_timeout"), users, address));

					String type = address.getValue();
					if (type.contains("wss")) {
						clientType.put(username, WEBRTC);
					} else {
						clientType.put(username, STANDARD);
					}

					logger.info("User " + from + " registered " + req.getHeader(VIA_HEADER));
				}

				resp.send();
			} else {
				SipServletResponse resp = req.createResponse(SipServletResponse.SC_FORBIDDEN);
				logger.info("User " + from + " registered fail");
				resp.send();
			}
		}
	}

	@Override
	protected void doInfo(SipServletRequest info) throws ServletException, IOException {
		logger.info("Got INFO \n");
	}

	@Override
	public void doInvite(SipServletRequest request) throws ServletException, IOException {

		SipSession session = request.getSession();
		setLock(session);

		try {
			logger.info("Got INVITE:");
			logger.info(request.toString());

			String fromName = ((request.getFrom().getURI().toString()).split("[:@]"))[1];
			String toName = ((request.getTo().getURI().toString()).split("[:@]"))[1];

			// 主叫和被叫的用户名都是有_的
			if (toName.contains("_") == false) {
				request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
				return;
			}

			SipUser fromUser = users.get(fromName);
			// 表示是另一个SipServlet转过来的
			if (fromUser == null) {
				Address from = sipFactory
						.createAddress("sip:" + fromName + "@" + getSipServletIpByUserName(fromName) + ":5080");
				fromUser = new SipUser(fromName, getSipServletIpByUserName(fromName), from, users);
				users.put(fromName, fromUser);
			}

			SipUser toUser = users.get(toName);
			// 查看toName是不是本域，如果是本域且不在就回
			if (toUser == null) {

				if (toName.split("_")[1].equals(realmId)) {
					// 本域的用户且不在线（其实也包括了不存在的情况，因为不存在就一定不会在线），回404
					logger.info("被叫不在线，也许不存在");
					request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
					return;
				} else {
					// 不是本域的用户，根据域id查找对应域的sip-servlet的ip地址
					String sipServletIp = getSipServletIpByUserName(toName);

					// 存到users-map中的key是不带realm-id的
					Address to = sipFactory.createAddress("sip:" + toName + "@" + sipServletIp + ":5080");
					toUser = new SipUser(toName, sipServletIp, to, users);
					users.put(toName, toUser);
				}
			}

			session.setAttribute("CUR_INVITE", request);

			session.setAttribute("USER", fromName);
			session.setAttribute("OPPO", toName);

			fromUser.sessions.put(toName, session);
			fromUser.setState(toName, SipUser.IDLE);

			// TODO 把这打开
			// // 遍历fromUser，如果它和某个人正在通话，不让他发出invite其他人的邀请
			// for (Map.Entry<String, SipSession> entry :
			// fromUser.sessions.entrySet()) {
			// if
			// (entry.getValue().getAttribute("STATE").equals(SipUser.CALLING)
			// && !entry.getKey().equals(toName)) {
			// request.createResponse(SipServletResponse.SC_FORBIDDEN).send();
			// return;
			// }
			// }

			// 对方正在通话中
			for (Map.Entry<String, SipSession> entry : toUser.sessions.entrySet()) {
				if (entry.getValue().getAttribute("STATE").equals(SipUser.CALLING)) {
					request.createResponse(SipServletResponse.SC_BUSY_HERE).send();
					return;
				}
			}

			// 尝试接入
			if (fromUser.compareState(toName, SipUser.IDLE)) {

				logger.info("This is an invite from " + fromName + " to " + toName);

				// request.createResponse(SipServletResponse.SC_RINGING).send();

				Address from = sipFactory.createAddress(fromUser.sipadd + ":5080");
				Address to = sipFactory.createAddress("sip:" + toUser.name + "@" + toUser.ip + ":" + toUser.port);

				// 这个invite的from是sip-servlet
				SipServletRequest invite = sipFactory.createRequest(sipFactory.createApplicationSession(), "INVITE",
						from, to);
				invite.setRequestURI(toUser.contact.getURI());

				if (request.getHeader(SUPPORT_HEADER) != null) {
					invite.setHeader(SUPPORT_HEADER, request.getHeader(SUPPORT_HEADER));
				}

				SipSession linkedSession = invite.getSession();
				toUser.sessions.put(fromName, linkedSession);

				fromUser.setState(toName, SipUser.WAITING_FOR_MEDIA_SERVER);
				toUser.setState(fromName, SipUser.INIT_BRIDGE);

				session.setAttribute("MEDIA_SESSION", mediaSession);
				linkedSession.setAttribute("MEDIA_SESSION", mediaSession);

				linkedSession.setAttribute("CUR_INVITE", invite);
				linkedSession.setAttribute("USER", toName);
				linkedSession.setAttribute("OPPO", fromName);

				shareLock(session, linkedSession);

				createConnWithMS(session);
			} else {
				
				// a发起跟b的呼叫，但是a的状态却不是idle，这里说明a的状态有问题，拒绝发起呼叫
				SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
				
			}
		} catch (Exception e) {
			request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
			e.printStackTrace();
		} finally {
			setUnLock(session);
		}
	}

	@Override
	protected void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException {
		SipSession session = resp.getSession();
		setLock(session);
		try {
			logger.info("Got OK");

			ServletTimer st = (ServletTimer) session.getAttribute("TIMER");
			if (st != null)
				st.cancel();

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			if (fromUser == null) { // OK for MESSAGE
				logger.info("Got OK for MESSAGE !");
			} else {
				if (session.isValid()) {
					String cSeqValue = resp.getHeader("CSeq");
					if (cSeqValue.indexOf("INVITE") != -1) {

						if (fromUser.compareState(toName, SipUser.INIT_BRIDGE)) {
							byte[] sdpAnswer = resp.getRawContent();
							if (sdpAnswer == null) {
								sdpAnswer = (byte[]) session.getAttribute("180SDP");
								if (sdpAnswer == null) {
									// TODO internal error
								}
							}

							SipServletRequest ack = resp.createAck();
							session.setAttribute("PREPARE_ACK", ack);

							fromUser.setState(toName, SipUser.ANSWER_BRIDGE);

							answerSDP(session, sdpAnswer);
						}
					} else if (fromUser.compareState(toName, SipUser.END)) {
						logger.info("OK for BYE/CANCEL/...");
						releaseSession(resp.getSession());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setUnLock(session);
		}
	}

	/**
	 * 1xx
	 */
	@Override
	protected void doProvisionalResponse(SipServletResponse resp) throws ServletException, IOException {
		SipSession session = resp.getSession();
		setLock(session);
		try {
			logger.info("Got RESPONSE: \n" + resp.toString());

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			if (fromUser.compareState(toName, SipUser.INIT_BRIDGE)) {
				byte[] sdpOffer = resp.getRawContent();
				if (sdpOffer != null)
					session.setAttribute("180SDP", sdpOffer);
			}

			SipSession linkedSession = getLinkedSession(fromName, toName);
			if (linkedSession != null && linkedSession.isValid()) {
				SipServletRequest request = (SipServletRequest) linkedSession.getAttribute("CUR_INVITE");
				request.createResponse(resp.getStatus()).send();
			}

		} finally {
			setUnLock(session);
		}
	}

	// @Override
	// protected void doBranchResponse(SipServletResponse resp) throws
	// ServletException, IOException {
	// // TODO Auto-generated method stub
	// super.doBranchResponse(resp);
	// }

	@Override
	protected void doErrorResponse(SipServletResponse resp) throws ServletException, IOException {
		SipSession session = (SipSession) resp.getSession();
		setLock(session);
		try {
			logger.info("Got ERROR: \n" + resp.toString());

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			// ACK is auto send by the container
			if (session != null && session.isValid()) {
				ServletTimer st = (ServletTimer) session.getAttribute("TIMER");
				if (st != null)
					st.cancel();

				SipSession linkedSession = getLinkedSession(fromName, toName);
				if (linkedSession != null && linkedSession.isValid()) {
					SipServletRequest request = (SipServletRequest) linkedSession.getAttribute("CUR_INVITE");
					SipServletResponse re = request.createResponse(resp.getStatus());
					toUser.setState(fromName, SipUser.END);
					re.send();
					logger.info(re.toString());
					releaseSession(linkedSession);
				}

				fromUser.setState(toName, SipUser.END);
				releaseSession(session);
			}
		} finally {
			setUnLock(session);
		}
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException, IOException {
		SipSession session = request.getSession();
		setLock(session);

		try {
			logger.info("Got ACK: \n" + request.toString());

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			if (session != null && session.isValid()) {
				if ((fromUser.compareState(toName, SipUser.WAITING_FOR_ACK)
						&& toUser.compareState(fromName, SipUser.WAITING_FOR_ACK))) {
					fromUser.setState(toName, SipUser.CALLING);
					toUser.setState(fromName, SipUser.CALLING);
				} else if (fromUser.compareState(toName, SipUser.END)) {
					logger.info("ACK for error...");
					releaseSession(session);
				}
			}

		} finally {
			setUnLock(session);
		}
	}

	@Override
	public void doCancel(SipServletRequest request) throws ServletException, IOException {

		SipSession session = (SipSession) request.getSession();
		setLock(session);

		try {
			logger.info("Got CANCEL: \n" + request.toString());

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			fromUser.setState(toName, SipUser.END);
			// SipServletResponse ok =
			// request.createResponse(SipServletResponse.SC_OK);
			// ok.send();

			SipSession linkedSession = getLinkedSession(fromName, toName);
			if (linkedSession != null && linkedSession.isValid()) {
				SipServletRequest origin = (SipServletRequest) linkedSession.getAttribute("CUR_INVITE");
				SipServletRequest cancel = origin.createCancel();
				toUser.setState(fromName, SipUser.END);
				cancel.send();
				logger.info(cancel.toString());
			}

			releaseSession(session);
		} finally {
			setUnLock(session);
		}
	}

	@Override
	public void doBye(SipServletRequest request) throws ServletException, IOException {
		SipSession session = (SipSession) request.getSession();
		setLock(session);

		try {
			logger.info("Got BYE: \n" + request.toString());

			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			fromUser.setState(toName, SipUser.END);
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
			sipServletResponse.send();

			SipSession linkedSession = getLinkedSession(fromName, toName);
			if (linkedSession != null && linkedSession.isValid()) {
				SipServletRequest bye = linkedSession.createRequest("BYE");
				toUser.setState(fromName, SipUser.END);
				bye.send();
				logger.info("Bye to " + toName);
			}

			releaseSession(session);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setUnLock(session);
		}
	}

	@Override
	protected void doMessage(SipServletRequest request) throws ServletException, IOException {

		request.createResponse(SipServletResponse.SC_OK).send();

		logger.info("receive message : \r\n" + request.toString());

		String fromName = ((request.getFrom().getURI().toString()).split("[:@]"))[1];
		String toName = ((request.getTo().getURI().toString()).split("[:@]"))[1];

		logger.info("Message from " + fromName + " to " + toName);

		SipUser toUser = users.get(toName);

		if (toName.split("_")[1].equals(realmId)) { // 对方是本域用户
			if (toUser == null) { // 本域的用户且不在线，回404
				request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
				return;
			} else { // 对方在线
				// Address to = sipFactory.createAddress("sip:" + toName + "@" +
				// toUser.ip + ":" + toUser.port);
				if (request.isInitial()) {
					Proxy proxy = request.getProxy();
					proxy.setRecordRoute(true);
					proxy.setSupervised(true);
					proxy.proxyTo(toUser.contact.getURI()); // important
				}
			}
		} else { // 对方不是本域用户
			String sipServletIp = getSipServletIpByUserName(toName); // 根据toName查找那个sip-servlet的地址
			Address to = sipFactory.createAddress("sip:" + toName + "@" + sipServletIp + ":5080");

			if (request.isInitial()) {
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(false);
				proxy.setSupervised(true);
				proxy.proxyTo(to.getURI());

				logger.info("转发成功");
			}
		}

	}

	// 超时前转以及其余超时情况处理
	public void timeout(ServletTimer st) {
		logger.info("Time out");
		final SipServletRequest request = (SipServletRequest) st.getInfo();

		SipSession session = (SipSession) request.getSession();
		setLock(session);
		try {
			String fromName = (String) session.getAttribute("USER");
			SipUser fromUser = users.get(fromName);
			String toName = (String) session.getAttribute("OPPO");
			SipUser toUser = users.get(toName);

			fromUser.setState(toName, SipUser.END);
			request.createResponse(SipServletResponse.SC_REQUEST_TIMEOUT).send();

			SipSession linkedSession = getLinkedSession(fromName, toName);
			if (linkedSession != null && linkedSession.isValid()) {
				SipServletRequest origin = (SipServletRequest) linkedSession.getAttribute("CUR_INVITE");
				SipServletRequest cancel = origin.createCancel();
				toUser.setState(fromName, SipUser.END);
				cancel.send();
				logger.info(cancel.toString());
			}

			releaseSession(session);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setUnLock(session);
		}
	}

	protected void releaseSession(SipSession session) {
		if (session != null && session.isValid()) {

			destroyLock(session);

			SipUser from = users.get((String) session.getAttribute("USER"));
			SipUser to = users.get((String) session.getAttribute("OPPO"));
			from.clean(to.name);

			Thread thread = new Thread(new Task((NetworkConnection) session.getAttribute("NETWORK_CONNECTION"),
					(MediaGroup) session.getAttribute("MEDIAGROUP")));
			session.removeAttribute("NETWORK_CONNECTION");
			session.removeAttribute("MEDIAGROUP");
			// session.invalidate();

			thread.start();
		}
	}

	private class MyRtpPortsListener implements MediaEventListener<SdpPortManagerEvent> {
		public void onEvent(SdpPortManagerEvent event) {
			logger.info("Got EVENT " + event.getSource().getContainer().getURI());
			logger.info(event.toString());
			MediaSession mediaSession = event.getSource().getMediaSession();
			SipSession sipSession = (SipSession) mediaSession
					.getAttribute("SIP_SESSION" + event.getSource().getContainer().getURI());
			setLock(sipSession);

			try {
				String fromName = (String) sipSession.getAttribute("USER");
				String toName = (String) sipSession.getAttribute("OPPO");
				SipUser fromUser = users.get(fromName);
				SipUser toUser = users.get(toName);

				SipSession linkedSession = getLinkedSession(fromName, toName);
				SipServletRequest inv = (SipServletRequest) sipSession.getAttribute("CUR_INVITE");

				if (linkedSession != null)
					logger.info(sipSession.getAttribute("STATE") + " : " + linkedSession.getAttribute("STATE"));

				try {
					if (event.isSuccessful()) {
						if (fromUser.compareState(toName, SipUser.WAITING_FOR_MEDIA_SERVER)) {
							if (linkedSession != null) {
								if (toUser.compareState(fromName, SipUser.INIT_BRIDGE)) {
									
									// 用户邀请被叫的invite
									SipServletRequest request = (SipServletRequest) linkedSession
											.getAttribute("CUR_INVITE");

									if (request != null) {

										// 用户回给主叫的ok消息
										SipServletResponse resp = inv.createResponse(SipServletResponse.SC_OK);
										byte[] sdp = event.getMediaServerSdp();
										resp.setContent(sdp, "application/sdp");
										resp.getSession().setAttribute("PREPARE_OK", resp);
										
										// 主叫状态设置为WAITING_FOR_BRIDGE
										fromUser.setState(toName, SipUser.WAITING_FOR_BRIDGE);

										// 这里没有直接去邀请被叫而是generateSDP，这样带着ms的sdp去邀请被叫
										gernateSDP(linkedSession);
									}
								} else {
									logger.error("undefined");
								}
							}
						} else if (fromUser.compareState(toName, SipUser.INIT_BRIDGE)) {
							// generateSDP完了以后会进到这里
							byte[] sdp = event.getMediaServerSdp();
							inv.setContent(sdp, "application/sdp");
							inv.send();
						} else if (fromUser.compareState(toName, SipUser.ANSWER_BRIDGE)) {
							// 被叫回了200ok以后进到这里
							
							logger.info("finish SDP");

							NetworkConnection conn1 = (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION");
							NetworkConnection conn2 = (NetworkConnection) linkedSession
									.getAttribute("NETWORK_CONNECTION");

							conn1.join(Direction.DUPLEX, conn2);
							conn2.join(Direction.DUPLEX, conn1);

							fromUser.setState(toName, SipUser.WAITING_FOR_ACK);
							toUser.setState(fromName, SipUser.WAITING_FOR_ACK);

							SipServletResponse resp2 = (SipServletResponse) linkedSession.getAttribute("PREPARE_OK");
							resp2.send();

							SipServletRequest ack = (SipServletRequest) sipSession.getAttribute("PREPARE_ACK");
							byte[] sdp = event.getMediaServerSdp();
							ack.setContent(sdp, "application/sdp");
							ack.send();
						}
					} else {
						if (SdpPortManagerEvent.SDP_NOT_ACCEPTABLE.equals(event.getError())) {
							// Send 488 error response to INVITE
							inv.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE_HERE).send();
						} else if (SdpPortManagerEvent.RESOURCE_UNAVAILABLE.equals(event.getError())) {
							// Send 486 error response to INVITE
							inv.createResponse(SipServletResponse.SC_BUSY_HERE).send();
						} else {
							// Some unknown error. Send 500 error response to
							// INVITE
							inv.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR).send();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				setUnLock(sipSession);
			}
		}
	}

	protected void terminate(SipSession sipSession, MediaSession mediaSession) {
		setLock(sipSession);
		try {
			SipUser user = users.get((String) sipSession.getAttribute("USER"));
			user.setState((String) sipSession.getAttribute("OPPO"), SipUser.END);
			sipSession.createRequest("BYE").send();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			setUnLock(sipSession);
		}
	}

	class MyPlayerListener implements MediaEventListener<PlayerEvent> {

		public void onEvent(PlayerEvent event) {
			log("Play terminated with: " + event);
			// Release the call and terminate
			MediaSession mediaSession = event.getSource().getMediaSession();
			SipSession sipSession = (SipSession) mediaSession
					.getAttribute("SIP_SESSION" + event.getSource().getContainer().getURI());
			terminate(sipSession, mediaSession);
		}
	}

	private SipSession getLinkedSession(String fromName, String toName) {
		try {
			return users.get(toName).sessions.get(fromName);
		} catch (Exception e) {
			logger.warn("No user");
			return null;
		}
	}

	private NetworkConnection createConnWithMS(SipSession session) throws MsControlException, IOException {

		SipServletRequest request = (SipServletRequest) session.getAttribute("CUR_INVITE");

		NetworkConnection conn = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
		SdpPortManager sdpManag = conn.getSdpPortManager();

		MyRtpPortsListener networkConnectionListener = new MyRtpPortsListener();
		sdpManag.addListener(networkConnectionListener);

		session.setAttribute("NETWORK_CONNECTION", conn);
		mediaSession.setAttribute("SIP_SESSION" + conn.getURI(), session);

		byte[] sdpOffer = request.getRawContent();
		if (sdpOffer == null) {
			sdpOffer = (byte[]) session.getAttribute("SDP");
			if (sdpOffer == null) {
				logger.error("NO SDP");
			}
			
			// TODO 在这里判断呼叫类型
			
			
		}

		conn.getSdpPortManager().processSdpOffer(sdpOffer);
		return conn;
	}

	private void gernateSDP(SipSession session) throws MsControlException, IOException {

		NetworkConnection conn = mediaSession.createNetworkConnection(NetworkConnection.BASIC);
		SdpPortManager sdpManag = conn.getSdpPortManager();

		// add by zlren
		String username = (String) session.getAttribute("USER");
		if (clientType.get(username) != null && clientType.get(username).equals(WEBRTC)) {
			Parameters sdpConfiguration = mediaSession.createParameters();
			Map<String, String> configurationData = new HashMap<String, String>();
			configurationData.put("webrtc", "yes");
			sdpConfiguration.put(SdpPortManager.SIP_HEADERS, configurationData);
			conn.setParameters(sdpConfiguration);
		}

		MyRtpPortsListener networkConnectionListener = new MyRtpPortsListener();
		sdpManag.addListener(networkConnectionListener);

		session.setAttribute("NETWORK_CONNECTION", conn);
		mediaSession.setAttribute("SIP_SESSION" + conn.getURI(), session);

		sdpManag.generateSdpOffer();
	}

	private void answerSDP(SipSession session, byte[] sdpAnswer) throws MsControlException, IOException {
		
		NetworkConnection conn = (NetworkConnection) session.getAttribute("NETWORK_CONNECTION");
		SdpPortManager sdpManag = conn.getSdpPortManager();

		MyRtpPortsListener networkConnectionListener = new MyRtpPortsListener();
		sdpManag.addListener(networkConnectionListener);

		sdpManag.processSdpAnswer(sdpAnswer);
	}

	// protected void releaseDialog(SipSession sipSession, NetworkConnection
	// conn) {
	// MediaGroup mg = (MediaGroup) sipSession.getAttribute("MEDIAGROUP");
	//
	// if (mg != null) {
	// if (conn != null)
	// try {
	// mg.unjoin(conn);
	// } catch (MsControlException e) {
	// e.printStackTrace();
	// }
	//
	// mg.release();
	// sipSession.removeAttribute("MEDIAGROUP");
	// }
	// }

	protected void runDialog(SipSession sipSession, String src) {
		URI prompt = URI.create(src);
		try {
			MediaGroup mg = null;
			mg = (MediaGroup) sipSession.getAttribute("MEDIAGROUP");
			if (mg == null) {
				mg = mediaSession.createMediaGroup(MediaGroup.PLAYER);
				sipSession.setAttribute("MEDIAGROUP", mg);
				MyPlayerListener playerListener = new MyPlayerListener();
				mg.getPlayer().addListener(playerListener);
				mg.join(Direction.DUPLEX, (NetworkConnection) sipSession.getAttribute("NETWORK_CONNECTION"));
			}

			// Play prompt
			Parameters playParams = mscFactory.createParameters();
			playParams.put(Player.REPEAT_COUNT, new Integer(10000));
			playParams.put(Player.INTERVAL, new Integer(2));
			mg.getPlayer().play(prompt, RTC.NO_RTC, playParams);
			logger.info("Play " + prompt);
		} catch (Exception e) {
			// TODO internal error
			e.printStackTrace();
			return;
		}
	}

	private void setLock(SipSession session) {
		if (session.isValid()) {
			ReentrantLock r = (ReentrantLock) session.getAttribute("LOCK");
			if (r == null) {
				r = new ReentrantLock();
				session.setAttribute("LOCK", r);
			}
			logger.info("SET LOCK " + r.toString());
			r.lock();
		}
	}

	private void setUnLock(SipSession session) {
		if (session.isValid()) {
			ReentrantLock r = (ReentrantLock) session.getAttribute("LOCK");
			if (r != null && r.isLocked()) {
				logger.info("SET UNLOCK " + r.toString());
				r.unlock();
			}
		}
	}

	private void destroyLock(SipSession session) {
		if (session.isValid()) {
			ReentrantLock r = (ReentrantLock) session.getAttribute("LOCK");
			if (r != null && r.isLocked()) {
				logger.info("SET UNLOCK " + r.toString());
				r.unlock();
			}

			session.removeAttribute("LOCK");
		}
	}

	private void shareLock(SipSession src, SipSession dest) {
		setLock(dest);
		ReentrantLock r0 = (ReentrantLock) dest.getAttribute("LOCK");
		ReentrantLock r = (ReentrantLock) src.getAttribute("LOCK");
		if (r != null) {
			dest.setAttribute("LOCK", r);
		} else {
			r = new ReentrantLock();
			src.setAttribute("LOCK", r);
			dest.setAttribute("LOCK", r);
		}
		r0.unlock();
	}

	private String getSipServletIpByUserName(String username) {

		if (!username.contains("_")) {
			return null;
		}

		Map<String, Object> map = JDBCUtils
				.queryForMap("select * from realm where id = '" + username.split("_")[1] + "'");
		String sipServletIp = (String) map.get("sip_servlet_ip");
		return sipServletIp;
	}

	/**
	 * 用于释放xms资源
	 * 
	 * @author zlren
	 *
	 */
	private class Task implements Runnable {

		public NetworkConnection networkConnection;
		public MediaGroup mediaGroup;

		public Task(NetworkConnection networkConnection, MediaGroup mediaGroup) {
			super();
			this.networkConnection = networkConnection;
			this.mediaGroup = mediaGroup;
		}

		@Override
		public void run() {
			if (networkConnection != null) {
				if (mediaGroup != null) {
					try {
						mediaGroup.unjoin(networkConnection);
					} catch (MsControlException e) {
						e.printStackTrace();
					}
					mediaGroup.release();
				}
				networkConnection.release();
			}
		}
	}
}
