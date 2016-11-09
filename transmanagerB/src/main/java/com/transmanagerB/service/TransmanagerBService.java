package com.transmanagerB.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.transmanagerB.domain.TransmanagerBCommand;
import com.transmanagerB.mq.TransmanagerBMQSender;
import com.transmanagerB.util.StringUtil;

@Service
public class TransmanagerBService {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${activemq.name}")
	public String activemqName;
	
	@Value("${ffmpeg.path}")
	public String ffmpegPath;
	
	@Value("${ffmpeg.option}")
	public String ffmpegOption;
	
	@Value("${ffmpeg.preset01}")
	public String preset01;
	
	@Value("${ffmpeg.preset02}")
	public String preset02;
	
	@Value("${ffmpeg.preset03}")
	public String preset03;
	
	@Value("${ffmpeg.preset04}")
	public String preset04;
	
	@Value("${ffmpeg.preset05}")
	public String preset05;
	
	@Value("${ffmpeg.preset06}")
	public String preset06;	
	
	@Value("${ffmpeg.srcpath}")
	public String srcPath;
	
	@Value("${ffmpeg.destpath}")
	public String destPath;
	
	@Autowired 
    TransmanagerBMQSender transmanagerGMQSender;
	
	@Resource(name = "redisTemplate")
	private ListOperations<String, String> listOps;
	
	@Resource(name = "redisTemplate")
	private SetOperations<String, String> setOps;
	
	@Resource(name = "redisTemplate")
	private ZSetOperations<String, String> opsForZSet;
	
	@Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;
	
	public List<TransmanagerBCommand> redisList() throws JsonParseException, JsonMappingException, IOException {

		//RedisOperations<String, String> redis = listOps.getOperations();      
		RedisOperations<String, String> redis = opsForZSet.getOperations();
        Set<String> setKeys = redis.keys(activemqName + "*");
		List<String> keyList = new ArrayList<String>(setKeys);
        Collections.sort(keyList, Collections.reverseOrder());
        
        List<String> valueList = valueOps.multiGet(keyList);        
        
        List<TransmanagerBCommand> transmanagerGCommandList = new ArrayList<TransmanagerBCommand>();
        ObjectMapper mapper = new ObjectMapper();
        for (String value : valueList) {
        	TransmanagerBCommand transmanagerGCommand = mapper.readValue(value, TransmanagerBCommand.class);
        	log.info("transmanagerGCommand={}", transmanagerGCommand.toString());
        	transmanagerGCommandList.add(transmanagerGCommand);
        }
		return transmanagerGCommandList;
	}
	
	public List<String> subDirList() {
		log.info("srcPath={}", srcPath);
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0) {
			srcPath = "C:\\Users\\user\\Pictures";
		}
		return StringUtil.subDirList(srcPath);
	}
	
	/**
	 * job 등록
	 * @param redisCommand
	 * @throws JMSException
	 */
	public void create(TransmanagerBCommand transmanagerGCommand) throws JMSException {
		
		long nanoTime = System.nanoTime();
		String correlation_ID = activemqName + "-" + nanoTime; // 535187520935910
		
		// redis 
		transmanagerGCommand.setCorrelation_ID(correlation_ID);
		transmanagerGCommand.setDestfile1(destPath + nanoTime + ".01.mp4");
		transmanagerGCommand.setDestfile2(destPath + nanoTime + ".02.mp4");
		transmanagerGCommand.setDestfile3(destPath + nanoTime + ".03.mp4");
		transmanagerGCommand.setDestfile4(destPath + nanoTime + ".04.mp4");
		transmanagerGCommand.setDestfile5(destPath + nanoTime + ".05.mp4");
		
		Gson gson = new Gson();
	    String jsonString = gson.toJson(transmanagerGCommand);
		valueOps.set(correlation_ID, jsonString);
		
		// activeMQ
		StringBuffer bufCmd = new StringBuffer();
		bufCmd.append(ffmpegPath);
		bufCmd.append(" " + ffmpegOption + "\"" + transmanagerGCommand.getSrcfile() + "\"");
		bufCmd.append(" " + preset01 + "\"" + transmanagerGCommand.getDestfile1() + "\"");
		bufCmd.append(" " + preset02 + "\"" + transmanagerGCommand.getDestfile2() + "\"");
		bufCmd.append(" " + preset03 + "\"" + transmanagerGCommand.getDestfile3() + "\"");
		bufCmd.append(" " + preset04 + "\"" + transmanagerGCommand.getDestfile4() + "\"");
		bufCmd.append(" " + preset05 + "\"" + transmanagerGCommand.getDestfile5() + "\"");
		
		transmanagerGMQSender.sendMessageRedisCommand(correlation_ID, bufCmd.toString(), transmanagerGCommand.getPriority());
	}
	
	/**
	 * job삭제/취소
	 * @param correlation_ID
	 * @throws Exception
	 */
	public void delete(String correlation_ID) throws Exception {		
		log.info("id={}", correlation_ID);
		setOps.getOperations().delete(correlation_ID); // redis
		transmanagerGMQSender.removeMessageRedisCommand(correlation_ID); // activeMQ
	}
}
