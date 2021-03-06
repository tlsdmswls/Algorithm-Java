import java.io.*;
import java.net.*;
import java.util.*;

public class ClientApplication {
	Socket mySocket = null;

	public static void main(String[] args) {
		ClientApplication client = new ClientApplication();
		Scanner sc = new Scanner(System.in);

		OutputStream out = null;
		DataOutputStream dout = null;
		InputStream in = null;
		DataInputStream din = null;

		int num_req = 1;

		/* TCP 연결 전, 사용자에게 CID를 입력받음 */
		System.out.print("CID(NickName) 입력: ");
		String cid = sc.nextLine();

		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("서버로 연결되었습니다.");

			MessageListener listener = new MessageListener(client.mySocket);
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
				reqMessage(client_req, cid, num_req, dout);
				num_req++; 	// Request message 1번 보낼 때마다 +1
				Thread.sleep(100);

				/* 서버와 클라이언트의 연결을 종료하는 부분 */
				if(listener.quit == 1) {
					try {
						if(din != null)
							din.close();
						if(in != null)
							in.close();
						if(dout != null)
							dout.close();
						if(out != null)
							out.close();
						if(client.mySocket != null) {
							client.mySocket.close();
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Connection Fail.");
		}
	}

	/* 서버로 Request message를 전송하는 메소드 */
	public static void reqMessage(String client_req, String cid, int num_req, DataOutputStream dout) {
		// msg = Request message
		String msg = "Req///" + client_req + "///CID:" + cid + "///Num_Req:" + num_req + "///END_MSG";

		// Base64 인코딩
		base64_encoding(msg);

		try {
			dout.writeUTF(msg);
		} catch (IOException e) {
			System.out.println("입출력 예외 발생...");
		}
	}

	/* Base64로 인코딩하고 콘솔창에 출력하는 메소드 */
	static String base64_encoding(String msg) {
		String base64_enc = Base64.getEncoder().encodeToString(msg.getBytes());
		System.out.println("---------------------------------------------");
		System.out.println("[Request] Base64 인코딩 : " + base64_enc);

		return base64_enc;
	}
}

class MessageListener extends Thread {
	Socket socket;
	int quit = 0;

	MessageListener(Socket _s) {
		this.socket = _s;
	}

	public void run() {
		try {
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);

			while (true) {
				String scode = null;
				
				// 서버로부터 메시지를 받아와서 출력
				String res_msg = din.readUTF();
				System.out.println("[Response] Base64 인코딩 : " + res_msg);
				
				// 서버로부터 받은 메시지를 Base64로 디코딩
				byte[] base64_dec = Base64.getDecoder().decode(res_msg.getBytes());
				String msg = new String(base64_dec);
				System.out.println("[Response] Base64 디코딩 : " + msg);
				System.out.println("---------------------------------------------");
				
				StringTokenizer st = new StringTokenizer(msg, "///");
				for (int i = 0; i < 3; i++) {
					msg = st.nextToken();
					if (i == 1)
						scode = msg;
				}

				/* 사용자의 요청 결과 출력 */
				if (scode.equals("200")) {
					clientList(msg);
				} else if (scode.equals("250")) {
					System.out.println(msg);
					try {
						if(din != null)
							din.close();
						if(in != null)
							in.close();
						if(socket != null)
							socket.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					quit++;
					break;
				} else {
					System.out.println(msg);
				}
				System.out.println("---------------------------------------------");
			}
		} catch (Exception e) {
			System.out.println("듣기 객체에서 예외 발생...");
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