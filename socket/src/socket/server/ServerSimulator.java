package socket.server;

import java.io.IOException;
import java.util.Random;

/* 데이터 손실 시뮬레이터용 객체 */
public class ServerSimulator {
	Random rd = new Random();
	Client c = null;
		
	ServerSimulator(Client _c) {
		c = _c;
	}
	
	/* 클라이언트로 Message를 전송하는 메소드 */
	public void sendMessage(String msg) {
		int if_write = rd.nextInt(10);
		try {
			if(if_write < 7) {
				c.dout.writeUTF(msg);
			} else {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(msg);
	}
}