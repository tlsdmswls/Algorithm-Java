package socket.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/* 데이터 손실 시뮬레이터용 객체 */
public class ClientSimulator {
	ClientApplication c;
	Socket socket;
	Random rd = new Random();
	MessageListener ml;
	String aString = null;
	ClientSimulator(Socket _s) {
		socket = _s;
	}
	
	/* 서버로 Request message를 전송하는 메소드 */
	public void sendMessage(String msg) {
		try {
			OutputStream out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			aString = "ASTRING";
			ml = new MessageListener(socket);
			
			int if_write = rd.nextInt(10);
			if(if_write < 7) {
				dout.writeUTF(msg);
				dout.flush();
				System.out.println(msg);
				
				if(ml.ack_status) {
					ml.ack_resend++;
					ml.ack_status = true;
				}
				else if(ml.res_status) {
					ml.res_resend++;
					ml.res_status = true;
				}
				ml.true_send++;
			}
			else {
				System.out.println("Request Message is Loss...");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}