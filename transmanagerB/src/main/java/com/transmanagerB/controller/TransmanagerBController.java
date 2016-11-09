package com.transmanagerB.controller;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.transmanagerB.domain.TransmanagerBCommand;
import com.transmanagerB.service.TransmanagerBService;

/**
 * java -jar transcoder-0.0.1-SNAPSHOT.jar --server.port=8888 <==포트설정
 * java -jar transmanagerB-0.0.1-SNAPSHOT.war 
 * @RestController와 @Controller 차이
 * @RestController : 뷰가 필요없는 API만 지원하는 서비스에서 사용, @ResponseBody를 포함하고 있음
 * @Controller : API와 뷰를 동시에 사용, 대신 API 서비스는 @ResponseBody를 붙여줘야 함
 */
@Controller
@RequestMapping("transmanager")
public class TransmanagerBController extends WebMvcConfigurerAdapter {
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
    @Autowired
    TransmanagerBService transmanagerBService;

    @ModelAttribute
    TransmanagerBCommand setUpForm() {
        return new TransmanagerBCommand();
    }
    
    @RequestMapping(value="template", method = RequestMethod.GET)
	public String template(Model model) {
		return "transmanager/template";
	}
    
    /**
     * CP사로 부터 FTP로 제공된 영상파일리스트를 화면에 출력
     * redis 에 있는 작업리스트를 화면에 출력
     * http://stackoverflow.com/questions/30254977/how-to-refresh-table-contents-in-div-using-jquery-ajax
     * http://api.jquery.com/load/
     * @param model
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    String list(Model model) throws JsonParseException, JsonMappingException, IOException {
    	List<TransmanagerBCommand> transmanagerGCommand = transmanagerBService.redisList();
        model.addAttribute("command", transmanagerGCommand);
        List<String> fileList = transmanagerBService.subDirList();
        
//        for (String string : fileList) {
//			log.info("{}", string);
//		}
        
        model.addAttribute("fileList", fileList);
        return "transmanager/list";
    }
    
    /**
     * job등록 (redis + activeMQ)
     * @param form
     * @param result
     * @param model
     * @return
     * @throws JMSException
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    String create(@Validated TransmanagerBCommand form, BindingResult result, Model model) throws JMSException, JsonParseException, JsonMappingException, IOException {
        if (result.hasErrors()) {
            return list(model);
        }
        TransmanagerBCommand transmanagerGCommand = new TransmanagerBCommand();
        BeanUtils.copyProperties(form, transmanagerGCommand);
        transmanagerBService.create(transmanagerGCommand);
        return "redirect:/transmanager/list";
    }
    
    /**
     * 작업진행이 되지 않은 작업에 대한 삭제 처리
     * @param correlation_ID
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "delete/{correlation_ID}", method = RequestMethod.POST)
    String delete(@PathVariable("correlation_ID") String correlation_ID, Model model) throws Exception {
    	log.info("correlation_ID={}", correlation_ID);
        transmanagerBService.delete(correlation_ID);
        return "redirect:/transmanager/list";
    }
    
    /**
     * 작업완료된 미디어 파일에 대한 재생확인 및 검수
     * @param videoFile
     * @param model
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @RequestMapping(value = "play/{videoFile}", method = RequestMethod.GET)
    String play(@PathVariable("videoFile") String videoFile, Model model) throws JsonParseException, JsonMappingException, IOException {
//    	log.info("videoFile={}", videoFile);
    	model.addAttribute("videoFile", "/home/tvingadmin/ffmpeg/out/" + videoFile + ".mp4");
        return "transmanager/play";
    }
}

