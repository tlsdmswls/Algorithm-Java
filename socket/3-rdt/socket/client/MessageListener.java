package socket.client;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

/* 메시지 리스너 객체 */
public class MessageListener extends Thread {
	Socket socket;
	boolean check_ack = false;
	int num_ack = 1;
	int clientTimer = 1;	// 1이면 0.1초
	ClientApplication c;
	ClientSimulator sm;
	boolean run = true;
	int ack_resend = 0;		// ACK 재전송
	int res_resend = 0;		// Response 재전송
	int true_send = 0;		// 메시지 전송 성공
	boolean ack_status = false;
	boolean res_status = false;
	
	
	MessageListener(Socket _s, ClientApplication _c) {
		this.socket = _s;
		this.c = _c;
	}
	
	MessageListener(Socket _s) {
		this.socket = _s;
	}

	public void run() {
		try {
			ClientSimulator sm = new ClientSimulator(socket);
			
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			
			String msg = null;
			String scode = null;
			StringTokenizer st = null;
			
			while (true) {
				System.out.println(sm.aString);
//				clientTimer = 1;	// 타이머 초기화
//				startTimer();		// 타이머 시작
				
				/* 타이머 시작 후 ACK message를 받아온다. */
				String full_ack_msg = din.readUTF();
				
				
				String full_res_msg = din.readUTF();
				
				if(full_ack_msg != null && full_res_msg != null) {
					System.out.println("ACK: " + full_ack_msg + 
							"\nRES: " + full_res_msg);
				
					msg = full_ack_msg;
					scode = null;
					st = new StringTokenizer(msg, "///");
					msg = st.nextToken();
					
					// ACK 받은 경우
					if(msg.equals("ACK")) {
						msg = st.nextToken();
						ack_status = true;
						System.out.println(full_ack_msg);
						
						
						
					} else {	// ACK 받지 못한 경우
						ack_resend++;
					}
					
					if(ack_status == true) {
						msg = full_res_msg;
						st = new StringTokenizer(msg, "///");
						msg = st.nextToken();
					
						// Response 받은 경우
						if(msg.equals("Res")) {
							System.out.println(full_res_msg);
							res_status = true;
							break;
						} else {	// Response 받지 못한 경우
							res_resend++;
						}
						ack_status = false;
						ClientApplication.num_req++;	// 통신이 정상적으로 이루어졌을 때마다 +1
						full_ack_msg = null;
						full_res_msg = null;
					}
				}

				// 타이머 재시작 후 Response message를 받아온다.
//					startTimer();
				
				// Response message 받은 경우 결과 출력
				if(res_status) {
					msg = st.nextToken();
					scode = msg;
					msg = st.nextToken();
					
					/* 사용자의 요청 결과 출력 */
					if (scode.equals("200")) {
						clientList(msg);
					} else if (scode.equals("250")) {
						System.out.println(msg);
						ClientApplication.quit = true;
						break;
					} else {
						System.out.println(msg);
						
					}
				}
			}
		} catch (Exception e) {
			System.out.println("듣기 객체에서 예외 발생...");
			e.printStackTrace();
		}
	}
	
	/* 타이머를 구동(재시작)하는 메소드 */
	public void startTimer() {
		clientTimer = 0;
		Timer timer = new Timer();
		TimerTask task = new TimerTask(){
		    @Override
		    public void run() {
		    	while(clientTimer < 6) {	// 0.5초동안 실행
    				clientTimer++;			// 실행 횟수 증가
    				try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		}
	    	}
	    };
		timer.schedule(task, 100, 100);	// 0.1초 뒤 실행, 0.1초마다 반복
		task.cancel();
		timer.cancel();
		timer.purge();
	}

	/* 서버에 연결된 클라이언트들을 출력하는 메소드 */
	void clientList(String msg) {
		StringTokenizer st = new StringTokenizer(msg, "***");
		int count = st.countTokens();

		for (int i = 0; i < count - 1; i++) {
			msg = st.nextToken();
			System.out.println(msg);
		}
	}
}