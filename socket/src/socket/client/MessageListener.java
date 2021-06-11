package socket.client;

import java.io.*;
import java.util.*;

/* �޽��� ������ ��ü */
public class MessageListener extends Thread {
	Socket socket;
	boolean quit = false;
	boolean check_ack = false;
	int num_ack = 1;
	int clientTimer = 1; // 1�̸� 0.1��
	ClientApplication c;
	boolean run = true;
	int res_resend = 0; // Response ������ ī��Ʈ
	int ack_resend = 0; // Ÿ�Ӿƿ��� ���� ������ ī��Ʈ

	MessageListener(Socket _s, ClientApplication _c) {
		this.socket = _s;
		this.c = _c;
	}

	public MessageListener(int clientTimer) {
		this.clientTimer = clientTimer;
	}

	public void run() {
		try {
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			OutputStream out = this.socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);

			String full_msg = null;
			String msg = null;
			String scode = null;
			StringTokenizer st = null;

			boolean ack_status = false;
			boolean res_status = false;

			while (true) {
				clientTimer = 1; // Ÿ�̸Ӹ� 0.1�ʷ� �ʱ�ȭ
				startTimer(); // Ÿ�̸� ����

				full_msg = din.readUTF();

				// Ÿ�̸� ���� �� ACK message�� �޾ƿ´�.
				while (clientTimer < 6) {
					msg = full_msg;
					scode = null;
					st = new StringTokenizer(msg, "///");
					msg = st.nextToken();

					// ACK message ���� ���
					if (msg.equals("ACK")) {
						System.out.println("[ACK] : " + full_msg);
						ack_status = true;
						break;
					}

					// Ÿ�̸� ����� �� Response message�� �޾ƿ´�.
					startTimer();
					full_msg = din.readUTF();
					while (clientTimer < 6) {
						full_msg = din.readUTF();
						msg = full_msg;
						st = new StringTokenizer(msg, "///");
						msg = st.nextToken();

						// Response message ���� ���
						if (msg.equals("Res")) {
							res_status = true;
							break;
						}
					}
				}

				// ACK message ���� ���� ���
				if (ack_status == false) {
					ack_resend++;
					System.out.println("Timeout�� ���� ACK ������: " + ack_resend);
				}

				// Response message ���� ���� ���
				if (res_status == false) {
					res_resend++;
					System.out.println("Timeout�� ���� Response ������: " + res_resend);
				}

				// Response message ���� ��� ��� ���
				if (res_status == true && res_status == true) {
					msg = st.nextToken();
					scode = msg;
					msg = st.nextToken();

					/* ������� ��û ��� ��� */
					if (scode.equals("200")) {
						clientList(msg);
					} else if (scode.equals("250")) {
						System.out.println(msg);
						quit = true;
						try {
							if (quit == true) {
								if (dout != null)
									dout.close();
								if (out != null)
									out.close();
								if (din != null)
									din.close();
								if (in != null)
									in.close();
								if (socket != null)
									socket.close();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					} else {
						System.out.println(msg);
					}
					// ����� ���������� �̷������ ������ +1
					c.num_req++;
					System.out.println(c.num_req);
				}
			}
		} catch (Exception e) {
			System.out.println("��� ��ü���� ���� �߻�...");
			e.printStackTrace();
		}
	}

	/* Ÿ�̸Ӹ� ����(�����)�ϴ� �޼ҵ� */
	public void startTimer() {
		clientTimer = 0;
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				while (clientTimer < 6) { // 0.5�ʵ��� ����
					clientTimer++; // ���� Ƚ�� ����
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		timer.schedule(task, 100, 100); // 0.1�� �� ����, 0.1�ʸ��� �ݺ�
		task.cancel();
		timer.cancel();
		timer.purge();
	}

	/* ������ ����� Ŭ���̾�Ʈ���� ����ϴ� �޼ҵ� */
	void clientList(String msg) {
		StringTokenizer st = new StringTokenizer(msg, "***");
		int count = st.countTokens();

		for (int i = 0; i < count - 1; i++) {
			msg = st.nextToken();
			System.out.println(msg);
		}
	}
}