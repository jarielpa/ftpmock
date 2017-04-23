package io.puhos.util;

import java.io.IOException;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.sshd.server.SshServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class FtpmockApplication {

	public static void main(String[] args) throws IOException, FtpException {
		
		ConfigurableApplicationContext ctx = SpringApplication.run(FtpmockApplication.class, args);
		FtpServer ftpServer = (FtpServer) ctx.getBean("ftpServer");		
		FtpServer ftpsServer = (FtpServer) ctx.getBean("ftpsServer");
		SshServer sftpServer = (SshServer)ctx.getBean("sftpServer");	
		ftpServer.start();
		ftpsServer.start();
		sftpServer.start();
		


	}

}
