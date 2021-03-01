package com.iris.dss.controller;

import java.io.File;
import java.io.OutputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.iris.dss.dto.AdminAuditReportDto;
import com.iris.dss.dto.DocReportDto;
import com.iris.dss.dto.PasswordDto;
import com.iris.dss.model.AuditTrail;
import com.iris.dss.model.Ipts;
import com.iris.dss.model.Modul;
import com.iris.dss.model.Pager;
import com.iris.dss.model.PasswordResetToken;
import com.iris.dss.model.PdfDoc;
import com.iris.dss.model.PreviewPdfSetting;
import com.iris.dss.model.RestApiRequestRecord;
import com.iris.dss.model.Role;
import com.iris.dss.model.SystemSetting;
import com.iris.dss.model.Token;
import com.iris.dss.model.User;
import com.iris.dss.model.UserApproval;
import com.iris.dss.model.UserForm;
import com.iris.dss.repo.AuditTrailRepository;
import com.iris.dss.repo.IptsRepository;
import com.iris.dss.repo.ModulRepository;
import com.iris.dss.repo.PaginationAuditTrailRepository;
import com.iris.dss.repo.PaginationPdfDocumentRepository;
import com.iris.dss.repo.PaginationRestApiTransactionRepository;
import com.iris.dss.repo.PaginationUserRepository;
import com.iris.dss.repo.PasswordResetTokenRepository;
import com.iris.dss.repo.PdfDocumentRepository;
import com.iris.dss.repo.PdfPreviewSettingRepository;
import com.iris.dss.repo.RestApiTransactionRepository;
import com.iris.dss.repo.RoleRepository;
import com.iris.dss.repo.SystemSettingRepository;
import com.iris.dss.repo.TokenRepository;
import com.iris.dss.repo.UserApprovalRepository;
import com.iris.dss.repo.UserRepository;
import com.iris.dss.security.ISecurityUserService;
import com.iris.dss.service.SecurityService;
import com.iris.dss.service.UserApprovalService;
import com.iris.dss.service.UserService;
import com.iris.dss.utils.AuditTrailConstant;
import com.iris.dss.utils.GenericResponse;
import com.iris.dss.utils.PdfUtil;
import com.iris.dss.utils.SendmailUtils;
import com.iris.dss.utils.WSError;
import com.iris.dss.validator.UserValidator;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Controller
public class AdminPortalController {

	private static final Logger log = LoggerFactory.getLogger(AdminPortalController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserApprovalRepository userApprovalRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private SystemSettingRepository systemSettingRepository;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private UserService userService;

	@Autowired
	private PdfDocumentRepository pdfDocRepo;

	@Autowired
	private AuditTrailRepository auditTrailRepository;

	@Autowired
	private ISecurityUserService securityUserService;

	@Autowired
	private MessageSource messages;

	// @Autowired
	// private JavaMailSender mailSender;

	@Autowired
	private Environment env;

	@Autowired
	private RestApiTransactionRepository restApiTransactionRepository;

	@Autowired
	private PdfPreviewSettingRepository pdfPreviewSettingRepo;

	@Autowired
	private UserApprovalService userApprovalService;

	@Autowired
	private PaginationPdfDocumentRepository paginationPdfDocumentRepository;

	@Autowired
	private PaginationAuditTrailRepository paginationAuditTrailRepo;

	@Autowired
	private PaginationRestApiTransactionRepository PaginationRestApiTransactionRepo;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private TokenRepository tokenRepo;

	@Autowired
	private IptsRepository iptsRepo;

	@Autowired
	private PaginationUserRepository paginationUserRepo;

	@Autowired
	private ModulRepository modulRepo;

	@Autowired
	private PasswordResetTokenRepository passwordTokenRepository;

	static final int PDF_PREVIEW_SETTING_DEFAULT_VALUE = -1;

	@Value("${app.jasperReport.storage.path}")
	String jasperReportPath;

	@Value("${notification.email.from}")
	String notificationEmailFromAddress;

	// Set a form validator
	@InitBinder
	protected void initBinder(WebDataBinder dataBinder) {
		// Form target
		Object target = dataBinder.getTarget();
		if (target == null) {
			return;
		}
		// System.out.println("Target=" + target);

		if (target.getClass() == UserForm.class) {
			dataBinder.setValidator(userValidator);
		}
		// ...
	}

	@GetMapping({ "/test-error" })
	String testError(Model model) {
		throw new RuntimeException("Testing on error handling.");
		// return "test";
	}

	@GetMapping({ "/", "/login" })
	String login(Model model) {
		return "login";
	}

	@GetMapping("/home")
	String adminHome(Model model) {

		int sumDocs = 0;
		int notPaid = 0;
		int totalRegDoc = 0;

		Long approvalNo = userApprovalRepository.countByActive(1);
		List<PdfDoc> allDoc = pdfDocRepo.findAll();
		List<PdfDoc> listNotPaid = pdfDocRepo.findAllByCheckPaymentAndPaid(1, 0);

		if (!allDoc.isEmpty()) {
			sumDocs = allDoc.size();
		}

		if (!listNotPaid.isEmpty()) {
			notPaid = listNotPaid.size();
		}

		totalRegDoc = sumDocs - notPaid;

		model.addAttribute("approvalNo", approvalNo);
		model.addAttribute("notPaid", notPaid);
		model.addAttribute("totalRegDoc", totalRegDoc);

		return "home";
	}

	// admin
	@GetMapping("/createAdmin")
	String createAdmin(Model model, Principal principal) {

		User usrlogin = null;
		List<User> getAllUser = new ArrayList<User>();
		if (principal.getName() != null) {

			usrlogin = userRepository.findByUserName(principal.getName());

		}

		getAllUser = userRepository.findAllByActive(1); // find all activw only

		// System.out.println("getAllUser"+getAllUser);
		UserForm form = new UserForm();
		List<Role> departments = new ArrayList<Role>();

		departments = roleRepository.findAll();

		model.addAttribute("allRoles", departments);
		model.addAttribute("user", null);
		model.addAttribute("userForm", form);
		model.addAttribute("usrlogin", usrlogin);
		model.addAttribute("getAllUser", getAllUser);

		return "createAdmin";
	}

	// @RequestMapping(value = "/getUserAdmin/{id}", method = RequestMethod.GET)
	@GetMapping(value = "/getUserAdmin/{id}")
	public String getApprovalById(@PathVariable("id") String id, Model model,
			@ModelAttribute("userForm") @Validated UserForm userForm, //
			BindingResult result, Principal principal) {
		UserForm form = new UserForm();
		User user = null;
		User usrlogin = null;
		List<User> getAllUser = new ArrayList<User>();
		if (id != null) {

			user = userRepository.findById(Integer.parseInt(id));
			// String hashedPassword = bCryptPasswordEncoder.encode(user.getPassword());
			form.setId(user.getId());
			form.setUsernameAdm(user.getUserName());
			form.setFirstName(user.getFirstName());
			form.setRoles(user.getRoles());
			form.setEmail(user.getEmail());

			if (principal.getName() != null) {

				usrlogin = userRepository.findByUserName(principal.getName());

			}

			getAllUser = userRepository.findAllByActive(1);

			model.addAttribute("getAllUser", getAllUser);
			model.addAttribute("usrlogin", usrlogin);
			model.addAttribute("userForm", form);
			model.addAttribute("user", user);
		}
		// List<Role> departments = new ArrayList<Role>();

		// departments = roleRepository.findAll();

		// model.addAttribute("allRoles", departments);

		return "createAdmin";
	}

	@GetMapping("/adminList")
	String adminList(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "nameSearch", required = false) String nameSearch, Model model, Principal principal) {
		log.info("AdminPortalController {}", "adminList");

		Page<User> users = null;
		User usrlogin = null;

		// calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		// calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);
		Role role = roleRepository.findByRole("PENTADBIR");
		try {

			if (nameSearch == null || nameSearch.isEmpty()) {
				System.out.println("siniaku masuk dl");
				users = paginationUserRepo.findAllByActiveAndRolesOrderByIdDesc(1, role,
						new PageRequest(currentPage, selectedPageSize));
			} else {

				System.out.println("name search masuk dl");
				users = paginationUserRepo.findAllByActiveAndRolesAndFirstNameContaining(1, role, nameSearch,
						new PageRequest(currentPage, selectedPageSize));

			}

			// System.out.println("value v"+users.getSize());
			// initialize pagination helper class
			Pager pager = new Pager(users.getTotalPages(), users.getNumber(), Pager.getButtonRange(), selectedPageSize);

			if (principal.getName() != null) {
				usrlogin = userRepository.findByUserName(principal.getName());
			}

			/*
			 * int rows = users.getNumberOfElements();
			 * 
			 * int nOfPages = rows / pageSize;
			 * 
			 * if (nOfPages % pageSize > 0) { nOfPages++; }
			 * 
			 * request.setAttribute("noOfPages", nOfPages);
			 */

			model.addAttribute("list", users);
			model.addAttribute("pager", pager);
			model.addAttribute("usrlogin", usrlogin);
			model.addAttribute("nameSearch", nameSearch);

		} catch (Exception e) {
			System.out.println(e);
			model.addAttribute("users", users);
		}

		return "adminList";
	}

	@PreAuthorize("@securityService.isLoggedInUser(#id, authentication.principal)")
	@PostMapping("/addAdmin")
	public String addAdmin(@RequestParam String id, @RequestParam String usernameAdm, @RequestParam String firstName,
			@RequestParam String passwordAdm, @RequestParam String email, @RequestParam List<String> roleId,
			Model model, @ModelAttribute("userForm") @Validated UserForm userForm, BindingResult result,
			RedirectAttributes ra, Principal principal) throws Exception {
		List<User> getAllUser = new ArrayList<User>();
		getAllUser = userRepository.findAllByActive(1);
		User usrlogin = null;
		if (id.isEmpty()) {

			User dbUser = userRepository.findByUserName(usernameAdm);
			if (dbUser != null) {

				if (result.hasErrors()) {

					List<Role> departments = new ArrayList<Role>();

					departments = roleRepository.findAll();

					if (principal.getName() != null) {
						usrlogin = userRepository.findByUserName(principal.getName());

					}
					model.addAttribute("allRoles", departments);
					model.addAttribute("user", null);
					model.addAttribute("usrlogin", usrlogin);
					model.addAttribute("getAllUser", getAllUser);
					ra.addFlashAttribute("duplicate",
							"Nama Pengguna telah digunakan.Sila masukkan Nama Pengguna yang lain");
					return "createAdmin";
				}

			} else {

				User user = new User();
				user.setFirstName(firstName); // user.setLastName(lastName);
				user.setUserName(usernameAdm);
				user.setActive(1);
				user.setPassword(bCryptPasswordEncoder.encode(passwordAdm));
				user.setEmail(email);
				Set<Role> roles = new HashSet<Role>();
				for (String a : roleId) {

					Role getRole = roleRepository.findByRole(a);

					if (getRole != null) {
						roles.add(getRole);

					}

				}
				user.setRoles(roles);
				userRepository.save(user);

				ra.addFlashAttribute("message", "Data berjaya disimpan");

				// Save record
				// if (roleUsrLogin=="PENTADBIR") {
				saveRequestRecord(principal.getName(), AuditTrailConstant.FN_CREATE_ADMIN,
						AuditTrailConstant.FN_CREATE_ADMIN + ' ' + AuditTrailConstant.MSG_SUCCESS,
						AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date()); // }

			}
		} else {

			// update data user
			User user = userRepository.findById(Integer.parseInt(id));
			if (user != null) {

				user.setFirstName(firstName); // user.setLastName(lastName);
				// user.setUserName(username);
				user.setEmail(email);
				user.setActive(1);
				/*
				 * if(passwordAdm!=null) {
				 * user.setPassword(bCryptPasswordEncoder.encode(passwordAdm)); }
				 */

				Set<Role> roles = new HashSet<Role>();
				for (String a : roleId) {

					Role getRole = roleRepository.findByRole(a);

					if (getRole != null) {
						roles.add(getRole);

					}

				}
				user.setRoles(roles);

				userRepository.save(user);
				ra.addFlashAttribute("message", "Data berjaya dikemaskini");

				// Save record
				// if (roleUsrLogin=="PENTADBIR") {
				saveRequestRecord(principal.getName(), AuditTrailConstant.FN_UPDATE_ADMIN,
						AuditTrailConstant.FN_UPDATE_ADMIN + ' ' + AuditTrailConstant.MSG_SUCCESS,
						AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date()); // }

			}

		}

		return "redirect:/adminList";

	}

	@GetMapping(value = "/deleteUser/{id}")
	public String deleteUser(Principal principal, @PathVariable("id") String id, Model model, RedirectAttributes ra) {
		log.info("AdminPortalController {}", "deleteUser" + id);
		User user = null;

		if (id != null) {

			user = userRepository.findById(Integer.parseInt(id));
			if (user != null) {
				/*
				 * if(user.getRoles()!=null) { user.getRoles().removeAll(user.getRoles());}
				 * userRepository.delete(user);
				 */

				user.setActive(0);
				userRepository.save(user); // new requirement - audit trail super admin can show all

				ra.addFlashAttribute("message", "Data berjaya dihapuskan");

				// Save record
				saveRequestRecord(principal.getName(), AuditTrailConstant.FN_DELETE_USER,
						AuditTrailConstant.FN_DELETE_USER + ' ' + AuditTrailConstant.MSG_SUCCESS,
						AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
			}

		}

		return "redirect:/adminList";
	}

	@GetMapping("/fileList")
	String fileList(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "searchItem", required = false) String searchItem,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "modulCode", required = false) String modulCode,
			@RequestParam(value = "jenisIpts", required = false) String jenisIpts,
			@RequestParam(value = "iptsName", required = false) String iptsName, Model model) {

		List<String> getUserApproval = new ArrayList<String>();
		List<UserApproval> allUser = userApprovalService.findAllUserApprovalActive(1);
		List<Ipts> iptsList = new ArrayList<Ipts>();
		List<Modul> modulList = new ArrayList<Modul>();

		List<PreviewPdfSetting> setting = new ArrayList<PreviewPdfSetting>();
		// calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		// calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);

		if (!allUser.isEmpty())
			for (UserApproval user : allUser) {
				getUserApproval.add(user.getUserId());
			}

		Collections.sort(getUserApproval);

		getUserApproval.sort(String.CASE_INSENSITIVE_ORDER);

		// for map --- getUserApproval.forEach((k,v) -> System.out.println(k + " - " +
		// v));

		Page<PdfDoc> files = null;
		try {

			if (searchItem == null || searchItem.equals("0")) {

				files = paginationPdfDocumentRepository.findAllByStatusNot(0,
						new PageRequest(currentPage, selectedPageSize));

			} else if (searchItem.equals("a")) {

				String pattern = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String startDate1 = startDate + " 00:00:00";
				String endDate1 = endDate + " 23:59:59";
				files = paginationPdfDocumentRepository.searchfilesByCreatedDate(simpleDateFormat.parse(startDate1),
						simpleDateFormat.parse(endDate1), new PageRequest(currentPage, selectedPageSize));
				System.out.println("startDate == startDate" + files.getTotalElements());
				/*
				 * }else { String pattern = "yyyy-MM-dd"; SimpleDateFormat simpleDateFormat =
				 * new SimpleDateFormat(pattern);
				 * //System.out.println("startDate != startDate"); files =
				 * paginationPdfDocumentRepository.searchfilesByCreatedAt(simpleDateFormat.parse
				 * (startDate), simpleDateFormat.parse(endDate),new PageRequest(currentPage,
				 * selectedPageSize)); }
				 */

			} else if (searchItem.equals("b")) {
				files = paginationPdfDocumentRepository.findAllByApprovalUserIdAndStatusNot(userId, 0,
						new PageRequest(currentPage, selectedPageSize));

			}
			/*
			 * else if(searchItem.equals("c")) { files =
			 * paginationPdfDocumentRepository.findAllByStatus(Integer.parseInt(status),new
			 * PageRequest(currentPage, selectedPageSize));
			 * 
			 * }
			 */
			else if (searchItem.equals("d")) {
				files = paginationPdfDocumentRepository.findAllByIptsNameAndStatusNot(iptsName, 0,
						new PageRequest(currentPage, selectedPageSize));
			} else if (searchItem.equals("e")) {
				files = paginationPdfDocumentRepository.findAllByModuleNameAndStatusNot(modulCode, 0,
						new PageRequest(currentPage, selectedPageSize));
			}

			setting = pdfPreviewSettingRepo.findAll();

			// initialize pagination helper class
			Pager pager = new Pager(files.getTotalPages(), files.getNumber(), Pager.getButtonRange(), selectedPageSize);
			iptsList = iptsRepo.findByStatus(1);
			modulList = modulRepo.findByStatus(1);
			// System.out.println("modulList="+modulList.size());
			model.addAttribute("getUserApproval", getUserApproval);
			model.addAttribute("list", files);
			model.addAttribute("setting", setting);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("userId", userId);
			model.addAttribute("modulList", modulList);
			model.addAttribute("jenisIpts", jenisIpts);
			
			if (searchItem == null) {
				model.addAttribute("searchItem", 0);
			} else {
				model.addAttribute("searchItem", searchItem);
			}
			model.addAttribute("iptsName", iptsName);
			model.addAttribute("modulCode", modulCode);
			model.addAttribute("pager", pager);
			model.addAttribute("iptsList", iptsList);

		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("files", files);
			model.addAttribute("setting", setting);
			model.addAttribute("getUserApproval", getUserApproval);
			model.addAttribute("searchItem", "0");
			model.addAttribute("iptsName", "0");
		}

		return "fileList";
	}

	@PostMapping("/setPage")
	public String setPage(@RequestParam String fileId, @RequestParam List<String> pageSettingPreview, Model model,
			RedirectAttributes ra, Principal principal) throws Exception {
		// log.info("AdminPortalController {}", "setPage");
		// System.out.println(pageSettingPreview);

		String page = "";
		for (String s : pageSettingPreview) {

			page += s + ",";

		}

		// System.out.println("page" +page);

		PdfDoc pdfDoc = null;
		if (fileId != null) {
			pdfDoc = pdfDocRepo.findById(Integer.parseInt(fileId));

			if (pdfDoc != null) {

				if (pdfDoc.getPreviewSettingId() == PDF_PREVIEW_SETTING_DEFAULT_VALUE) {

					PreviewPdfSetting ps = new PreviewPdfSetting();
					ps.setPageNo(page);
					ps.setActive("1");
					ps.setCreatedAt(new Date());
					pdfPreviewSettingRepo.save(ps);

					pdfDoc.setPreviewSettingId(ps.getId());
					pdfDocRepo.save(pdfDoc);

					saveRequestRecord(principal.getName(), AuditTrailConstant.FN_SET_PREVIEW,
							AuditTrailConstant.FN_SET_PREVIEW + ' ' + AuditTrailConstant.MSG_SUCCESS,
							AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

				} else {

					PreviewPdfSetting ps = pdfPreviewSettingRepo.findById(pdfDoc.getPreviewSettingId());
					if (ps != null) {
						ps.setPageNo(page);
						pdfPreviewSettingRepo.save(ps);

						saveRequestRecord(principal.getName(), AuditTrailConstant.FN_UPDATE_PREVIEW,
								AuditTrailConstant.FN_UPDATE_PREVIEW + ' ' + AuditTrailConstant.MSG_SUCCESS,
								AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
					}

				}
			}
		}

		return "redirect:/fileList";
	}

	@GetMapping("/generateToken")
	String generateToken(Model model) {
		List<Token> tokenActive = new ArrayList<Token>();
		log.info("AdminPortalController {}", "generateToken");
		tokenActive = tokenRepo.findAllByStatus(1);

		model.addAttribute("tokenActive", tokenActive);
		return "genToken";
	}

		@RequestMapping(value = "/getIptsByJenis", method = RequestMethod.GET,produces="application/json")
		@ResponseBody List<Ipts> getIptsByJenis(final HttpServletRequest request,	@RequestParam("jenisIpts") final String jenisIpts) {
			System.out.println(">jenisIpts>username:" + jenisIpts);
			 List<Ipts> iptsList = new ArrayList<Ipts>(); 
			 iptsList = iptsRepo.findByJenisIpts(jenisIpts);
			 return iptsList;
		

		}

	  
	 

	/*
	 * @ModelAttribute("allRoles") public List<Role> populateRoles() {
	 * 
	 * log.info("AdminPortalController {}", "allRoles");
	 * 
	 * List<Role> departments = new ArrayList<Role>();
	 * 
	 * departments = roleRepository.findAll(); System.out.println("size roles :" +
	 * departments.size()); return departments; }
	 */

	// tetapan sistem

	@GetMapping("/adminSetting")
	String adminSetting(Model model, Principal principal) {

		User usrlogin = null;
		if (principal.getName() != null) {

			usrlogin = userRepository.findByUserName(principal.getName());

		}

		List<SystemSetting> getData = systemSettingRepository.findAll();

		SystemSetting sys = null;

		if (getData != null) {

			for (SystemSetting system : getData) {

				sys = systemSettingRepository.findById(system.getId());
				// System.out.println("setting "+sys);

			}

		}

		model.addAttribute("sys", sys);

		Role getRole = roleRepository.findByRole("PENTADBIR");
		if (usrlogin.getRoles().contains(getRole)) {

			return "systemSetting2";
		} else {
			return "systemSetting";
		}

	}

	@PostMapping("/systemAdmin") // if tick ssl mandatory to keyin emailId, username and password. if not disable
									// emailId, username and password.
	public String systemAdmin(Principal principal, @RequestParam String id, @RequestParam String emailSMTP,
			@RequestParam String emailPort, @RequestParam String emailId, @RequestParam String emailAdmin,
			@RequestParam(value = "emailAdmin2", required = false) String emailAdmin2,
			@RequestParam(value = "emailPassword", required = false) String emailPassword,
			@RequestParam(value = "username", required = false) String username,
			@RequestParam(value = "ssl", required = false) String ssl, @RequestParam int notification, Model model,
			RedirectAttributes ra) throws Exception {

		if (id.isEmpty()) {

			SystemSetting sys = new SystemSetting();
			sys.setEmailSMTP(emailSMTP);
			sys.setEmailPort(emailPort);
			sys.setEmailId(emailId);
			sys.setEmailAdmin(emailAdmin);
			if (emailAdmin2 != null) {
				sys.setEmailAdmin2(emailAdmin2);
			} else {
				sys.setEmailAdmin2(null);
			}

			sys.setNotification(notification);

			if (ssl != null) {
				sys.setSslCert(Integer.parseInt(ssl));

				sys.setUserName(username);
				sys.setEmailPassword(emailPassword);
			} else {
				sys.setSslCert(0);

				sys.setUserName(null);
				sys.setEmailPassword(null);
			}
			systemSettingRepository.save(sys);
			ra.addFlashAttribute("message", "Data berjaya disimpan");

			// Save record
			saveRequestRecord(principal.getName(), AuditTrailConstant.FN_SAVE_SYSTEM_SETTING,
					AuditTrailConstant.MSG_SUCCESS_SAVE_SYSTEM_SETTING + " " + AuditTrailConstant.MSG_SUCCESS,
					AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

		} else {

			// update data user
			SystemSetting sys = systemSettingRepository.findById(Integer.parseInt(id));
			if (sys != null) {

				sys.setEmailSMTP(emailSMTP);
				sys.setEmailPort(emailPort);
				sys.setEmailId(emailId);
				sys.setEmailAdmin(emailAdmin);
				sys.setNotification(notification);
				if (emailAdmin2 != null) {
					sys.setEmailAdmin2(emailAdmin2);
				} else {
					sys.setEmailAdmin2(null);
				}

				if (ssl != null) {
					sys.setSslCert(Integer.parseInt(ssl));

					sys.setUserName(username);
					sys.setEmailPassword(emailPassword);
				} else {
					sys.setSslCert(0);

					sys.setUserName(null);
					sys.setEmailPassword(null);
				}

				systemSettingRepository.save(sys);
				ra.addFlashAttribute("message", "Data berjaya dikemaskini");

				// Save record
				saveRequestRecord(principal.getName(), AuditTrailConstant.FN_UPDATE_SYSTEM_SETTING,
						AuditTrailConstant.FN_UPDATE_SYSTEM_SETTING + " " + AuditTrailConstant.MSG_SUCCESS,
						AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

			}

		}

		return "redirect:/adminSetting";

	}

	// user profile
	@PreAuthorize("@securityService.isLoggedInUser(#id, authentication.principal)")
	@GetMapping(value = "/profile")
	public String userProfile(Principal principal, Model model, RedirectAttributes ra) {

		User user = null;

		if (principal.getName() != null) {

			user = userRepository.findByUserName(principal.getName());

		}
		model.addAttribute("userid", principal.getName());
		model.addAttribute("user", user);

		return "profile";
	}

	// change password
	@PreAuthorize("@securityService.isLoggedInUser(#id, authentication.principal)")
	@PostMapping(value = "/changePasswordProfile")
	public String savePassword(Principal principal, @RequestParam String passwordAdm, RedirectAttributes ra) {
		User user = null;
		if (principal.getName() != null) {
			user = userRepository.findByUserName(principal.getName());
		}

		if (user != null) {
			userService.changeUserPassword(user, passwordAdm);
			ra.addFlashAttribute("message", "Data berjaya dikemaskini");
			// Save record
			saveRequestRecord(principal.getName(), AuditTrailConstant.FN_CHANGE_PASSWORD,
					AuditTrailConstant.FN_CHANGE_PASSWORD + " " + AuditTrailConstant.MSG_SUCCESS,
					AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());
		}

		return "redirect:/profile";

	}

	// for pdf preview
	@GetMapping(value = "/pdf/{docSerialNumber}", produces = MediaType.APPLICATION_PDF_VALUE)
	public @ResponseBody byte[] getPdfFile(Principal principal, @PathVariable String docSerialNumber) throws Exception {
		log.info("RestApiController, /pdf/{docSerialNumber}, docSerialNumber: {}", docSerialNumber);
		try {

			// Load pdf from DB by QRCode Serial Number
			PdfDoc pdfDoc = pdfDocRepo.findByQrcodeSerialNum(docSerialNumber);
			if (null == pdfDoc) {
				throw new Exception("Document not found!");
			}

			String pdfFile = pdfDoc.getSignedFilePath(); // .getFilePath();

			// Save record
			saveRequestRecord(principal.getName(), AuditTrailConstant.FN_DOWNLOAD_PDF,
					AuditTrailConstant.MSG_SUCCESS_DOWNLOAD_PDF + " " + AuditTrailConstant.MSG_SUCCESS,
					AuditTrailConstant.STATUS_SUCCESS, new Date(), new Date());

			return PdfUtil.fileToByteArray(new File(pdfFile));

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new Exception("Fail to retrieve document. " + e.getMessage());
		}
	}

	// for link laporan
	@GetMapping("/documentReport")
	String documentReport(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "module", required = false) String module,
			@RequestParam(value = "iptsName", required = false) String iptsName,
			@RequestParam(value = "modulCode", required = false) String modulCode,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "jenisIpts", required = false) String jenisIpts,
			@RequestParam(value = "status", required = false) String status, Model model, Principal principal,
			Authentication authentication) {

		// System.out.println("sini masuk");
		Page<RestApiRequestRecord> transactionAPI = null;
		List<String> getUserApproval = new ArrayList<String>();
		List<PdfDoc> pdfFiles = new ArrayList<PdfDoc>();
		List<UserApproval> allUser = userApprovalService.findAllUserApprovalActive(1);
		List<UserApproval> allUsers = userApprovalRepository.findAll();
		User userlogin = null;

		// calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		// calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);
		List<Ipts> iptsList = new ArrayList<Ipts>();
		List<Modul> modulList = new ArrayList<Modul>();
		try {

			if (principal.getName() != null) {
				userlogin = userRepository.findByUserName(principal.getName());
			}

			if (!allUser.isEmpty())
				for (UserApproval user : allUser) {
					getUserApproval.add(user.getName() + " [" + user.getUserId() + "]");

				}
			Collections.sort(getUserApproval);
			getUserApproval.sort(String.CASE_INSENSITIVE_ORDER);

			if (module != null && iptsName != null && startDate != null && endDate != null && modulCode != null) {

				String pattern = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String startDate1 = startDate + " 00:00:00";
				String endDate1 = endDate + " 23:59:59";

				// System.out.println("dlm star not null");
				if (userId != null && !userId.isEmpty() && status.isEmpty()) {

					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");
					// System.out.println(userName);

					// System.out.println("dlm else userId!=null && status.isEmpty()");
					transactionAPI = PaginationRestApiTransactionRepo.searchByApprovalUserId(userName, module, iptsName,
							modulCode, simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1),
							new PageRequest(currentPage, selectedPageSize));

				} else if (userId.isEmpty() && status != null && !status.isEmpty()) {

					// System.out.println("dlm else userId.isEmpty() && status!=null");
					transactionAPI = PaginationRestApiTransactionRepo.searchByStatus(status, module, iptsName,
							modulCode, simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1),
							new PageRequest(currentPage, selectedPageSize));

				} else if (userId != null && !userId.isEmpty() && status != null && !status.isEmpty()) {
					// System.out.println("dlm status!=null");
					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");

					transactionAPI = PaginationRestApiTransactionRepo.searchAll(userName, status, module, iptsName,
							modulCode, simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1),
							new PageRequest(currentPage, selectedPageSize));

				} else {

					// System.out.println("dlm startdate not null");
					transactionAPI = PaginationRestApiTransactionRepo.searchByCreatedAtBetween(
							simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1), module, iptsName,
							modulCode, new PageRequest(currentPage, selectedPageSize));
				}

			} else {
				// System.out.println("dlm else luar ");
				transactionAPI = PaginationRestApiTransactionRepo.searchByModuleAndIptsName(null, null, null,
						new PageRequest(currentPage, selectedPageSize));
			}

			pdfFiles = pdfDocRepo.findAll();
			Pager pager = new Pager(transactionAPI.getTotalPages(), transactionAPI.getNumber(), Pager.getButtonRange(),
					selectedPageSize);

			iptsList = iptsRepo.findByStatus(1);
			modulList = modulRepo.findByStatus(1);

			model.addAttribute("pager", pager);
			model.addAttribute("list", transactionAPI);
			model.addAttribute("userlogin", authentication.getDetails());
			model.addAttribute("pdfFiles", pdfFiles);
			model.addAttribute("getUserApproval", getUserApproval);
			model.addAttribute("module", module);
			model.addAttribute("iptsName", iptsName);
			model.addAttribute("modulCode", modulCode);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("userId", userId);
			model.addAttribute("status", status);
			model.addAttribute("iptsList", iptsList);
			model.addAttribute("modulList", modulList);
			model.addAttribute("allUser", allUser);
			model.addAttribute("allUsers", allUsers);
			model.addAttribute("jenisIpts", jenisIpts);

		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("list", transactionAPI);
			model.addAttribute("userlogin", authentication.getDetails());
			model.addAttribute("pdfFiles", pdfFiles);
			model.addAttribute("getUserApproval", getUserApproval);
			model.addAttribute("pager", null);
			System.out.println(e);
		}

		return "documentReport";
	}

	@GetMapping("/generateDocReport")
	public @ResponseBody void generatePdfReport(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "module", required = false) String module,
			@RequestParam(value = "iptsName", required = false) String iptsName,
			@RequestParam(value = "modulCode", required = false) String modulCode,
			@RequestParam(value = "searchItem", required = false) String searchItem,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "status", required = false) String status, Model model, HttpServletResponse response,
			Principal principal, Authentication authentication) throws Exception {

		List<RestApiRequestRecord> transactionAPI = null;
		List<PdfDoc> pdfFiles = new ArrayList<PdfDoc>();

		try {

			if (module != null && iptsName != null && startDate != null && endDate != null && modulCode != null) {

				String pattern = "yyyy-MM-dd HH:mm:ss";
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
				String startDate1 = startDate + " 00:00:00";
				String endDate1 = endDate + " 23:59:59";

				// System.out.println("dlm star not null");
				if (userId != null && !userId.isEmpty() && status.isEmpty()) {

					// System.out.println("dlm else userId!=null && status.isEmpty()");
					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");
					transactionAPI = restApiTransactionRepository.searchByApprovalUserId(userName, module, iptsName,
							modulCode, simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1));

				} else if (userId.isEmpty() && status != null && !status.isEmpty()) {

					// System.out.println("dlm else userId.isEmpty() && status!=null");
					transactionAPI = restApiTransactionRepository.searchByStatus(status, module, iptsName, modulCode,
							simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1));

				} else if (userId != null && !userId.isEmpty() && status != null && !status.isEmpty()) {
					// System.out.println("dlm status!=null");
					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");
					transactionAPI = restApiTransactionRepository.searchAll(userName, status, module, iptsName,
							modulCode, simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1));

				} else {

					// System.out.println("dlm startdate not null");
					transactionAPI = restApiTransactionRepository.searchByCreatedAtBetween(
							simpleDateFormat.parse(startDate1), simpleDateFormat.parse(endDate1), module, iptsName,
							modulCode);
				}

			} else {
				// System.out.println("dlm else luar ");
				transactionAPI = restApiTransactionRepository.searchByModuleAndIptsName(null, null, null);
			}
			// log.info("--------------dalam ipts nama----------"+transactionAPI.size());

			pdfFiles = pdfDocRepo.findAll();
			List<DocReportDto> docReportDto = new ArrayList<DocReportDto>();
			if (!transactionAPI.isEmpty()) {
				for (RestApiRequestRecord record : transactionAPI) {

					DocReportDto dto = new DocReportDto();

					for (PdfDoc pdf : pdfFiles) {
						Ipts iptsDesc = new Ipts();
						Modul modulV = new Modul();
						UserApproval usrApprV = new UserApproval();

						if (record.getPdfDocId() == pdf.getId()) {

							if (pdf.getIptsName() != null) {
								// log.info("--------------dalam ipts nama----------"+pdf.getIptsName());
								iptsDesc = iptsRepo.findByIptsCode(pdf.getIptsName());
								if (iptsDesc != null) {
									// log.info("--------------dalam ipts nama----------"+iptsDesc.getIptsName());
									dto.setIptsName(iptsDesc.getIptsName());
								}
							}

							if (pdf.getModuleName() != null) {
								// log.info("--------------dalam modul nama----------"+pdf.getModuleName());
								modulV = modulRepo.findByModulCode(pdf.getModuleName());
								if (iptsDesc != null) {
									// log.info("--------------dalam ipts nama----------"+iptsDesc.getIptsName());
									dto.setModuleName(modulV.getModulDesc());
								}
							}

							if (pdf.getQrcodeSerialNum() != null) {
								dto.setQrcodeSerialNum(pdf.getQrcodeSerialNum());
							}

							dto.setFileName(pdf.getFileName());
							dto.setApprovalUserId(pdf.getApprovalUserId());
							if (pdf.getApprovalUserId() != null) {
								// log.info("--------------dalam modul nama----------"+pdf.getModuleName());
								usrApprV = userApprovalRepository.findByUserId(pdf.getApprovalUserId());
								if (usrApprV != null) {
									// log.info("--------------dalam ipts nama----------"+iptsDesc.getIptsName());
									dto.setApprovalName(usrApprV.getName());
								}
							}

							dto.setPdfCreatedAt(pdf.getCreatedAt());
						}
					}
					dto.setRequestId(record.getRequestId());
					dto.setCreatedAt(record.getCreatedAt());
					dto.setModule(record.getModule());
					dto.setStatus(record.getStatus());

					docReportDto.add(dto);
				}
			}

			log.info("--------------generate PDF report----------" + docReportDto.size());

			if (!docReportDto.isEmpty()) {

				// Compile the Jasper report from .jrxml to .japser
				// log.info("--------------generate PDF report----------"+jasperReportPath);

				// JasperReport jasperReport =
				// JasperCompileManager.compileReport(jasperReportPath +
				// "\\laporanPermohonanDokumen.jrxml");
				JasperReport jasperReport = null;
				if (module.equals("SIGN_PDF")) {
					jasperReport = JasperCompileManager
							.compileReport(jasperReportPath + "laporanPermohonanDokumen.jrxml");
				} else {
					jasperReport = JasperCompileManager
							.compileReport(jasperReportPath + "laporanPermohonanDokumenTandatanganLengkap.jrxml");
				}

				// Get your data source
				JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(docReportDto,
						false);

				Ipts ipts = iptsRepo.findByIptsCode(iptsName);
				Modul modul = modulRepo.findByModulCode(modulCode);

				// Add parameters
				Map<String, Object> parameters = new HashMap<>();

				parameters.put("module", module);

				// utk modul pengajian
				if (modul != null) {
					parameters.put("modul", modul.getModulDesc());
				}
				if (ipts != null) {
					parameters.put("iptsName", ipts.getIptsName());
				}

				parameters.put("dijanaOleh", authentication.getDetails());

				DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
				fromFormat.setLenient(false);
				DateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
				toFormat.setLenient(false);
				Date dateStart = fromFormat.parse(startDate);
				Date dateEnd = fromFormat.parse(endDate);
				DateFormat toFormat2 = new SimpleDateFormat("dd/MM/yyyy");
				parameters.put("startDate", toFormat2.format(dateStart));
				parameters.put("endDate", toFormat2.format(dateEnd));

				if (userId != null && status.isEmpty()) {
					parameters.put("userId", userId);
				}
				if (userId.isEmpty() && status != null) {

					parameters.put("status", status);
				}
				if (userId != null && status != null) {
					parameters.put("userId", userId);
					parameters.put("status", status);
				}

				// Fill the report
				response.setContentType("application/x-download");
				response.setHeader("Content-Disposition",
						String.format("attachment; filename=\"laporanPermohonanDokumen.pdf\""));

				OutputStream out = response.getOutputStream();
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
						jrBeanCollectionDataSource);
				JasperExportManager.exportReportToPdfStream(jasperPrint, out);

			}

		} catch (Exception e) {
			throw e;

		}
	}

	@GetMapping("/adminActivityReport")
	String adminActivity(@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "status", required = false) String status, Model model, Principal principal) {

		// calculate current page size
		int selectedPageSize = Pager.calculateSelectedPageSize(pageSize);
		// calculate current page number
		int currentPage = Pager.calculateCurrentPage(page);
		System.out.println("userId not null" + userId);

		// System.out.println("status not null"+status);
		Page<AuditTrail> audit = null;
		// Role role = roleRepository.findByRole("PENTADBIR"); //2 for tadbir only
		List<User> allUser = userService.findAllByActive(1);
		User userlogin = null;
		List<String> getUsers = new ArrayList<String>();
		List<User> getAllUser = new ArrayList<User>();
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String startDate1 = startDate + " 00:00:00";
		String endDate1 = endDate + " 23:59:59";

		try {

			if (startDate != null && endDate != null) {

				if (userId != null && !userId.isEmpty() && status.isEmpty()) {

					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");

					System.out.println("userId not null");
					audit = paginationAuditTrailRepo.searchByDateAndUser(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), userName, new PageRequest(currentPage, selectedPageSize));

				} else if (status != null && !status.isEmpty() && userId.isEmpty()) {

					System.out.println("status not null");
					audit = paginationAuditTrailRepo.searchByDateAndStatus(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), Integer.parseInt(status),
							new PageRequest(currentPage, selectedPageSize));

				} else if (startDate != null && endDate != null && userId != null && !status.isEmpty()) {
					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");

					System.out.println("semua tk null");
					audit = paginationAuditTrailRepo.searchAll(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), userName, Integer.parseInt(status),
							new PageRequest(currentPage, selectedPageSize));

				}

				else {

					System.out.println("date not null" + startDate);
					audit = paginationAuditTrailRepo.searchByCreatedAtBetween(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), new PageRequest(currentPage, selectedPageSize));

				}

			} /*
				 * else if(startDate!= null && endDate!= null && userId!= null && status!= null)
				 * { String[] userIdSplit = userId.split("[ ]"); String userName =
				 * userIdSplit[userIdSplit.length-1]; // 425 userName =
				 * userName.replaceAll("(\\[)(.*.)(\\])", "$2");
				 * 
				 * System.out.println("semua tk null"); audit =
				 * paginationAuditTrailRepo.searchAll(simpleDateFormat.parse(startDate1),
				 * simpleDateFormat.parse(endDate1),userName,Integer.parseInt(status),new
				 * PageRequest(currentPage, selectedPageSize));
				 * 
				 * }
				 */

			else {
				// System.out.println("====================null=====================");
				audit = paginationAuditTrailRepo.searchByCreatedAtBetween(null, null,
						new PageRequest(currentPage, selectedPageSize));
			}

			// initialize pagination helper class
			Pager pager = new Pager(audit.getTotalPages(), audit.getNumber(), Pager.getButtonRange(), selectedPageSize);

			getAllUser = userRepository.findAll();
			if (principal.getName() != null) {
				userlogin = userRepository.findByUserName(principal.getName());
			}

			if (!allUser.isEmpty())
				for (User user : allUser) {
					getUsers.add(user.getFirstName() + " [" + user.getUserName() + "]");
				}

			getUsers.sort(String.CASE_INSENSITIVE_ORDER);

			/*
			 * getUsers.sort(new Comparator<User>() {
			 * 
			 * @Override public int compare(User o1, User o2) { //Return 0 if o1 and o2 are
			 * equal //Return +1 if o1 comes after o2 //Return -1 if o1 comes before o2 if
			 * (o1 == o2) { return 0; } if (o1 != null) { return (o2 != null) ?
			 * o1.getFirstName().compareToIgnoreCase(o2.getFirstName()) : 1; } return -1; }
			 * }); Collections.sort(getUsers, Comparator.comparing(User::getFirstName));
			 */
			System.out.println("userId----" + userId);
			// System.out.println("====================getAllUser====================="+getAllUser);
			model.addAttribute("audit", audit);
			model.addAttribute("getUsers", getUsers);
			model.addAttribute("getAllUser", getAllUser);
			model.addAttribute("pager", pager);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("userId", userId);
			model.addAttribute("status", status);

		} catch (Exception e) {
			System.out.println("error" + e);
			// TODO: handle exception
			model.addAttribute("audit", audit);
			model.addAttribute("userlogin", "");
			model.addAttribute("getUsers", getUsers);
			model.addAttribute("getAllUser", getAllUser);
			model.addAttribute("searchItem", "0");
			model.addAttribute("pager", "");
		}
		return "adminActivityReport";
	}

	@GetMapping("/generateAdminActivitReport")
	public @ResponseBody void generateAdminActivitReport(
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			@RequestParam(value = "userId", required = false) String userId,
			@RequestParam(value = "status", required = false) String status, Model model, HttpServletResponse response,
			Principal principal) throws Exception {

		List<AuditTrail> audit = null;
		// Role role = roleRepository.findByRole("PENTADBIR"); //2 for tadbir only
		List<User> allActiveUser = userService.findAllByActive(1);
		List<User> getUsers = new ArrayList<User>();
		List<User> getAllUser = new ArrayList<User>();
		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String startDate1 = startDate + " 00:00:00";
		String endDate1 = endDate + " 23:59:59";

		try {

			if (startDate != null && endDate != null) {

				if (userId != null && !userId.isEmpty()) {

					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");
					// System.out.println("userId not null");
					audit = auditTrailRepository.searchByDateAndUser(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), userName);

				} else if (status != null && !status.isEmpty()) {

					// System.out.println("status not null");
					audit = auditTrailRepository.searchByDateAndStatus(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1), Integer.parseInt(status));

				} else {

					// System.out.println("date not null"+startDate);
					audit = auditTrailRepository.searchByCreatedAtBetween(simpleDateFormat.parse(startDate1),
							simpleDateFormat.parse(endDate1));

				}

			} else if (startDate != null && endDate != null && userId != null && status != null) {
				// System.out.println("semua tk null");
				String[] userIdSplit = userId.split("[ ]");
				String userName = userIdSplit[userIdSplit.length - 1]; // 425
				userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");
				audit = auditTrailRepository.searchAll(simpleDateFormat.parse(startDate1),
						simpleDateFormat.parse(endDate1), userName, Integer.parseInt(status));

			} else {
				// System.out.println("====================null=====================");
				audit = auditTrailRepository.searchByCreatedAtBetween(null, null);
			}

			getAllUser = userRepository.findAll();

			if (!allActiveUser.isEmpty())
				for (User user : allActiveUser) {
					getUsers.add(user);
				}

			List<AdminAuditReportDto> dtos = new ArrayList<AdminAuditReportDto>();

			if (!audit.isEmpty()) {
				for (AuditTrail oldRecord : audit) {
					AdminAuditReportDto dto = new AdminAuditReportDto();

					for (User usr : getAllUser) {

						if (oldRecord.getUser().equals(usr.getUserName())) {

							dto.setFirstName(usr.getFirstName() + " [" + usr.getUserName() + "]");

						}
					}
					// dto.setUserName(oldRecord.getUser());
					dto.setUser(oldRecord.getUser());
					dto.setId(oldRecord.getId());
					dto.setCreatedAt(oldRecord.getCreatedAt());
					dto.setModule(oldRecord.getModule());
					dto.setStatus(oldRecord.getStatus());
					dtos.add(dto);

				}
			}

			if (!dtos.isEmpty()) {

				// Compile the Jasper report from .jrxml to .japser
				log.info("--------------generate PDF report----------" + jasperReportPath);
				JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportPath + "laporanAudit.jrxml");

				// Get your data source
				JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(dtos, false);

				// Add parameters
				Map<String, Object> parameters = new HashMap<>();
				parameters.put("dijanaOleh", principal.getName());
				DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd");
				fromFormat.setLenient(false);
				DateFormat toFormat = new SimpleDateFormat("dd-MM-yyyy");
				toFormat.setLenient(false);
				Date dateStart = fromFormat.parse(startDate);
				Date dateEnd = fromFormat.parse(endDate);
				DateFormat toFormat2 = new SimpleDateFormat("dd/MM/yyyy");
				parameters.put("startDate", toFormat2.format(dateStart));
				parameters.put("endDate", toFormat2.format(dateEnd));

				if (!userId.isEmpty() && status.isEmpty()) {

					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");

					parameters.put("userId", userName);
					User usrSearch = userRepository.findByUserName(userName);
					if (usrSearch != null)
						parameters.put("userIdFullName", usrSearch.getFirstName() + " [" + userName + "]");

				}
				if (userId.isEmpty() && !status.isEmpty()) {

					parameters.put("status", status);
				}

				if (!userId.isEmpty() && !status.isEmpty()) {

					String[] userIdSplit = userId.split("[ ]");
					String userName = userIdSplit[userIdSplit.length - 1]; // 425
					userName = userName.replaceAll("(\\[)(.*.)(\\])", "$2");

					parameters.put("userId", userName);
					User usrSearch = userRepository.findByUserName(userName);
					if (usrSearch != null)
						parameters.put("userIdFullName", usrSearch.getFirstName() + " [" + userName + "]");
					parameters.put("status", status);
				}

				// Fill the report
				response.setContentType("application/x-download");
				response.setHeader("Content-Disposition",
						String.format("attachment; filename=\"laporanAktivitiPentadbir.pdf\""));

				OutputStream out = response.getOutputStream();
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
						jrBeanCollectionDataSource);
				JasperExportManager.exportReportToPdfStream(jasperPrint, out);

			}

		} catch (Exception e) {
			System.out.println("e--------------------" + e);
			throw e;

		}
	}

	private void saveRequestRecord(String user, String module, String desc, int status, Date createAt,
			Date modifiedAt) {

		log.info("AdminPortalController saveRequestRecord user:{}|module:{}|desc:{}|status:{}", user, module, desc,
				status);

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

	// reset password

	// Reset password
	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse resetPassword(final HttpServletRequest request,
			@RequestParam("username") final String username) {
		System.out.println(">resetPassword>username:" + username);
		
		log.info("--------------resetPassword----------" + username);
		final User user = userService.findUserByUserName(username); // .findUserByEmail(userEmail);
		System.out.println(">resetPassword>user:" + user);
		
		log.info("--------------resetPassword----------" + user);
		final User user2 = userService.findUserByUserNameAndActive(username, 1);
		if (user == null) {
			log.info(">sini>user:" + user);
			return new GenericResponse("null", "error.userNotFound");
		}

		if (user2 == null) {
			log.info(">sini>user:" + user);
			return new GenericResponse("null", "error.userNotActive");
		}

		if (user != null && user2 != null) {
			final String token = UUID.randomUUID().toString();
			log.info(">resetPassword>token:" + token);
			
			log.info("--------------token----------" + token);
			userService.createPasswordResetTokenForUser(user, token);

			String from = "";

			List<SystemSetting> emailSetting = systemSettingRepository.findAll();
			SystemSetting setting = null;
			if (emailSetting.size() > 0) {
				setting = emailSetting.get(0);
				from = setting.getEmailId();
				log.info("--------------from----------" + from);
				SendmailUtils.getJavaMailSender(emailSetting.get(0))
						// mailSender
						.send(constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user, from));
			} else {
				log.info("--------------error----------");
				log.error(">resetPassword. Fail to send mail. Please configure email in DSS portal.");
			}

			return new GenericResponse(messages.getMessage("message.resendToken", null, request.getLocale()));
		}

		return new GenericResponse("hiii");

	}

	@RequestMapping(value = "/changePassword", method = RequestMethod.GET)
	public ModelAndView showChangePasswordPage(final Locale locale, final Model model,
			@RequestParam("id") final long id, @RequestParam("token") final String token) {
		System.out.println(">ID:" + id);
		System.out.println(">token:" + token);
		final String result = securityUserService.validatePasswordResetToken(id, token);

		ModelMap map = new ModelMap();
		if (result != null) {
			map.addAllAttributes(model.asMap());
			map.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
			return new ModelAndView("redirect:/login?lang=" + locale.getLanguage(), map);
		}
		PasswordDto passwordDto = new PasswordDto();
		passwordDto.setToken(token);

		return new ModelAndView("redirect:/updatePassword.html?lang=" + locale.getLanguage() + "&token=" + token, map);
	}

	@RequestMapping(value = "/savePassword", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto,
			@RequestParam("token") final String token) {
		System.out.println(">save password:" + passwordDto.getNewPassword());
		System.out.println(">save password:" + token);
		System.out.println(">save password:" + passwordDto.getToken());

		User user = null;
		final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
		System.out.println(">passToken" + passToken);
		if (passToken != null) {
			System.out.println(">tokennot null-----:" + user);
			user = passToken.getUser();
			final Authentication auth = new UsernamePasswordAuthenticationToken(user, null,
					Arrays.asList(new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
			SecurityContextHolder.getContext().setAuthentication(auth);

		}

		System.out.println(">user:" + user);

		userService.changeUserPassword(user, passwordDto.getNewPassword());
		return new GenericResponse(messages.getMessage("message.resetPasswordSuc", null, locale));
	}

	private SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale,
			final String token, final User user, final String from) {
		log.info("--------------constructResetTokenEmail----------" + user.getEmail());
		final String url = contextPath + "/changePassword?id=" + user.getId() + "&token=" + token;
		final String message = messages.getMessage("message.resetPassword", null, locale);
		return constructEmail("Reset Password", message + " \r\n" + url, user, from);
	}

	private SimpleMailMessage constructEmail(String subject, String body, User user, String from) {
		log.info("--------------email----------" + user.getEmail());
		final SimpleMailMessage email = new SimpleMailMessage();
		email.setSubject(subject);
		email.setText(body);
		email.setTo(user.getEmail());
		email.setFrom(from); // env.getProperty("support.email"));
		
		log.info("--------------constructResetTokenEmail----------" + email.getFrom());
		log.info("--------------constructResetTokenEmail----------" + email.getTo().toString());
		return email;
	}

	private String getAppUrl(HttpServletRequest request) {
		return "https://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

}
