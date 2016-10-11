package com.ota.websocket;

import java.io.*;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/log")
public class LogWebSocketHandle {
	
	private Process process;
	private InputStream inputStream;
	
	/**
	 * 新的WebSocket请求开启
	 */
	@OnOpen
	public void onOpen(Session session) {
		try {
			String path =  Thread.currentThread().getContextClassLoader().getResource("config.txt").getPath();
			System.out.println(path);
			InputStream is =new FileInputStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
			// 读取一行，存储于字符串列表中
			String logPath = reader.readLine();
			System.out.println(logPath);
			// 执行tail -f命令
			process = Runtime.getRuntime().exec("tail -f "+logPath);
			inputStream = process.getInputStream();
			
			// 一定要启动新的线程，防止InputStream阻塞处理WebSocket的线程
			TailLogThread thread = new TailLogThread(inputStream, session);
			thread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * WebSocket请求关闭
	 */
	@OnClose
	public void onClose() {
		try {
			if(inputStream != null)
				inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(process != null)
			process.destroy();
	}
	
	@OnError
	public void onError(Throwable thr) {
		thr.printStackTrace();
	}
}