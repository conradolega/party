package src;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;


public class MyClient extends JFrame {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel chatWindow, onlineClients;
	JPanel leftPanel, rightPanel, msgPanel, chatPanel, onlinePanel;
	JTextArea leftText, rightText, msgField;
	JButton sendButton;
	JScrollPane msgPane;
	String msg;
	boolean quitted = false;
	
	public MyClient() {
		super("My Client");
		
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
		
		chatWindow = new JLabel("Chat Window");
		chatWindow.setAlignmentX(Component.LEFT_ALIGNMENT);
		chatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		chatPanel.setPreferredSize(new Dimension(400, 20));
		
		onlineClients = new JLabel("Online Clients");
		onlineClients.setAlignmentX(Component.CENTER_ALIGNMENT);
		onlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		onlinePanel.setPreferredSize(new Dimension(200, 20));
		
		leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(400, 420));
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		leftPanel.setMaximumSize(leftPanel.getPreferredSize());
		
		rightPanel = new JPanel();
		rightPanel.setPreferredSize(new Dimension(200, 400));
		rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		
		msgPanel = new JPanel();
		msgPanel.setPreferredSize(new Dimension(400, 100));
		msgPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		msgPanel.setMaximumSize(msgPanel.getPreferredSize());
		
		leftText = new JTextArea(20, 20);
		leftText.setLineWrap(true);
		leftText.setEditable(false);
		leftText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		leftText.setPreferredSize(new Dimension(390, 250));
		leftText.setMaximumSize(leftText.getPreferredSize());
		
		rightText = new JTextArea(20, 15);
		rightText.setEditable(false);
		rightText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		rightText.setPreferredSize(new Dimension(190, 250));
		rightText.setMaximumSize(rightText.getPreferredSize());
		
		msgField = new JTextArea(1, 20);
		msgField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		msgField.setPreferredSize(new Dimension(390, 50));
		msgField.setMaximumSize(msgField.getPreferredSize());
		
		msgPane = new JScrollPane(msgField);
		
		sendButton = new JButton("Send");
		sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		sendButton.setPreferredSize(new Dimension(190, 50));
		
		chatPanel.add(chatWindow);
		onlinePanel.add(onlineClients);
		
		leftPanel.add(chatPanel);
		leftPanel.add(leftText);
		leftPanel.add(msgField);

		rightPanel.add(onlinePanel);
		rightPanel.add(rightText);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		rightPanel.add(sendButton);
		rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		this.add(leftPanel);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		this.add(rightPanel);
		this.add(Box.createRigidArea(new Dimension(10, 0)));
		this.pack();
		this.setVisible(true);
		
		final JFrame window = this;
		
		try {
			leftText.setText(leftText.getText() + "Connecting to server...");
			Socket socket = new Socket("127.0.0.1", 8888);
			leftText.setText(leftText.getText() + "\nClient: Connected! \\^_^/");
			final MyConnection conn = new MyConnection(socket);
			sendButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					conn.sendMessage(msgField.getText().replaceAll("\n", "\0"));
					msgField.setText("");
				}
			});
			new Thread() {
			
				public void run() {
					while (!quitted) {
						msg = conn.getMessage();
						if (msg.substring(0, 4).equals("List")) {
							msg = msg.substring(7).replaceAll("\0", "\n");
							rightText.setText(msg);
						}
						else if (msg.equals("QUIT")) {
							quitted = true;
							window.dispose();
						}
						else leftText.setText(leftText.getText() + "\n" + msg.replaceAll("\0", "\n"));
					}
				}
			
			}.start();
		} catch (Exception e) {
		
		}
	}
	
	public static void main(String[] args) {
		new MyClient();
	}

}