import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServerApplication {
	ServerSocket ss = null;
	ArrayList<Client> clients = new ArrayList<Client>();

	public static void main(String[] args) {
		ServerApplication server = new ServerApplication();
		try {
			server.ss = new ServerSocket(55555);
			System.out.println("서버 소켓이 생성되었습니다.");
		
		while(true) {
			Socket socket = server.ss.accept();
			Client c = new Client(socket, server);
			server.clients.add(c);
			c.start();
		}
	} catch (SocketException e) {
		System.out.println("소켓 예외 발생...");
	} catch (IOException e) {
		System.out.println("입출력 예외 발생...");
		}
	}
}

class Client extends Thread {
	Socket socket;
	ServerApplication server;
	ArrayList<String> clientList = new ArrayList<String>();	// 연결된 클라이언트 CID, IP주소
	ArrayList<String> cidList = new ArrayList<String>();	// 저장을 요청한 클라이언트 CID
	
	int currentTime = 1;		// 서버 연결 시간
	int scode;					// 상태 코드
	String cid;					// 사용자가 입력한 CID
	
	OutputStream out = null;
	DataOutputStream dout = null;
	InputStream in = null;
	DataInputStream din = null;
	
	Client(Socket _s, ServerApplication _ss){
		this.socket = _s;
		this.server = _ss;
	}
	
	Client(int currentTime) {
		this.currentTime = currentTime;
	}
	
	public void run() {
		try {
			startTimer();	// CurrentTime을 재기 위한 타이머 시작
			
			out = socket.getOutputStream();
			dout = new DataOutputStream(out);
			in = socket.getInputStream();
			din = new DataInputStream(in);
			
			/* 클라이언트로부터 Request message를 읽어들이고 Request message를 전송한다. */
			while(true) {
				String msg = din.readUTF();
				StringTokenizer st = new StringTokenizer(msg, "///");
				
				/* 조건문:	Request message 중에서 사용자의 요청사항을 조건으로 한다.
				 * 블록 내부:	Response message 전송을 위한 내용 */
				msg = st.nextToken();
				if(msg.equals("Req")) msg = st.nextToken();
				
				if(msg.equals("a")) {
					// CID 저장
					cid = st.nextToken();
					cid = cid.substring(cid.lastIndexOf("CID:") + 4);
					cidList.add(cid);
					// 상태 코드에 맞는 value 작성 후, 클라이언트로 Response message 전송
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
					resMessage(300, resValue(300, null));
				}
			}
		} catch (Exception e) {
			System.out.println("예외 발생...");
			e.printStackTrace();
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
	
	/* Response Message의 상태코드에 맞는 value 값을 구하는 메소드 */
	public String resValue(int scode, String cid) {
		String value = "CID: IP address";
		if (scode == 100) {
			value = "서버에 아이디를 정상적으로 저장했습니다. (" + cid + ")";
		} else if (scode == 130) {
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
	
	/* 클라이언트로 Response message를 전송하는 메소드 */
	public void resMessage(int scode, String value) {
		// msg = Response message
		String msg = "Res///" + scode + "///" + value + "///END_MSG";
		
		// Base64 인코딩
		String base64_enc = Base64.getEncoder().encodeToString(msg.getBytes());
		
		try {
			dout.writeUTF(base64_enc);
		} catch (IOException e) {
			System.out.println("입출력 예외 발생...");
			}
		}
	}