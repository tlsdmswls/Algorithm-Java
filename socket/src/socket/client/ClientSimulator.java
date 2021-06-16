package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/* 데이터 손실 시뮬레이터용 객체 */
public class ClientSimulator {
	ClientApplication c;
	Socket socket;
	Random rd = new Random();
	int clientTimer = 1; // 타이머는 0.1초부터 시작
	boolean status = false; // 전송 성공하면 true
	static Timer timer = null;

	ClientSimulator(Socket _s) {
		socket = _s;
  }
  
	/* 서버로 Request 전송하는 메소드 */
	public boolean sendMessage(String msg) {

		OutputStream out;
		try {
			out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			int if_write = rd.nextInt(10);
			if (if_write < 7) {
				dout.writeUTF(msg);
				System.out.println("Request: " + msg);
				status = true;
			} else {
				System.out.println("Request: Loss");
				status = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}
  
	// /* 타이머 구동하는 메소드 */
	// public static void runTimer() {
	// stopTimer();
	// timer = new Timer();
	// timer.schedule(new Timeout(), 50);
	// }
	//
	// /* 타이머 중지하는 메소드*/
	// public static void stopTimer() {
	// if(timer != null) {
	// timer.cancel();
	// timer.purge();
	// timer = null;
	// }
	// }
	//
	// public static class Timeout extends TimerTask {
	// Thread timeout;
	//
	// public Timeout() {
	// this.timeout = new Thread(this);
	// }
	//
	// @Override
	// public void run() {
	// runTimer();
	// }
	// }

	/* 타이머를 구동(재시작)하는 메소드 */
	// public void startTimer() {
	// Timer timer = new Timer();
	// clientTimer = 1;
	// TimerTask task = new TimerTask(){
	// @Override
	// public void run() {
	// while(true) {
	// try {
	// clientTimer++; // 실행 횟수 증가
	// Thread.sleep(100); // 0.1초 단위
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }}}};
	// timer.schedule(task, 100, 100); // 0.1초 뒤 실행, 0.1초마다 반복
	// }
}