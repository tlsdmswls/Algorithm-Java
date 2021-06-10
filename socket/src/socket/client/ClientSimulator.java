package socket.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class ClientSimulator {
	ClientApplication c;
	Socket socket;
	Random rd = new Random();
	MessageListener ml;
	
	ClientSimulator(Socket _s) {
		socket = _s;
	}
	
	/* 서버로 Request message를 전송하는 메소드 */
	public void sendMessage(String msg) {
		OutputStream out;
		try {
			out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			
			int if_write = rd.nextInt(10);
			if(if_write < 7) {
				dout.writeUTF(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}