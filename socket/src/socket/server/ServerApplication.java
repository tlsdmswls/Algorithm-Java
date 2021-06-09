import java.io.*;
import java.net.*;
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

			while (true) {
				Socket socket = server.ss.accept();
				c = new Client(socket, server);
				server.clients.add(c);
				c.sm = new ServerSimulator(c);
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
class ServerSimulator {
	Random rd = new Random();
	Client c = null;

	ServerSimulator(Client _c) {
		c = _c;
	}

	/* 클라이언트로 Message를 전송하는 메소드 */
	public void sendMessage(String msg) {
		int if_write = rd.nextInt(10);
		try {
			if (if_write < 7) {
				c.dout.writeUTF(msg);
			} else {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(msg);
	}
}