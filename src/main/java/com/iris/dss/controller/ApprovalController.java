package com.iris.dss.controller;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iris.dss.model.AuditTrail;
import com.iris.dss.model.CertificateFile;
import com.iris.dss.model.Pager;
import com.iris.dss.model.UserApproval;
import com.iris.dss.model.UserApprovalForm;
import com.iris.dss.repo.AuditTrailRepository;
import com.iris.dss.repo.CertificateFileRepository;
import com.iris.dss.repo.PaginationUserApprovalRepository;
import com.iris.dss.repo.UserApprovalRepository;
import com.iris.dss.service.CertificateFileService;
import com.iris.dss.service.UserApprovalService;
import com.iris.dss.utils.AuditTrailConstant;
import com.iris.dss.validator.UserApprovalValidator;

@Controller
public class ApprovalController {

	@Value("${message.dateTo}")
	String error;
	
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

	private static final Logger log = LoggerFactory.getLogger(ApprovalController.class);

	@Autowired
	private UserApprovalService userPortalService;

	@Autowired
	private CertificateFileService certificateFileService;

	@Autowired
	private UserApprovalRepository userApprovalRepository;

	@Autowired
	private CertificateFileRepository certificateFileRepository;

	 @Autowired
	 private UserApprovalValidator userApprovalValidator;
	 
	 @Autowired
	 private AuditTrailRepository auditTrailRepository;
	 
	 @Autowired
	 private PaginationUserApprovalRepository paginationUserApprovalRepository;
	 
	 // Set a form validator
    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
       // Form target
       Object target = dataBinder.getTarget();
       if (target == null) {
          return;
       }
      
  
       if (target.getClass() == UserApprovalForm.class) {
          dataBinder.setValidator(userApprovalValidator);
       }
       // ...
    }
    
	@GetMapping("/createApproval")
	String createUser(Model model) {
		log.info("AdminPortalController {}", "createApproval");
		UserApprovalForm form = new UserApprovalForm();
		model.addAttribute("userApprovalForm", form);
		return "createApproval";
	}

	// @RequestMapping(method = RequestMethod.POST, value = "/addUser")
	// @ResponseBody
	@PostMapping("/addApproval")
	public String addUser(@RequestParam(value = "file", required = false) MultipartFile file,  
			@RequestParam String id, 
			@RequestParam (value = "userId", required = false) String userId,
			@RequestParam (value = "name", required = false)  String name, 
			@RequestParam (value = "email", required = false)  String email, 
			@RequestParam (value = "password", required = false) String password,
			@RequestParam (value = "jenisFail", required = false) String jenisFail, Model model,
			RedirectAttributes ra,  @ModelAttribute("userApprovalForm") @Validated UserApprovalForm userApprovalForm, BindingResult result,Principal principal) throws Exception {
	/*
	 * public String addUser(@RequestParam MultipartFile file, @RequestParam
	 * MultipartFile file2,
	 * 
	 * @RequestParam MultipartFile file3, @RequestParam String id, @RequestParam
	 * String userId,
	 * 
	 * @RequestParam String name, @RequestParam String email, @RequestParam String
	 * password, Model model, RedirectAttributes ra) throws Exception {
	 */
		//System.out.println("name------" + name);
		byte[] pathFile1 = null;
		String fileName1 = null;
		Map<String, String> validityDate = null;
		// if file not empty
		
		
		if (id.isEmpty()) {
			
			//checkId pelulus
			UserApproval dbUser = userApprovalRepository.findByUserIdAndActive(userId, 1);
			if(dbUser!=null) {
				
				if (result.hasErrors()) {
					
					return "createApproval";
				}
				
			}
			
			if (file!= null&&!file.isEmpty()) {
				//System.out.println("file tidak null ------");
				/*File convFile1 = new File(file1.getOriginalFilename());  
				FileOutputStream fos = new FileOutputStream(convFile1);
				fos.write(file1.getBytes());
				fos.close();*/

				pathFile1 = file.getBytes();
				fileName1 = file.getOriginalFilename();
				validityDate = certificateFileService.validity(password, pathFile1);
				
				if(validityDate!=null) {
					
					if (validityDate.containsKey("invalid")) {
						//System.out.println("invalid ------");
						if (result.hasErrors()) {
							return "createApproval";
						}
					}
					
					if (validityDate.containsKey("validTo")) {
						
						Date date = new SimpleDateFormat("yyyy/MM/dd").parse(validityDate.get("validTo"));
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						
						java.util.Date dateToday= new Date();
						Calendar cal2 = Calendar.getInstance();
						//cal2.setTime(dateToday);
						
						/*
						 * System.out.println("\nmasuk controller" +date);
						 * System.out.println("\nmasuk controller"+cal.get(Calendar.DAY_OF_MONTH));
						 * System.out.println("\nmasuk controller" +cal.get(Calendar.MONTH));
						 * System.out.println("\nmasuk controller" +cal.get(Calendar.YEAR));
						 * 
						 * System.out.println("\nmasuk controller" +dateToday);
						 * System.out.println("\nmasuk controller" +cal2.get(Calendar.DATE));
						 * System.out.println("\nmasuk controller" +cal2.get(Calendar.MONTH));
						 * System.out.println("\nmasuk controller " +cal2.get(Calendar.YEAR));
						 */
						Date dateValidTo = new Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
					    Date today = new Date(cal2.get(Calendar.YEAR),cal2.get(Calendar.MONTH),cal2.get(Calendar.DAY_OF_MONTH));
										
						boolean isExpiredDate = dateValidTo.before(today);
						//System.out.println("error" + isExpiredDate);
						if(isExpiredDate==true) {
							
							if (result.hasErrors()) {
								return "createApproval";
							}
							
						}
						
					}
					
				}

			}
			

			UserApproval user = new UserApproval();
			user.setUserId(userId);
			user.setName(name);
			user.setEmail(email);
			user.setActive(1);
	
			try {
				
				if (jenisFail.toLowerCase().equals("p12")) //file must be attach (required)
				{
					
					if (pathFile1!= null) 
					{						
						//log.info(">addUser>Input file size: " + pathFile1.length);
						if (validityDate!=null) {
							String pattern = "yyyy/MM/dd";
							SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
							if (validityDate.containsKey("validTo")) {								
								user.setValidTo(simpleDateFormat.parse(validityDate.get("validTo")));
							}
							if (validityDate.containsKey("validFrom")) {								
								user.setValidFrom(simpleDateFormat.parse(validityDate.get("validFrom")));
							}
						}
						
					}
					
					userApprovalRepository.save(user);

					if (user.getId() != 0) {
						id = String.valueOf(user.getId());
						ra.addFlashAttribute("message", "Data berjaya disimpan");

					}
					
					if (pathFile1!= null) 
					{						
						certificateFileService.storeFile(pathFile1, fileName1, user, password, principal.getName());
					}
				
				} 				
				else if(jenisFail.toLowerCase().equals("dongle"))
				{
					userApprovalRepository.save(user);

					if (user.getId() != 0) {
						id = String.valueOf(user.getId());
						ra.addFlashAttribute("message", "Data berjaya disimpan");

					}
					
				    CertificateFile dbFile = new CertificateFile();
				  	dbFile.setFileType(jenisFail.toLowerCase());
			        dbFile.setUserApproval(user);
			        dbFile.setActive(1);
			        certificateFileRepository.save(dbFile);
			        
			        saveRequestRecord(
			          principal.getName(), 
					  AuditTrailConstant.FN_CREATE_CERTIFICATE, 
					  AuditTrailConstant.FN_CREATE_CERTIFICATE+" "+AuditTrailConstant.MSG_SUCCESS, 
					  AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
				}
				
				 saveRequestRecord(
    					 principal.getName(), 
    					 AuditTrailConstant.FN_CREATE_APPROVAL, 
    					 AuditTrailConstant.FN_CREATE_APPROVAL+" "+AuditTrailConstant.MSG_SUCCESS, 
    					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

			} catch (Exception e) {

				System.out.println("error" + e);
				
				throw e;

			}

		}

		else {

			UserApproval userFromDb = userApprovalRepository.findByIdAndActive(Integer.parseInt(id), 1);
			
			if (userFromDb != null) {

				if (file!= null&&!file.isEmpty()) {

					/*
					 * File convFile1 = new File(TMP_DIR+"/"+file1.getOriginalFilename());
					 * FileOutputStream fos = new FileOutputStream(convFile1);
					 * fos.write(file1.getBytes()); fos.close();
					 * 
					 * pathFile1 = convFile1.getAbsolutePath(); fileName1 = convFile1.getName();
					 */
					
					pathFile1 = file.getBytes();
					fileName1 = file.getOriginalFilename();
					validityDate = certificateFileService.validity(password, pathFile1);
					
					if(validityDate!=null) {
						if (validityDate.containsKey("invalid")) {
							System.out.println("invalid");
							if (result.hasErrors()) {
								return "updateApproval";
							}
						}
						
						if (validityDate.containsKey("validTo")) {
							//System.out.println("validTo update  " + validityDate.get("validTo"));
							
							Date date = new SimpleDateFormat("yyyy/MM/dd").parse(validityDate.get("validTo"));
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							
							java.util.Date dateToday= new Date();
							Calendar cal2 = Calendar.getInstance();
							//cal2.setTime(dateToday);
							
							
							Date dateValidTo = new Date(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
						    Date today = new Date(cal2.get(Calendar.YEAR),cal2.get(Calendar.MONTH),cal2.get(Calendar.DAY_OF_MONTH));
						   							
							boolean isExpiredDate = dateValidTo.before(today);
							
							System.out.println("error" + isExpiredDate);
							if(isExpiredDate==true) {
								
								if (result.hasErrors()) {
									
									model.addAttribute("error", error);
									model.addAttribute("user", userFromDb);
									model.addAttribute("file", null);
									model.addAttribute("message", "");
									model.addAttribute("userApprovalForm", userApprovalForm);
									return "updateApproval";
								}
								
							}
							
						}
						
					}

				}

				// userFromDb.setUserId(userId);
				userFromDb.setName(name);
				userFromDb.setEmail(email);
				userFromDb.setActive(1);
				//if (!file.isEmpty()) { != null
				if (file!= null&&!file.isEmpty()) { 
					if (!validityDate.isEmpty()) {
						//SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	
						String pattern = "yyyy/MM/dd";
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
					
	
						if (validityDate.containsKey("validTo")) {
						
							userFromDb.setValidTo(simpleDateFormat.parse(validityDate.get("validTo")));
						}
						if (validityDate.containsKey("validFrom")) {
							
							userFromDb.setValidFrom(simpleDateFormat.parse(validityDate.get("validFrom")));
						}
					}
				}
				userApprovalRepository.save(userFromDb);
				if (userFromDb.getId() != 0) {
					// id = String.valueOf(userFromDb.getId());
					ra.addFlashAttribute("message", "Data berjaya dikemaskini");

				}

				/*
				 * if (file!= null&&!file.isEmpty()) {
				 * certificateFileService.storeFile(pathFile1, fileName1, userFromDb, password,
				 * principal.getName()); }
				 */
				
				
				if (jenisFail!= null&&!jenisFail.isEmpty()) {
					if (jenisFail.toLowerCase().equals("p12")) 
					{
						
						if (pathFile1!= null) 
						{						
							certificateFileService.storeFile(pathFile1, fileName1, userFromDb, password, principal.getName());
						}
					} 				
					else if(jenisFail.toLowerCase().equals("dongle"))
					{
						//userApprovalRepository.save(userFromDb);
	
					    CertificateFile dbFile = new CertificateFile();
					  	dbFile.setFileType(jenisFail.toLowerCase());
				        dbFile.setUserApproval(userFromDb);
				        dbFile.setActive(1);
				        certificateFileRepository.save(dbFile);
				        
				        saveRequestRecord(
				          principal.getName(), 
						  AuditTrailConstant.FN_CREATE_CERTIFICATE, 
						  AuditTrailConstant.FN_CREATE_CERTIFICATE+" "+AuditTrailConstant.MSG_SUCCESS, 
						  AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
					}
					
				}
				saveRequestRecord(
   					 principal.getName(), 
   					 AuditTrailConstant.FN_UPDATE_APPROVAL, 
   					 AuditTrailConstant.FN_UPDATE_APPROVAL+" "+AuditTrailConstant.MSG_SUCCESS, 
   					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());


			}

		}

		// if pasing by id
		// return "redirect:/admin/"+id;
		return "redirect:/approvalList";

	}
	// no attachment dah ada attachemt

	@PostMapping("/addApprovalNoAttachment")
	public String addUser(@RequestParam String id, @RequestParam String userId, @RequestParam String name,
			@RequestParam String email, @RequestParam String password, Model model, RedirectAttributes ra,Principal principal)
			throws Exception {

		UserApproval userFromDb = userApprovalRepository.findById(Integer.parseInt(id));

		if (userFromDb != null) {

			// userFromDb.setUserId(userId);
			userFromDb.setName(name);
			userFromDb.setEmail(email);
			userFromDb.setActive(1);
			userApprovalRepository.save(userFromDb);

			if (userFromDb.getId() != 0) {
				// id = String.valueOf(userFromDb.getId());
				ra.addFlashAttribute("message", "Data berjaya dikemaskini");

			}
			
			 saveRequestRecord(
					 principal.getName(), 
					 AuditTrailConstant.FN_UPDATE_APPROVAL, 
					 AuditTrailConstant.FN_UPDATE_APPROVAL+" "+AuditTrailConstant.MSG_SUCCESS, 
					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

		}

		// if pasing by id
		// return "redirect:/admin/"+id;
		return "redirect:/approvalList";

	}

	@GetMapping("/approvalList")
	String approvalList(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "nameSearch", required = false) String nameSearch,
			Model model) {
		
		Page<UserApproval> findAllUser = null;
		List<CertificateFile> findAllFiles = new ArrayList<CertificateFile>();
		
		//calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		//calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);
				
		try {
			
			findAllFiles = certificateFileRepository.findAllByActive(1); //1 active 0 inactive(deleted file)
			
			if(nameSearch==null || nameSearch.isEmpty() ) {
				findAllUser = paginationUserApprovalRepository.queryfindAllByActive(1,new PageRequest(currentPage, selectedPageSize));
			}
			else {
				findAllUser = paginationUserApprovalRepository.queryFindUserApprovalByName(nameSearch,new PageRequest(currentPage, selectedPageSize));
				
			}
			
			//initialize pagination helper class
			Pager pager = new Pager(findAllUser.getTotalPages(), findAllUser.getNumber(), Pager.getButtonRange(), selectedPageSize);
			
			model.addAttribute("list", findAllUser);
			model.addAttribute("files", findAllFiles);
			model.addAttribute("nameSearch",nameSearch);
			model.addAttribute("pager", pager);

		} catch (Exception e) { // TODO Auto-generated catch block
			e.printStackTrace();
			model.addAttribute("list", findAllUser);
			model.addAttribute("files", findAllFiles);
		}

		return "userApprovalList";
	}

	@GetMapping(value = "/getApproval/{id}")
	String getApprovalById(@PathVariable("id") String id, Model model) {
		UserApprovalForm form = new UserApprovalForm();
		UserApproval user = null;
		CertificateFile getFile = null;
		String jenisFail ="";
		if (id != null) {

			user = userPortalService.findUserApprovalById(Integer.parseInt(id));
			getFile = certificateFileRepository.findByUserApprovalAndActive(user,1);
			 //System.out.println("user==="+user);
			 //System.out.println("getFile==="+getFile);
			form.setId(user.getId());
			form.setEmail(user.getEmail());
			form.setName(user.getName());
			form.setUserId(user.getUserId());
			form.setFile(form.getFile());
			
			if(getFile!=null) {
				if(getFile.getFileType()!=null) {
					form.setJenisFail(getFile.getFileType());
					jenisFail = getFile.getFileType();
					
				}
				
				if(getFile.getPasswordP12()!=null) {
					//System.out.println("getFile.getPasswordP12()"+getFile.getPasswordP12());
				   form.setPassword(getFile.getPasswordP12());}
			}
			// log.info("AdminPortalController {}", "getAdminUser" + user);
			model.addAttribute("user", user);
			model.addAttribute("file", getFile);
			model.addAttribute("message", "");
			model.addAttribute("userApprovalForm", form);
			model.addAttribute("jenisFail", jenisFail);
			
		}

		return "updateApproval";
	}

	
	/* yg lama
	 * @GetMapping(value = "/deleteFile/{id}/{fileId}") public String
	 * deleteFile(@PathVariable("id") String id, @PathVariable("fileId") String
	 * fileId,
	 * 
	 * @ModelAttribute("userApprovalForm") @Validated UserApprovalForm
	 * userApprovalForm, BindingResult result, Model model,Principal principal) {
	 * log.info("AdminPortalController {}", "getFile" +
	 * userApprovalForm.getEmail()); log.info("AdminPortalController {}", "getFile"
	 * + userApprovalForm.getName());
	 * 
	 * UserApproval user = null; CertificateFile getFile = null; if (id != null &&
	 * fileId != null) {
	 * 
	 * user = userApprovalRepository.findById(Integer.parseInt(id)); getFile =
	 * certificateFileRepository.findById(Integer.parseInt(fileId)); if (getFile !=
	 * null) {
	 * 
	 * getFile.setActive(0); //deletefile certificateFileRepository.save(getFile);
	 * 
	 * user.setValidFrom(null); user.setValidTo(null);
	 * userApprovalRepository.save(user); model.addAttribute("message",
	 * "Data berjaya dihapuskan"); }
	 * 
	 * userApprovalForm.setId(user.getId());
	 * userApprovalForm.setEmail(user.getEmail());
	 * userApprovalForm.setName(user.getName());
	 * userApprovalForm.setUserId(user.getUserId());
	 * 
	 * 
	 * model.addAttribute("user", user); model.addAttribute("file", null);
	 * model.addAttribute("userApprovalForm", userApprovalForm);
	 * 
	 * saveRequestRecord( principal.getName(),
	 * AuditTrailConstant.FN_DELETE_CERTIFICATE,
	 * AuditTrailConstant.FN_DELETE_CERTIFICATE+" "+AuditTrailConstant.MSG_SUCCESS,
	 * AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
	 * 
	 * }
	 * 
	 * return "updateApproval"; }
	 */
	
	@GetMapping(value = "/deleteFile/{fileId}")
	public String deleteFile(@PathVariable("fileId") String fileId, 
			RedirectAttributes ra, Principal principal, Model model) {
		

		UserApproval user = null;
		CertificateFile getFile = null;
		if (fileId != null) {
			
			getFile = certificateFileRepository.findById(Integer.parseInt(fileId));
			if (getFile != null) {
				
				user = userApprovalRepository.findById(getFile.getUserApproval().getId());
				
				getFile.setActive(0); //deletefile
				certificateFileRepository.save(getFile);
				
				user.setValidFrom(null);
				user.setValidTo(null);
				userApprovalRepository.save(user);
				ra.addFlashAttribute("message", "Sijil berjaya dihapuskan");
			}
			
		
			 saveRequestRecord(
					 principal.getName(), 
					 AuditTrailConstant.FN_DELETE_CERTIFICATE, 
					 AuditTrailConstant.FN_DELETE_CERTIFICATE+" "+AuditTrailConstant.MSG_SUCCESS, 
					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

		}

		return "redirect:/approvalList";
	}
	
	@GetMapping(value = "/deleteApproval/{id}")
	public String deleteApproval(@PathVariable("id") String id, Model model,RedirectAttributes ra, Principal principal) {

		UserApproval user = null;
		CertificateFile getFile = null;
		if (id != null) {

			user = userApprovalRepository.findById(Integer.parseInt(id));
			
			getFile = certificateFileRepository.findByUserApprovalAndActive(user,1);
		
			if (getFile != null) {
				getFile.setActive(0);
				certificateFileRepository.save(getFile);
			}
			
			if (user != null) {

				user.setActive(0); //set to not active
				userApprovalRepository.save(user);
				//userApprovalRepository.delete(user);
				ra.addFlashAttribute("message", "Data berjaya dihapuskan");
			}
			 saveRequestRecord(
					 principal.getName(), 
					 AuditTrailConstant.FN_DELETE_APPROVAL, 
					 AuditTrailConstant.FN_DELETE_APPROVAL+" "+AuditTrailConstant.MSG_SUCCESS, 
					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			
			

		}
		
		
		return "redirect:/approvalList";
	}

	/*
	 * @RequestMapping(value = "/admin/getFile/{id}", method = RequestMethod.GET)
	 * public String getFile(@PathVariable("id") String id, Model model) {
	 * log.info("AdminPortalController {}", "getFile" + id);
	 * 
	 * UserPortal user = null; CertificateFile getFile = null; if (id != null) {
	 * 
	 * user = userPortalService.findUserPortalById(Integer.parseInt(id)); getFile =
	 * certificateFileService.getFile(user);
	 * 
	 * log.info("AdminPortalController {}", "getAdminUser" + user);
	 * model.addAttribute("user", user); model.addAttribute("file", getFile);
	 * 
	 * }
	 * 
	 * return "admin/updateUser"; }
	 */

	@GetMapping(value = "/download/{id}")
	public ResponseEntity<byte[]> getDownloadData(@PathVariable("id") String id,Principal principal) throws Exception {
		log.info("AdminPortalController {}", "getAdminUser" + id);
		CertificateFile file = certificateFileService.getFile(Integer.parseInt(id));
		log.info("AdminPortalController {}", "getAdminUser" + file);
		byte[] output = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		if (file != null) {

			output = Base64.decode(file.getFileData());
			//log.info("AdminPortalController {}", "getAdminUser" + output);
			responseHeaders.set("charset", "utf-8");
			responseHeaders.setContentType(MediaType.valueOf("text/plain"));
			responseHeaders.setContentLength(output.length);
			responseHeaders.set("Content-disposition", "attachment; filename=" + file.getFileName());
			
			 saveRequestRecord(
					 principal.getName(), 
					 AuditTrailConstant.FN_DOWNLOAD_APPROVAL, 
					 AuditTrailConstant.FN_DOWNLOAD_APPROVAL+" "+AuditTrailConstant.MSG_SUCCESS, 
					 AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
		}

		return new ResponseEntity<byte[]>(output, responseHeaders, HttpStatus.OK);

	}
	
	private void saveRequestRecord(
			String user, String module, String desc, int status, Date createAt, Date modifiedAt) {
		
		//log.info("AdminPortalController saveRequestRecord user:{}|module:{}|desc:{}|status:{}", user, module, desc, status);
		
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
