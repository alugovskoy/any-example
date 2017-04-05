package org.genesys.filerepository.service.ftps;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Andrey Lugovskoy.
 */
public class SftpLoaderTest extends AbstractProtocolTest {

	private SshServer sshd;

	private int port;

	@SuppressWarnings("unchecked")
	@Before
	public void startSSHServer() {
		port = getAvailablePort();

		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
				"src/test/resources/hostkey.ser"));
		sshd.setSubsystemFactories(Arrays
				.<NamedFactory<Command>> asList(new SftpSubsystem.Factory()));
		sshd.setCommandFactory(new ScpCommandFactory());
		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

			@Override
			public boolean authenticate(String u, String p, ServerSession s) {
				return ("user".equals(u) && "user".equals(p));
			}
		});

		try {
			sshd.start();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@After
	public void deleteTempFile() {
		try {
			sshd.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

		boolean deleted = new File("src/test/resources/hostkey.ser").delete();
		assert deleted;
	}

	@Test
	public void connect() throws IOException, JSchException {
		JSch jSch = new JSch();
		Session session = jSch.getSession( "user", "localhost", port );
		session.setConfig( "PreferredAuthentications", "password" );
		session.setPassword( "user" );
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();
	}
}
