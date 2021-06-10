package socket.client;

import java.io.*;
import java.util.*;

/* 메시지 리스너 객체 */
public class MessageListener extends Thread {
	Socket socket;
	boolean quit = false;
	boolean check_ack = false;
	int num_ack = 1;
	int clientTimer = 1; // 1이면 0.1초
	ClientApplication c;
	boolean run = true;
	int res_resend = 0; // Response 재전송 카운트
	int ack_resend = 0; // 타임아웃에 의한 재전송 카운트

	MessageListener(Socket _s, ClientApplication _c) {
		this.socket = _s;
		this.c = _c;
	}

	public MessageListener(int clientTimer) {
		this.clientTimer = clientTimer;
	}

	public void run() {
		try {
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			OutputStream out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			String full_msg = null;
			String msg = null;
			String scode = null;
			StringTokenizer st = null;

			boolean ack_status = false;
			boolean res_status = false;

			while (true) {
				clientTimer = 1; // 타이머를 0.1초로 초기화
				startTimer(); // 타이머 시작

				full_msg = din.readUTF();

				// 타이머 시작 후 ACK message를 받아온다.
				while (clientTimer < 6) {
					msg = full_msg;
					scode = null;
					st = new StringTokenizer(msg, "///");
					msg = st.nextToken();

					// ACK message 받은 경우
					if (msg.equals("ACK")) {
						System.out.println("[ACK] : " + full_msg);
						ack_status = true;
						break;
					}

					// 타이머 재시작 후 Response message를 받아온다.
					startTimer();
					full_msg = din.readUTF();
					while (clientTimer < 6) {
						full_msg = din.readUTF();
						msg = full_msg;
						st = new StringTokenizer(msg, "///");
						msg = st.nextToken();

						// Response message 받은 경우
						if (msg.equals("Res")) {
							res_status = true;
							break;
						}
					}
				}

				// ACK message 받지 못한 경우
				if (ack_status == false) {
					ack_resend++;
					System.out.println("Timeout에 의한 ACK 재전송: " + ack_resend);
				}

				// Response message 받지 못한 경우
				if (res_status == false) {
					res_resend++;
					System.out.println("Timeout에 의한 Response 재전송: " + res_resend);
				}

				// Response message 받은 경우 결과 출력
				if (res_status == true && res_status == true) {
					msg = st.nextToken();
					scode = msg;
					msg = st.nextToken();

					/* 사용자의 요청 결과 출력 */
					if (scode.equals("200")) {
						clientList(msg);
					} else if (scode.equals("250")) {
						System.out.println(msg);
						quit = true;
						try {
							if (quit == true) {
								if (dout != null)
									dout.close();
								if (out != null)
									out.close();
								if (din != null)
									din.close();
								if (in != null)
									in.close();
								if (socket != null)
									socket.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					} else {
						System.out.println(msg);
					}
					// 통신이 정상적으로 이루어졌을 때마다 +1
					c.num_req++;
					System.out.println(c.num_req);
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
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				while (clientTimer < 6) { // 0.5초동안 실행
					clientTimer++; // 실행 횟수 증가
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		timer.schedule(task, 100, 100); // 0.1초 뒤 실행, 0.1초마다 반복
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