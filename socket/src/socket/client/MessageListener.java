package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Timer;

/* 메시지 리스너 객체 */
public class MessageListener extends Thread {
	Socket socket;
	ClientApplication c;
	ClientTimer t;
	// ClientTimer t = new ClientTimer(socket);

	boolean quit = false;
	boolean check_send;
	int ack = 0;
	int res = 0;

	// static Timer timer = null;

	MessageListener(Socket _s, ClientApplication _c) {
		this.socket = _s;
		this.c = _c;
	}

	public void run() {
		try {
			t = new ClientTimer(socket);

			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			OutputStream out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			while (true) {
				// Request 전송 실패한 경우
				if (!ClientApplication.check_send) {
					continue;
				}

				/* 서버로부터 메시지를 받아와서 출력 */
				String full_msg = din.readUTF();
				String msg = full_msg;
				String scode = null;
				String num_ack = null;
				StringTokenizer st = new StringTokenizer(msg, "///");
				msg = st.nextToken();

				// ACK 받은 경우
				if (msg.equals("ACK")) {
					System.out.println("ACK: " + full_msg);
					msg = st.nextToken();
					num_ack = msg;
					num_ack = num_ack.substring(8);

					/* Req_Num, ACK_Num 비교 */
					// 성공
					if (ClientApplication.num_req == Integer.parseInt(num_ack)) {
						ack++; // ACK 받은 횟수 + 1

					}
					// 실패
					else {

					}
				}

				// Response 받은 경우
				if (msg.equals("Res")) {
					System.out.println("Response: " + full_msg);
					res++; // Response 받은 횟수 + 1
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
				}
			}
		} catch (Exception e) {
			System.out.println("듣기 객체에서 예외 발생...");
			e.printStackTrace();
		}
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