package client;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientApplication {
	Socket mySocket = null;
	static ClientSimulator sm; // 클라이언트의 데이터 손실 시뮬레이터용 객체
	static int num_req = 1; // Request message 내 Num_Req의 value
	static String cid = null; // 클라이언트가 입력한 CID
	String client_req = null; // 클라이언트 요청 사항
	public static boolean check_send;
	static int res_resend = 0; // Response 재전송 카운트
	static int ack_resend = 0; // ACK 재전송 카운트

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

		int send = 0; // Request 총 전송 횟수

		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("\n서버로 연결되었습니다.\n");

			MessageListener listener = new MessageListener(client.mySocket, client);
			sm = new ClientSimulator(client.mySocket);
			listener.start();

			out = client.mySocket.getOutputStream();
			dout = new DataOutputStream(out);
			in = client.mySocket.getInputStream();
			din = new DataInputStream(in);

			while (true) {
				System.out.println("서버로 보낼 요청에 해당하는 알파벳을 입력하세요.\n" + " a) CID 저장\n" + " b) 현재 시간\n"
						+ " c) TCP 연결 유지 시간\n" + " d) 연결된 모든 클라이언트의 IP주소와 CID\n" + " q) 서버와 연결 종료");
				System.out.print(">> ");
				String client_req = sc.nextLine();

				boolean check_null = (client_req).equals("");
				if (check_null == true) {
					System.out.println("값을 입력하지 않았습니다.");
					continue;
				}

				reqMessage(client_req, cid);
				send++;

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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				System.out.println("\n========================================\n");
			}
		} catch (Exception e) {
			System.out.println("Connection Fail.");
			e.printStackTrace();
		}

		System.out.println("========================================");
		System.out.println("Request 총 전송 횟수: " + send);
		System.out.println("   Request 전송 성공 횟수: " + (num_req - 1));
		System.out.println("   Request 재전송 횟수: " + (send - num_req - 1));
		System.out.println("타임아웃에 의한 재전송 횟수: " + (ack_resend + res_resend));
		System.out.println("   ACK 재전송 횟수: " + ack_resend);
		System.out.println("   Request 재전송 횟수: " + res_resend);
		System.out.println("재전송률: " + ((double) (send - num_req - 1) / (double) (ack_resend + res_resend - 1)));
		System.out.println("======================================================");
	}

	/* 서버로 Request message를 전송하는 메소드 */
	public static void reqMessage(String client_req, String cid) {
		// msg = Request message
		String msg = "Req///" + client_req + "///CID:" + cid + "///Num_Req:" + num_req + "///END_MSG";
		check_send = sm.sendMessage(msg);
	}
}