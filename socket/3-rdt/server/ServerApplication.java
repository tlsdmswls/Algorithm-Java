import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* 서버는 클라이언트에게 Request 메시지를 수신하게 되면 ACK 메시지를 클라이언트에게 송신한다.
 * ACK 송신이 완료되면 Request 요청에 따른 동작을 수행한다. */
public class ServerApplication {
	ServerSocket ss = null;
	ArrayList<Client> clients = new ArrayList<Client>();
	static Client c;
	
	public static void main(String[] args) {
		ServerApplication server = new ServerApplication();
		try {
			server.ss = new ServerSocket(55555);
			System.out.println("서버 소켓이 생성되었습니다.");
		
		while(true) {
			Socket socket = server.ss.accept();
			c = new Client(socket, server);
			server.clients.add(c);
			c.sm = new Simulator(c);
			c.start();
		}
	} catch (SocketException e) {
		System.out.println("소켓 예외 발생...");
	} catch (IOException e) {
		System.out.println("입출력 예외 발생...");
		}
	}
}


/* 데이터 손실 시뮬레이터용 객체 */
class Simulator {
	Random rd = new Random();
	Client c = null;
	String status = null;		// 데이터 손실 여부를 체크할 문자열 (Loss, NoLoss)
		
	Simulator(Client _c) {
		c = _c;
	}
	
	/* 클라이언트에 메시지를 보내는 메소드 */
	public void sendMessage(String type, String msg) {
		int if_write = rd.nextInt(10);
		
		try {
			if(if_write < 7) {
				// ACK message 전송
				if(type.equals("ack")) {
					c.dout.writeUTF(msg);
					status = "NoLoss";
				}
				// Response message 전송
				else if(type.equals("res")){
					c.dout.writeUTF(msg);
					status = "NoLoss";
				}
			}
			else {
				status = "Loss";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


class Client extends Thread {
	Client c;
	Socket socket;
	ServerApplication server;
	ArrayList<String> clientList = new ArrayList<String>();	// 연결된 클라이언트 CID, IP주소
	ArrayList<String> cidList = new ArrayList<String>();	// 저장을 요청한 클라이언트 CID
	Simulator sm;				// 데이터 손실 시뮬레이터 객체
	
	int currentTime = 1;		// 서버 연결 시간
	int scode;					// 상태 코드
	String cid;					// 사용자가 입력한 CID
	String num_req;				// Response message 내 Num_Req의 value
	
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
	
	public Client(Socket _s, Simulator _sm) {
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
			
			sm = new Simulator(server.c);
			
			/* 클라이언트로부터 Request message를 읽어들이고 Request message를 전송 */
			while(true) {
				String msg = din.readUTF();
				StringTokenizer st = new StringTokenizer(msg, "///");
				
				/* 조건문:	사용자의 요청사항
				 * 블록 내부:	Response message 전송을 위한 내용 */
				msg = st.nextToken();
				if(msg.equals("Req")) msg = st.nextToken();
				 
				if(msg.equals("a")) {
					// CID 저장
					cid = st.nextToken();
					cid = cid.substring(cid.lastIndexOf("CID:") + 4);
					cidList.add(cid);
					
					// 상태 코드에 맞는 value 작성 후, 데이터 손실 시뮬레이터용 객체에서 write 수행
					String valueA = resValue(100, cid);
					resMessage(100, valueA);
					
					if(sm.status.equals("NoLoss")) {
						ackMessage(num_req);
					}
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
					cid = st.nextToken();
					cid = cid.substring(cid.lastIndexOf("CID:") + 4);
					String valueD = resValue(200, null);
					resMessage(200, valueD);
				}
				else if(msg.equals("q")){
					// 서버 연결 종료
					String valueQ = resValue(250, null);
					resMessage(250, valueQ);
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
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {	// 요청 메시지 인식 실패
					String valueF = resValue(300, null);
					resMessage(300, valueF);
				}
				System.out.println(sm.status);
			}
		} catch (Exception e) {
			System.out.println("예외 발생...");
			e.printStackTrace();
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
	
	/* 클라이언트로 ACK message를 전송하는 메소드 */
	public void ackMessage(String num_req) {
		// msg = ACK message
		String msg = "ACK///Num_ACK:"+ num_req + "///END_MSG";
		sm.sendMessage(msg, "ack");
	}
	
	/* 클라이언트로 Response message를 전송하는 메소드 */
	public void resMessage(int scode, String value) {
		// ACK message가 정상적으로 전송될 때까지 반복
		while(true) {
			// msg = Response message
			String msg = "Res///" + scode + "///" + value + "///END_MSG";
			sm.sendMessage("res", msg);
			
			// Response message가 정상적으로 전송된 경우 ACK message를 전송
			if(sm.status.equals("NoLoss")) {
				ackMessage(num_req);
				break;
			}
		}
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