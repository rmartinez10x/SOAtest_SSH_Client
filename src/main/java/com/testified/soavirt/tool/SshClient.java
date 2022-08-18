package com.testified.soavirt.tool;

import com.parasoft.api.tool.*;
import com.jcraft.jsch.*;
import java.io.InputStream;

public class SshClient implements ICustomTool {

	public boolean acceptsInput(IToolInput input, ICustomToolConfiguration settings) {
		return true;
	}

	public boolean execute(IToolInput input, IToolContext context) throws CustomToolException, InterruptedException {
		ICustomToolConfiguration config = context.getConfiguration();
		String host = config.getString("host");
		String portNum = config.getString("port");
		String user = config.getString("username");
		String password = config.getString("password");
		String command1 = config.getString("commandText");

		String response = "";
		
		//if(host.isEmpty() || user.isEmpty() || command1.isEmpty()) {}
		
		try {
			// Define default properties for java ssh connnection
			java.util.Properties sshConfig = new java.util.Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, Integer.parseInt(portNum));
			session.setPassword(password);
			session.setConfig(sshConfig);
			session.connect();
			com.parasoft.api.Application.showMessage("Connected");

			// Open channel and send command1
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command1);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			com.parasoft.api.Application.showMessage("Channel created");
			InputStream output = channel.getInputStream();
			//channel.setPty(true);
			channel.connect();
			com.parasoft.api.Application.showMessage("Connected channel");
			// Receive response from Server. Response received as bytes
			byte[] tmp = new byte[1024];
			while (true) {
				while (output.available() > 0) {
					int i = output.read(tmp, 0, 1024);
					if (i < 0)
						break;
					// Application.showMessage("New showMessage"+new String(tmp, 0, i));
					// This will loop depending on the size of the response. Use below line to
					// combine the response chunks
					response = response + new String(tmp, 0, i);
				}
				// Close channel once full response is received
				if (channel.isClosed()) {
					com.parasoft.api.Application.showMessage("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			// Close the channel/session/connection
			channel.disconnect();
			session.disconnect();
			com.parasoft.api.Application.showMessage("DONE");
		} catch (Exception e) {
			com.parasoft.api.Application.showMessage("Unable to connect to server. Check host, port, and authentication");
			e.printStackTrace();
			return false;
		}
		DefaultTextInput output = new DefaultTextInput(response, "UTF-8", "text/plain");
		return context.getOutputManager().runOutput("response", output, context);
	}

	public boolean isValidConfig(ICustomToolConfiguration settings) {
		//return !(settings.getString("host").isEmpty() || settings.getString("username").isEmpty() ||
		//			settings.getString("commandText").isEmpty());
		if(settings.getString("host").isEmpty() || settings.getString("username").isEmpty() ||
					settings.getString("commandText").isEmpty()) {
			com.parasoft.api.Application.showMessage("Host, username, and command cannot be empty");
			return false;
		} else return true;
	
	}

}
