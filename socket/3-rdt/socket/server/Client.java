package socket.server;
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Client extends Thread {
	Client c;
	Socket socket;
	ServerApplication server;
	ArrayList<String> clientList = new ArrayList<String>();	// 연결된 클라이언트 CID, IP주소
	ArrayList<String> cidList = new ArrayList<String>();	// 저장을 요청한 클라이언트 CID
	ServerSimulator sm;			// 데이터 손실 시뮬레이터 객체
	
	int currentTime = 1;		// 서버 연결 시간
	int scode;					// 상태 코드
	String msg;					// 클라이언트로부터 읽어들인 메시지
	String cid;					// 사용자가 입력한 CID
	String num_req;				// Response message 내 Num_Req의 value
	int num_ack = 1;			// ACK message 내 Num_ACK의 value
	boolean check_send = false; // req_num과 ack_num이 같으면 true
	boolean close = false;		// 클라이언트가 연결 종료를 요청하면 true
	
	OutputStream out = null;
	DataOutputStream dout = null;
	InputStream in = null;
	DataInputStream din = null;
	
	/* 생성자 */
	Client(Socket _s, ServerApplication _ss) {
		this.socket = _s;
		this.server = _ss;
	}
	
	Client(int currentTime) {
		this.currentTime = currentTime;
	}
	
	public Client(Socket _s, ServerSimulator _sm) {
		this.socket = _s;
		this.sm = _sm;
	}

	public void run() {
		try {
			startTimer();
			
			out = socket.getOutputStream();
			dout = new DataOutputStream(out);
			in = socket.getInputStream();
			din = new DataInputStream(in);
			
			sm = new ServerSimulator(ServerApplication.c);
			
			/* 클라이언트로부터 Request 메시지를 읽어들이고 메시지 전송 */
			while(true) {
				msg = din.readUTF();
				num_req = msg;
				StringTokenizer st_msg = new StringTokenizer(msg, "///");
				StringTokenizer st_num = new StringTokenizer(num_req, "///");
				
				msg = st_msg.nextToken();
				
				/* 클라이언트에게 Request message를 받은 경우 */
				if(msg.equals("Req")) {
					msg = st_msg.nextToken();
					
					for (int i = 0; i < 4; i++) {
						num_req = st_num.nextToken();
					}
					num_req = num_req.substring(8);

					System.out.println("ack"+num_ack);
					System.out.println("req"+num_req);
					
					sendAckMessage();	// ACK 전송
					dout.flush();
					sendResMessage(msg, st_msg); // Response 전송
					dout.flush();
					
					/* num_req와 num_ack를 비교한다.*/
					check_send = Integer.parseInt(num_req) == num_ack;
					
					// 통신이 정상적으로 이루어졌을 때마다 +1
					if(check_send) {
						num_ack++;
					}
					
//					/* num_req와 num_ack를 비교한다.*/
//					check_send = Integer.parseInt(num_req) == num_ack;
//					
//					if(check_send) {
//						num_ack++;			// 통신이 정상적으로 이루어졌을 때마다 +1
//						sendAckMessage();	// ACK 전송
//						sendResMessage(msg, st_msg); // Response 전송
//					}

					if(close == true) {
						try {
							if(din != null)
								din.close();
							if(in != null)
								in.close();
							if(dout != null)
								dout.close();
							if(out != null)
								out.close();
							if(socket != null) {
								server.clients.remove(this);
								socket.close();
								close = true;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}				
	}
	
	/* Response message를 전송하는 메소드 */
	public void sendResMessage(String msg, StringTokenizer st_msg) {
		/* 조건문:	사용자의 요청사항
		 * 블록 내부:	Response message 전송을 위한 내용 */				
		if(msg.equals("a")) {
			// CID 저장
			cid = st_msg.nextToken();
			cid = cid.substring(cid.lastIndexOf("CID:") + 4);
			cidList.add(cid);
			
			// 상태 코드에 맞는 value 작성 후, 데이터 손실 시뮬레이터용 객체에서 write 수행
			String valueA = resValue(100, cid);
			resMessage(100, valueA);
		}
		else if(msg.equals("b")) {
			// 현재 시간
			String valueB = resValue(130, null);
			resMessage(130, valueB);
		}
		else if(msg.equals("c")) {
			// 클라이언트와의 연결 시간
			String valueC = resValue(150, null);
			resMessage(150, valueC);
		}
		else if(msg.equals("d")) {
			// 클라이언트 CID, IP주소 리스트
			cid = st_msg.nextToken();
			cid = cid.substring(cid.lastIndexOf("CID:") + 4);
			String valueD = resValue(200, null);
			resMessage(200, valueD);
		}
		else if(msg.equals("q")){
			// 서버 연결 종료
			String valueQ = resValue(250, null);
			resMessage(250, valueQ);
			close = true;
		}
		else {	// 요청 메시지 인식 실패
			String valueF = resValue(300, null);
			resMessage(300, valueF);
		}
	}
	

	/* Response Message의 상태코드에 맞는 value 값을 구하는 메소드 */
	public String resValue(int scode, String cid) {
		String value = "CID: IP address";
		if (scode == 100) {
			value = "서버에 아이디를 정상적으로 저장했습니다. (" + cid + ")";
		}
		else if (scode == 130) {
			LocalDateTime time = LocalDateTime.now();
			value = time.format(DateTimeFormatter.ofPattern("현재 시간: a hh시 mm분"));
		}
		else if (scode == 150) {
			value = "서버와의 연결 시간: " + currentTime + "초";
		}
		else if (scode == 200) {
			getClientList();
			// value = CID: IP *** CID: ... *** END_ClientList
			StringBuffer sb = new StringBuffer();
			for(String s : clientList) {
				sb.append(s + "***");
			}
			sb.append("END_ClientList");
			value = sb.toString();
		} else if (scode == 250) {
			value = "서버와의 연결이 정상적으로 종료되었습니다.";
		} else if (scode == 300) {
			value = "요청 메시지 인식에 실패했습니다.";
		}
		return value;
	}

	/* 데이터 손실 시뮬레이터용 객체에서 ACK message를 전송하는 메소드 */
	public void sendAckMessage() {
		// msg = ACK message
		String msg = "ACK///Num_ACK:"+ num_ack + "///END_MSG";
		sm.sendMessage(msg);
	}
	
	/* 데이터 손실 시뮬레이터용 객체에서 Response message를 전송하는 메소드 */
	public void resMessage(int scode, String value) {
		// msg = Response message
		String msg = "Res///" + scode + "///" + value + "///END_MSG";
		sm.sendMessage(msg);
	}

	/* CurrentTime을 재기 위한 메소드 */
	public void startTimer() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask(){
		    @Override
		    public void run() {
		    	while(true) {
		    		try {
		    			currentTime++;		// 실행 횟수 증가
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
		    	}
		    }	
		};
		timer.schedule(task, 1000, 1000);	// 1초 뒤 실행, 1초마다 반복
	}
	
	/* 연결된 클라이언트의 CID와 IP주소를 구하는 메소드 */
	public void getClientList() {
		String clientIp = null;
		String clientId = null;
		
		clientList.clear();
		for(int i = 0; i < server.clients.size(); i++) {
			clientId = server.clients.get(i).cid;
			clientIp = server.clients.get(i).socket.getInetAddress().toString();
			clientIp = clientIp.substring(clientIp.lastIndexOf("/") + 1);
			clientList.add(clientId + " " + clientIp);
		}
	}
}