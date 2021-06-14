package server;

import java.io.IOException;
import java.util.Random;

/* 데이터 손실 시뮬레이터용 객체 */
public class ServerSimulator {
	Random rd = new Random();
	Client c = null;
	boolean status;

	ServerSimulator(Client _c) {
		c = _c;
	}

	/* 메시지를 보내는 메소드 */
	public boolean sendMessage(String msg, String type) {
		int if_write = rd.nextInt(10);
		try {
			if (if_write < 7) {
				c.dout.writeUTF(msg);
				System.out.println(type + ": " + msg);
				status = true;
			} else {
				System.out.println(type + ": " + "Loss");
				status = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}
}