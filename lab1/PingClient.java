    // PingClient.java
    import java.io.*;
    import java.net.*;
    import java.util.*;

    /* 
     * Client to send ping requests over UDP.
     */

public class PingClient
{
		private int [] RTTs;
		private String serverName;
		private int serverPort;
		private int password;
		private Timer timer;
		private DatagramSocket clientSocket;
		private InetAddress serverIPAddress;

	public void schedule(){
			timer.scheduleAtFixedRate(new Task(), 1000, 1000);
	}

	public PingClient(String serverName, int serverPort, int password, InetAddress serverIPAddress)throws Exception{
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.password = password;
		this.timer = new Timer();
		this.clientSocket = new DatagramSocket();
		/** create socket. */
		this.serverIPAddress = serverIPAddress;
		this.clientSocket.setSoTimeout(1000);
		this.RTTs = new int[10];

	}

		class Task extends TimerTask {
			private int count = 0;
			@Override
		    public void run(){
		    	if(count==10){
					timer.cancel();
					/** close client socket. */
				 	clientSocket.close();
					int total_RTTs = 0;
					int min_RTTs = int.MAX_VALUE;
					int max_RTTs = 0;
					int valid_count = 0;

					for(int i = 0;i<10;i++){
						if(RTTs[i]>0){
								total_RTTs += RTTs[i];
								valid_count++;
								if(min_RTTs>RTTs[i])
									min_RTTs = RTTs[i];
								if(max_RTTs<RTTs[i])
									max_RTTs = RTTs[i];
						}
					}
					if(valid_count>0)
					System.out.printf("minimum RTTS %d ms maximum RTTs %d ms average RTTs %f ms \n", min_RTTs, max_RTTs, (double)total_RTTs/valid_count);
					System.out.printf("%d packets sent , %d echo-packets received\n", 10, valid_count);
					return;
				}

				//get timestamp
				long client_send_time = System.currentTimeMillis();
				String sentence = new String("PING " + String.format ("%02d", count) + " " + client_send_time + " " + String.format ("%05d", password)+ "\\r\\n");
				System.out.println(sentence);

				byte[] sendData = sentence.getBytes();
				System.out.println("sendData length = " + sendData.length);

				// construct and send datagram
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverPort);

				try{
					clientSocket.send(sendPacket);
				}

				catch(Exception e){
					System.out.println("Failed to send packet " + count);
				}
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try{
				// receive datagram
					clientSocket.receive(receivePacket);
					long client_receive_time = System.currentTimeMillis();
					RTTs[count] = (int)(client_receive_time - client_send_time);
					// print output
					String sentenceFromServer = receivePacket.getData();
					System.out.println("From Server: " + sentenceFromServer);
				}

				catch (Exception e) {//SocketTimeoutException e
            	// packet lost
					System.out.println("Failed to receive the %d-th ack-packet " + count);
					RTTs[count] = -1;
        		}
				count++;
		    }
		}

	public static void main(String args[])throws Exception{

		if(args.length!=3){
			System.out.println("Usage: java PingClient serverName serverPort password\n");
			return;
		}
		// get server address

		String serverName = args[0];
		InetAddress serverIPAddress = InetAddress.getByName(serverName);
		
		// get server port
		int serverPort = Integer.parseInt(args[1]);

		// get password
		int password = Integer.parseInt(args[2]);
		PingClient client = new PingClient(serverName, serverPort, password, serverIPAddress);
		client.schedule();
	}
}
