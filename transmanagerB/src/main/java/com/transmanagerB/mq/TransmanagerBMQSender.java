package com.transmanagerB.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.stereotype.Component;

/**
 * activeMQ 설치 
 * wget http://archive.apache.org/dist/activemq/apache-activemq/5.9.0/apache-activemq-5.9.0-bin.tar.gz
 * tar -xvzf apache-activemq-5.9.0-bin.tar.gz
 * 
 * activeMQ 설정 http://activemq.apache.org/how-can-i-support-priority-queues.html
 * vi activemq.xml
 * <policyEntry queue=">" prioritizedMessages="true" useCache="false" expireMessagesPeriod="0" queuePrefetch="1" />
 * 
 * activeMQ 구동
 * ./activemq start
 * ./activemq stop
 * 
 * 모니터링 페이지
 * http://119.149.188.226:8161/admin/
 * @author user
 *
 */
@Component
public class TransmanagerBMQSender {
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    public ActiveMQProperties activeMQProperties;	
	
	@Value("${activemq.name}")
	public String activemqName;
	
	@Value("${activemq.jmxserviceurl}")
	public String jmxServiceURL;
	
	@Value("${activemq.objectname}")
	public String objectName;
	
	long timeToLive = 0;
	
	public void sendMessageRedisCommand(String correlation_ID, String command, int priority) throws JMSException {
		
		ConnectionFactory factory = null;
		Connection connection = null;
		Session session = null;
		Destination destination = null;
		MessageProducer producer = null;
		
		try {
			factory = new ActiveMQConnectionFactory(activeMQProperties.getBrokerUrl());
			connection = factory.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(activemqName);
			
			TextMessage message = session.createTextMessage();
			message.setText(command);			
			message.setJMSType("FFMPEG");
			message.setJMSReplyTo(destination);
			
			//Correlation ID
			message.setJMSCorrelationID(correlation_ID);
			log.info("### {}", message.getText());
			
			producer = session.createProducer(destination);
			producer.send(destination, message, DeliveryMode.NON_PERSISTENT, priority, timeToLive);
			
		} catch (JMSException e) {
			e.printStackTrace();			
		} finally {
			producer.close();
			session.close();
			connection.close();
		}
	}
	
	public boolean removeMessageRedisCommand(String correlation_ID) throws Exception {
		
		JMXServiceURL url = new JMXServiceURL(jmxServiceURL);
		JMXConnector connector = JMXConnectorFactory.connect(url, null);
		connector.connect();
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		ObjectName name = new ObjectName(objectName);		
		QueueViewMBean queueMbean = (QueueViewMBean) MBeanServerInvocationHandler.newProxyInstance(connection, name, QueueViewMBean.class, true);
		
		//queueMbean.removeMatchingMessages(correlation_ID);
		return queueMbean.removeMessage(correlation_ID);
	}
}
