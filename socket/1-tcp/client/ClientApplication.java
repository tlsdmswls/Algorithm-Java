import java.io.*;
import java.net.*;
import java.util.*;

public class ClientApplication {
	Socket mySocket = null;	// 소켓
	
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
			System.out.println("Client > 서버 연결");
			
			MessageListener listener = new MessageListener(client.mySocket);
			listener.start();
			
			out = client.mySocket.getOutputStream();
			dout = new DataOutputStream(out);
			in = client.mySocket.getInputStream();
			din = new DataInputStream(in);
			
			while(true) {
				System.out.println("\n서버로 보낼 요청에 해당하는 알파벳을 입력하세요.\n"
						+ " a) CID 저장\n"
						+ " b) 현재 시간\n"
						+ " c) TCP 연결 유지 시간\n"
						+ " d) 연결된 모든 클라이언트의 IP주소와 CID\n"
						+ " q) 서버와 연결 종료");
				System.out.print(">> ");
				String client_req = sc.nextLine();
				reqMessage(client_req, cid, num_req, dout);
				num_req++;			// Request message 1번 보낼 때마다 +1
				Thread.sleep(100);
				
				if(listener.quit == 1) {
					client.mySocket.close();
					out.close();
					in.close();
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Connection Fail.");
		}
	}
	
	/* 서버로 Request message 전송 */
	public static void reqMessage(String client_req, String cid, int num_req, DataOutputStream dout) {
		try {
			dout.writeUTF("Req///" + client_req + "///CID:" + cid + "///Num_Req:" + num_req + "///END_MSG");
		} catch (IOException e) {
			System.out.println("입출력 예외 발생...");
		}
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
			
			while(true) {
				String msg = din.readUTF();
				String scode = null;
				
				StringTokenizer st = new StringTokenizer(msg, "///");
				for(int i = 0; i < 3; i++) {
					msg = st.nextToken();
					if(i == 1) scode = msg;
				}
				
				/* 사용자의 요청 결과 출력 */
				if(scode.equals("200")) {
					clientList(scode, msg);
				} else {
					System.out.println(msg);
				}
				
				if(scode.equals("250")) {
					quit++;
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("듣기 객체에서 예외 발생...");
		}
	}
	
	/* 서버에 연결된 클라이언트들을 출력하는 메소드 */
	void clientList(String scode, String msg) {
		if(scode.equals("200")) {
			StringTokenizer st = new StringTokenizer(msg, "***");
			for(int i = 0; i < st.countTokens(); i++) {
				msg = st.nextToken();
				System.out.println(msg);
			}
		}
	}
}