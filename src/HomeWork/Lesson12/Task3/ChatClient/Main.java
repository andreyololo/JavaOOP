package HomeWork.Lesson12.Task3.ChatClient;

import HomeWork.Lesson12.Task3.ChatServer.Message;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static Message createMessage(String s, String login) {
        int del = s.indexOf(':');
        String to = "";
        String text = s;

        if (del >= 0) {
            to = s.substring(0, del);
            text = s.substring(del + 1);
        }

        Message m = new Message();
        m.text = text;
        m.from = login;
        m.to = to;
        return m;
    }

    private static Message createFileMessage(String login, Scanner scanner) {
        System.out.print("Type in full file path: ");

        Message m = new Message();
        if (m.attachFile(scanner.nextLine())) {
            System.out.print("Type in to whom you want to send file: ");
            m.to = scanner.nextLine();
            m.from = login;
            m.isFile = true;
            return m;
        } else {
            System.out.println("No such file!");
            return null;
        }
    }

	public static void main(String[] args) {
		try {
			final Scanner scanner = new Scanner(System.in);
			final Socket socket = new Socket("127.0.0.1", 5000);
			final InputStream is = socket.getInputStream();
			final OutputStream os = socket.getOutputStream();

			System.out.println("Enter login: ");
			final String login = scanner.nextLine();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            objectOutputStream.writeUTF(login);
            objectOutputStream.flush();

            Thread th = new Thread() {
                @Override
                public void run() {
                    try {
                        while ( ! isInterrupted()) {
                            Message msg = Message.readFromStream(is);
                            if (msg == null)
                                Thread.yield();
                            else
                                System.out.println(msg.toString());

                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
            };
            th.setDaemon(true);
            th.start();
			
			try {
				while (true) {
					String s = scanner.nextLine();
					if (s.isEmpty())
						break;
                    Message m = null;
                    if (s.equalsIgnoreCase("!sendfile")) {
                        m = createFileMessage(login, scanner);
                    } else {
                        m = createMessage(s, login);
                    }
                    if (m != null) {
                        m.writeToStream(os);
                    }
				}
			} finally {
				th.interrupt();
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}