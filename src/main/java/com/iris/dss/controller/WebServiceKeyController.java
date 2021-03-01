package com.iris.dss.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iris.dss.model.AuditTrail;
import com.iris.dss.model.Pager;
import com.iris.dss.model.Token;
import com.iris.dss.repo.AuditTrailRepository;
import com.iris.dss.repo.PaginationTokenRepository;
import com.iris.dss.repo.TokenRepository;
import com.iris.dss.utils.AESKeyUtil;
import com.iris.dss.utils.AuditTrailConstant;
import com.iris.dss.utils.PdfUtil;

/**
 * This class consist of all the function related to REST API token.
 * 
 * 
 * @since 2019
 * @version 0.0.0.1
 * @author trlok
 *
 */
@Controller
public class WebServiceKeyController {

	private static final Logger log = LoggerFactory.getLogger(WebServiceKeyController.class);
	
	@Autowired
	private TokenRepository tokenRepository;
	
	@Autowired 
	private PaginationTokenRepository paginationTokenRepository;
	@Autowired
	private AuditTrailRepository auditTrailRepository;
	
	@PostMapping("/generateWSKey")
	String generateWSKey(@RequestParam String description, Authentication auth, Model model, Principal principal) throws Exception {
		log.info("WebServiceKeyController {}", "generateWSKey|"+ description);
		try {
			SecretKey sKey = AESKeyUtil.generateKey(256);
			String base64Key = Base64.toBase64String(sKey.getEncoded());
			log.info("WebServiceKeyController {}", "base64Key="+ base64Key);
			
			Token token = new Token();
			token.setToken(base64Key);
			token.setStatus(1);
			token.setDescription(description);
			token.setSha256Checksum(PdfUtil.getSHA256Checksum(base64Key.getBytes()));
			tokenRepository.save(token);
			
			 saveRequestRecord(
					 principal.getName(), 
					 AuditTrailConstant.FN_TOKEN, 
					 AuditTrailConstant.FN_TOKEN+" "+AuditTrailConstant.MSG_SUCCESS, 
					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			 
			List<Token> tokenActive = new ArrayList<Token>();
				
			tokenActive = tokenRepository.findAllByStatus(1);
			
			model.addAttribute("key", base64Key);
			model.addAttribute("desc", description);
			model.addAttribute("tokenActive", tokenActive);
			
		} catch (Exception e) {
			throw e;
		}
		return "genToken";
	}
		
	@GetMapping("/revokeToken")
	String tokenList(@RequestParam(value = "pageSize", required = false) Integer pageSize,
		@RequestParam(value = "page", required = false) Integer page,Model model) throws Exception {
		Page<Token> getAllToken = null;
		
		//calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		//calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);
				
		try {
			
			getAllToken = paginationTokenRepository.queryfindAllStatusExceptDeletedFile(new PageRequest(currentPage, selectedPageSize));
			Pager pager = new Pager(getAllToken.getTotalPages(), getAllToken.getNumber(), Pager.getButtonRange(), selectedPageSize);
			//check active token
			List<Token> tokenActive = new ArrayList<Token>();
			
			tokenActive = tokenRepository.findAllByStatus(1);
			
			model.addAttribute("list", getAllToken);
		    model.addAttribute("totalRecord", getAllToken.getTotalElements());
			model.addAttribute("pager", pager);
			model.addAttribute("tokenActive", tokenActive);
			
		}catch(Exception e) {
			
			model.addAttribute("tokens", getAllToken);
			
		}
			
		return "revokeToken";
	}
	
	//@RequestMapping(value = "/revoke/{id}", method = RequestMethod.GET)
	@GetMapping(value = "/revoke/{id}")
	String revoke(@PathVariable("id") String id, Model model,	RedirectAttributes ra,Principal principal) throws Exception {
		
		if(id!=null) {
			
			Token token = tokenRepository.findById(Integer.parseInt(id));
			System.out.println(token.getStatus());
			if(token!=null) {
				if(token.getStatus() == 1) {
					token.setStatus(0);
				} 
				
				tokenRepository.save(token);
				ra.addFlashAttribute("message", "Token berjaya dibatalkan");
				saveRequestRecord(
						 principal.getName(), 
						 AuditTrailConstant.FN_REVOKE, 
						 AuditTrailConstant.FN_REVOKE+" "+AuditTrailConstant.MSG_SUCCESS, 
						 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			}
			
			
		}

		return "redirect:/revokeToken";
	}
	
	@RequestMapping(value = "/activeBack/{id}", method = RequestMethod.GET)
	String activeBack(@PathVariable("id") String id, Model model,	RedirectAttributes ra, Principal principal) throws Exception {
		
		if(id!=null) {
			
			Token token = tokenRepository.findById(Integer.parseInt(id));
			System.out.println(token.getStatus());
			if(token!=null) {
				if(token.getStatus() == 0) {
					token.setStatus(1);
				} 
				
				tokenRepository.save(token);
				
				saveRequestRecord(
						 principal.getName(), 
						 AuditTrailConstant.FN_REACTIVATED, 
						 AuditTrailConstant.FN_REACTIVATED+" "+AuditTrailConstant.MSG_SUCCESS, 
						 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			}
			
			
		}

		return "redirect:/revokeToken";
	}
	
	//@RequestMapping(value = "/deleteToken/{id}", method = RequestMethod.GET)
	@GetMapping(value = "/deleteToken/{id}")
	String deleteToken(@PathVariable("id")  String id, Model model,	RedirectAttributes ra, Principal principal ) throws Exception {
		
		if(id!=null) {
			
			Token token = tokenRepository.findById(Integer.parseInt(id));
			if(token!=null) {
				token.setStatus(2);//delete token
				tokenRepository.save(token);
				ra.addFlashAttribute("message", "Token berjaya dihapuskan");
				saveRequestRecord(
						 principal.getName(), 
						 AuditTrailConstant.FN_DELETE, 
						 AuditTrailConstant.FN_DELETE+" "+AuditTrailConstant.MSG_SUCCESS, 
						 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			}
			
			
		}

		return "redirect:/revokeToken";
	}
	
	private void saveRequestRecord(
			String user, String module, String desc, int status, Date createAt, Date modifiedAt) {
		
		// Save request report to database
		AuditTrail auditTrail = new AuditTrail();
		auditTrail.setUser(user);
		auditTrail.setModule(module);
		auditTrail.setDescription(desc);
		auditTrail.setStatus(status);
		auditTrail.setCreatedAt(createAt);
		auditTrail.setModifiedAt(modifiedAt);
		auditTrailRepository.save(auditTrail);
	}
	
}
