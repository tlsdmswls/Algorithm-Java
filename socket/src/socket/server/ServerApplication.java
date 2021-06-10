import java.io.*;
import java.net.*;
import java.util.*;

/* ������ Ŭ���̾�Ʈ���� Request �޽����� �����ϰ� �Ǹ� ACK �޽����� Ŭ���̾�Ʈ���� �۽��Ѵ�.
 * ACK �۽��� �Ϸ�Ǹ� Request ��û�� ���� ������ �����Ѵ�. */
public class ServerApplication {
	ServerSocket ss = null;
	ArrayList<Client> clients = new ArrayList<Client>();
	static Client c;

	public static void main(String[] args) {
		ServerApplication server = new ServerApplication();
		try {
			server.ss = new ServerSocket(55555);
			System.out.println("���� ������ �����Ǿ����ϴ�.");

			while (true) {
				Socket socket = server.ss.accept();
				c = new Client(socket, server);
				server.clients.add(c);
				c.sm = new ServerSimulator(c);
				c.start();
			}
		} catch (SocketException e) {
			System.out.println("���� ���� �߻�...");
		} catch (IOException e) {
			System.out.println("����� ���� �߻�...");
		}
	}
}

/* ������ �ս� �ùķ����Ϳ� ��ü */
class ServerSimulator {
	Random rd = new Random();
	Client c = null;

	ServerSimulator(Client _c) {
		c = _c;
	}

	/* Ŭ���̾�Ʈ�� Message�� �����ϴ� �޼ҵ� */
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