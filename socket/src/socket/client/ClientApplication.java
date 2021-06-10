package socket.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientApplication {
	Socket mySocket = null;
	MessageListener ml = null;	// 메시지 리스너 객체
	static ClientSimulator sm;	// 클라이언트의 데이터 손실 시뮬레이터용 객체
	static int num_req = 1;		// Request message 내 Num_Req의 value
	static String cid = null;	// 클라이언트가 입력한 CID
	String client_req = null;	// 클라이언트 요청 사항
	
	public static void main(String[] args) {
		ClientApplication client = new ClientApplication();
		Scanner sc = new Scanner(System.in);

		OutputStream out = null;
		DataOutputStream dout = null;
		InputStream in = null;
		DataInputStream din = null;
		MessageListener listener = null;
		
		int send = 0;

		/* TCP 연결 전, 사용자에게 CID를 입력받음 */
		System.out.print("CID(NickName) 입력: ");
		cid = sc.nextLine();

		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("서버로 연결되었습니다.");

			listener = new MessageListener(client.mySocket, client);
			sm = new ClientSimulator(client.mySocket);
			listener.start();

			out = client.mySocket.getOutputStream();
			dout = new DataOutputStream(out);
			in = client.mySocket.getInputStream();
			din = new DataInputStream(in);

			while(true) {
				System.out.println("\n서버로 보낼 요청에 해당하는 알파벳을 입력하세요.\n" + " a) CID 저장\n" + " b) 현재 시간\n"
						+ " c) TCP 연결 유지 시간\n" + " d) 연결된 모든 클라이언트의 IP주소와 CID\n" + " q) 서버와 연결 종료");
				System.out.print(">> ");
				String client_req = sc.nextLine();
				
				boolean check_null = (client_req).equals("");
				if(check_null == true) {
					System.out.println("값을 입력하지 않았습니다.");
					continue;
				}
				
				reqMessage(client_req, cid, dout);
				send++;
				Thread.sleep(100);

				/* 서버와 클라이언트의 연결을 종료하는 부분 */
				if(listener.quit == true) {
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
						if(sc != null) {
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
		System.out.println("======================================================");
		System.out.println("Request 메시지 전송 횟수: " + send);
		System.out.println("Request 메시지 전송 성공 횟수: " + num_req);
		System.out.println("Request 메시지 재전송 횟수: " + (send - num_req));
		System.out.println("Time out에 의한 ACK 메시지 재전송 횟수: " + listener.ack_resend);
		System.out.println("Time out에 의한 Request 메시지 재전송 횟수: " + listener.res_resend);
		System.out.println("재전송률: " + ((double)(send - num_req) / (double)(listener.ack_resend + listener.res_resend)));
		System.out.println("======================================================");
	}

	/* 서버로 Request message를 전송하는 메소드 */
	public static void reqMessage(String client_req, String cid, DataOutputStream dout) {
		// msg = Request message
		String msg = "Req///" + client_req + "///CID:" + cid + "///Num_Req:" + num_req + "///END_MSG";
		sm.sendMessage(msg);
		System.out.println("[Request] : " + msg);
	}
}