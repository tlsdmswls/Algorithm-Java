import java.io.*;
import java.net.*;
import java.util.*;

public class ClientApplication {
	Socket mySocket = null;
	MessageListener ml = null; // 메시지 리스너 객체
	static ClientSimulator sm; // 클라이언트의 데이터 손실 시뮬레이터용 객체
	static int num_req = 1; // Request message 내 Num_Req의 value
	static String cid = null; // 클라이언트가 입력한 CID
	String client_req = null; // 클라이언트 요청 사항
	static int res_resend = 0; // Response 재전송 카운트
	static int timeout_resend = 0; // 타임아웃에 의한 재전송 카운트

	public static void main(String[] args) {
		ClientApplication client = new ClientApplication();
		Scanner sc = new Scanner(System.in);

		OutputStream out = null;
		DataOutputStream dout = null;
		InputStream in = null;
		DataInputStream din = null;

		/* TCP 연결 전, 사용자에게 CID를 입력받음 */
		System.out.print("CID(NickName) 입력: ");
		cid = sc.nextLine();

		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("서버로 연결되었습니다.");

			MessageListener listener = new MessageListener(client.mySocket, client);
			sm = new ClientSimulator(client.mySocket);
			listener.start();

			out = client.mySocket.getOutputStream();
			dout = new DataOutputStream(out);
			in = client.mySocket.getInputStream();
			din = new DataInputStream(in);

			while (true) {
				System.out.println("\n서버로 보낼 요청에 해당하는 알파벳을 입력하세요.\n" + " a) CID 저장\n" + " b) 현재 시간\n"
						+ " c) TCP 연결 유지 시간\n" + " d) 연결된 모든 클라이언트의 IP주소와 CID\n" + " q) 서버와 연결 종료");
				System.out.print(">> ");
				String client_req = sc.nextLine();

				boolean check_null = (client_req).equals("");
				if (check_null == true) {
					System.out.println("값을 입력하지 않았습니다.");
					continue;
				}

				reqMessage(client_req, cid, dout);
				// num_req++; // Request message 1번 보낼 때마다 +1
				Thread.sleep(100);

				/* 서버와 클라이언트의 연결을 종료하는 부분 */
				if (listener.quit == true) {
					try {
						if (din != null)
							din.close();
						if (in != null)
							in.close();
						if (dout != null)
							dout.close();
						if (out != null)
							out.close();
						if (client.mySocket != null) {
							client.mySocket.close();
							break;
						}
						if (sc != null) {
							sc.close();
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Connection Fail.");
			e.printStackTrace();
		}
		System.out.print("총 재전송 횟수: ");
		System.out.println(timeout_resend + res_resend);
		System.out.println("Time out에 의한 재전송 횟수: " + timeout_resend);
		System.out.println("Response message 재전송 횟수: " + res_resend);
	}

	/* 서버로 Request message를 전송하는 메소드 */
	public static void reqMessage(String client_req, String cid, DataOutputStream dout) {
		// msg = Request message
		String msg = "Req///" + client_req + "///CID:" + cid + "///Num_Req:" + num_req + "///END_MSG";
		sm.sendMessage(msg);
		System.out.println("[Request] : " + msg);
	}
}

/* 메시지 리스너 객체 */
class MessageListener extends Thread {
	Socket socket;
	boolean quit = false;
	boolean check_ack = false;
	int num_ack = 1;
	int clientTimer = 1; // 1이면 0.1초
	ClientApplication c;
	boolean run = true;

	MessageListener(Socket _s, ClientApplication _c) {
		this.socket = _s;
		this.c = _c;
	}

	public void run() {
		try {
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			OutputStream out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			boolean ack_status = false;
			boolean res_status = false;

			while (true) {
				clientTimer = 1;
				startTimer();

				// 타이머 시작 후 ACK message를 받아온다.
				while (clientTimer < 6) {
					String full_msg = din.readUTF();
					String msg = full_msg;
					// String scode = null;
					StringTokenizer st = new StringTokenizer(msg, "///");
					msg = st.nextToken();

					// ACK message 받은 경우
					if (msg.equals("ACK")) {
						System.out.println("[ACK] : " + full_msg);
						ack_status = true;
						break;
					}
				}
				clientTimer = 1;

				// ACK message 받지 못한 경우
				if (ack_status == false) {
					// c.timeout_resend = 0;
					ClientApplication.timeout_resend = 0;
				}

				// 타이머 재시작 후 Response message를 받아온다.
				startTimer();
				System.out.println("재시ㅏㅈㄱ!!!!!!@#!@#@#ㄸㅉㅇㄸㄹㅇㄲㄴㅋㄹㅋㄸㄴㄹㅇ");
				while (clientTimer < 6) {
					String full_msg = din.readUTF();
					String msg = full_msg;
					StringTokenizer st = new StringTokenizer(msg, "///");
					msg = st.nextToken();

					// Response message 받은 경우
					if (msg.equals("Res")) {
						res_status = true;
						break;
					}
				}

				// Response message 받은 경우 결과 출력
				if (res_status == true) {
					String full_msg = din.readUTF();
					String msg = full_msg;
					String scode = null;
					StringTokenizer st = new StringTokenizer(msg, "///");
					msg = st.nextToken();

					System.out.println("[Response] : " + full_msg);
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
					ClientApplication.num_req++;
				}
			}
		} catch (Exception e) {
			System.out.println("듣기 객체에서 예외 발생...");
		}
	}

	/* 타이머를 구동(재시작)하는 메소드 */
	public void startTimer() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				while (clientTimer < 6) {
					try {
						if (run) {
							clientTimer++; // 실행 횟수 증가
							Thread.sleep(100); // 0.1초 단위
						} else {
							timer.cancel();
							timer.purge();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		timer.schedule(task, 100, 100); // 0.1초 뒤 실행, 0.1초마다 반복
		System.out.println("타이머 종료!");
		run = false;
	}

	/* 타이머를 구동(재시작)하는 메소드 */
	// public void startTimer() {
	// Timer timer = new Timer();
	// clientTimer = 1;
	// TimerTask task = new TimerTask(){
	// @Override
	// public void run() {
	// while(true) {
	// try {
	// clientTimer++; // 실행 횟수 증가
	// Thread.sleep(100); // 0.1초 단위
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }}}};
	// timer.schedule(task, 100, 100); // 0.1초 뒤 실행, 0.1초마다 반복
	// }

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

/* 데이터 손실 시뮬레이터용 객체 */
class ClientSimulator {
	ClientApplication c;
	Socket socket;
	Random rd = new Random();
	String status = null; // 데이터 손실 여부를 체크할 문자열 (Loss, NoLoss)
	MessageListener ml;

	ClientSimulator(Socket _s) {
		socket = _s;
	}

	// ClientSimulator(MessageListener _ml) {
	// ml = _ml;
	// }

	/* 서버로 Request message를 전송하는 메소드 */
	public void sendMessage(String msg) {
		OutputStream out;
		try {
			out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			int if_write = rd.nextInt(10);
			if (if_write < 7) {
				dout.writeUTF(msg);
				status = "NoLoss";
			} else {
				status = "Loss";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}