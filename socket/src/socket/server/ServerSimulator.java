package socket.server;

import java.io.IOException;
import java.util.Random;

/* ������ �ս� �ùķ����Ϳ� ��ü */
public class ServerSimulator {
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