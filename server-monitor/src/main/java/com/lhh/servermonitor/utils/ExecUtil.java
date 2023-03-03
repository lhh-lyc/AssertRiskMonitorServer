package com.lhh.servermonitor.utils;

import com.lhh.serverbase.entity.SshResponse;
import lombok.NoArgsConstructor;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Component
public class ExecUtil {

    /**
     * 执行本机linux命令行，与ganymed-ssh2依赖无关，JDK自带功能
     */
    public static String exec(String command) {
        String result = "";
        try {
            String[] cmd = {"/bin/bash", "-c", command};
            Process ps = Runtime.getRuntime().exec(cmd);

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static SshResponse runCommand(String cmd)
            throws IOException {
        SshClient client = SshClient.setUpDefaultClient();

        try {
            // Open the client
            client.start();
            // Connect to the server
            ConnectFuture cf = client.connect(SshConnection.getUserName(), SshConnection.getHostName(), 22);
            ClientSession session = cf.verify().getSession();
            session.addPasswordIdentity(SshConnection.getPwd());
            session.auth().verify(TimeUnit.SECONDS.toMillis(SshConnection.getTimeout()));

            // Create the exec and channel its output/error streams
            ChannelExec ce = session.createExecChannel(cmd);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ce.setOut(out);
            ce.setErr(err);
            // Execute and wait
            ce.open();
            Set<ClientChannelEvent> events =
                    ce.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(SshConnection.getTimeout()));
            session.close(false);
            // Check if timed out
            if (events.contains(ClientChannelEvent.TIMEOUT)) {
                throw new RuntimeException(SshConnection.getHostName()+" 命令 "+cmd+ "执行超时 "+SshConnection.getTimeout());
            }
            return new SshResponse(out.toString(), err.toString(), ce.getExitStatus());
        } finally {
            client.stop();
        }
    }

}

