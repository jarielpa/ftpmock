package io.puhos.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FTPServer {

	@Value("${ftp.port:21}")
	int ftpPort;

	@Value("${sftp.port:22}")
	int sftpPort;

	@Value("${ftps.port:23}")
	int ftpsPort;

	@Value("${ftp.username:admin}")
	private String username;

	@Value("${ftp.password:admin}")
	private String password;

	@Bean
	public SshServer sftpServer() throws IOException {

		SshServer sftpSrver = null;

		TemporaryFolder tempFolder = new TemporaryFolder();
		tempFolder.create();

		File homeFolder = tempFolder.newFolder("sftphome");

		sftpSrver = SshServer.setUpDefaultServer();

		VirtualFileSystemFactory f = new VirtualFileSystemFactory(homeFolder.toPath());
		sftpSrver.setFileSystemFactory(f);

		sftpSrver.setPort(sftpPort);
		sftpSrver.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(new SftpSubsystemFactory()));
		sftpSrver.setCommandFactory(new ScpCommandFactory());

		sftpSrver.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(tempFolder.newFile("hostkey.ser")));

		sftpSrver.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session)
					throws PasswordChangeRequiredException {
				return username.equals(username) && password.equals(password);
			}
		});
		return sftpSrver;

	}

	@Bean
	public FtpServer ftpsServer() throws FtpException, IOException {

		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();
		// set the port of the listener
		factory.setPort(ftpsPort);
		// define SSL configuration
		SslConfigurationFactory ssl = new SslConfigurationFactory();
		ssl.setKeystoreFile(new File("src/main/resources/ftpkeystore.jks"));
		ssl.setKeystorePassword("pass0101");
		// set the SSL configuration for the listener
		factory.setSslConfiguration(ssl.createSslConfiguration());
		factory.setImplicitSsl(true);
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());

		UserManager um = createUserManager(username, password);
																
		serverFactory.setUserManager(um);

		// start the server
		FtpServer server = serverFactory.createServer();
		return server;

	}

	@Bean
	public FtpServer ftpServer() throws FtpException, IOException {

		FtpServerFactory serverFactory = new FtpServerFactory();
		ListenerFactory factory = new ListenerFactory();

		// set the port of the listener
		factory.setPort(ftpPort);
		factory.setIdleTimeout(3000);

		// replace the default listener
		serverFactory.addListener("default", factory.createListener());

		UserManager um = createUserManager(username, password);
		serverFactory.setUserManager(um);

		FtpServer server = serverFactory.createServer();

		return server;

	}

	private UserManager createUserManager(String username, String password) throws IOException {
		TemporaryFolder tempFolder = new TemporaryFolder();
		tempFolder.create();

		File homeFolder = tempFolder.newFolder("home");
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();

		userManagerFactory.setFile(tempFolder.newFile("users.properties"));

		userManagerFactory.setPasswordEncryptor(new PasswordEncryptor() {

			@Override
			public String encrypt(String password) {
				return password;
			}

			@Override
			public boolean matches(String passwordToCheck, String storedPassword) {
				return passwordToCheck.equals(storedPassword);
			}
		});

		BaseUser user1 = new BaseUser();
		user1.setName(username);
		user1.setPassword(password);

		user1.setHomeDirectory(homeFolder.getPath());
		List<Authority> authorities = new ArrayList<Authority>();
		authorities.add(new WritePermission());
		authorities.add(new ConcurrentLoginPermission(10, 10));
		user1.setAuthorities(authorities);
		UserManager um = userManagerFactory.createUserManager();
		try {
			um.save(user1);
		} catch (FtpException e1) {
			e1.printStackTrace();
		}

		return um;

	}

}
