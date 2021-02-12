package com.rab3tech.customer.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rab3tech.admin.service.CustomerAccountInfoService;
import com.rab3tech.admin.service.PayeeInfoService;
import com.rab3tech.admin.service.impl.CustomerSecurityQuestionsServiceImpl;
import com.rab3tech.customer.service.CustomerService;
import com.rab3tech.customer.service.LoginService;
import com.rab3tech.customer.service.impl.CustomerEnquiryService;
import com.rab3tech.customer.service.impl.SecurityQuestionService;
import com.rab3tech.email.service.EmailService;
import com.rab3tech.vo.AccountStatementVO;
import com.rab3tech.vo.ChangePasswordVO;
import com.rab3tech.vo.CustomerAccountInfoVO;
import com.rab3tech.vo.CustomerAccountTransactionVO;
import com.rab3tech.vo.CustomerSavingVO;
import com.rab3tech.vo.CustomerSecurityQueAnsVO;
import com.rab3tech.vo.CustomerVO;
import com.rab3tech.vo.EmailVO;
import com.rab3tech.vo.LoginVO;
import com.rab3tech.vo.PayeeInfoVO;
import com.rab3tech.vo.SecurityQuestionsVO;

/**
 * 
 * @author nagendra
 * This class for customer GUI
 *
 */
@Controller
public class CustomerUIController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerUIController.class);

	@Autowired
	private CustomerEnquiryService customerEnquiryService;

	
	@Autowired
	private SecurityQuestionService securityQuestionService;
	
	
	@Autowired
	private CustomerService customerService;

	@Autowired
	private EmailService emailService;
	
	@Autowired
   private LoginService loginService;
	
	@Autowired
	private CustomerSecurityQuestionsServiceImpl customerSQSI;
	
	@Autowired
	private CustomerAccountInfoService customerAccountInfoService;
	
	@Autowired 
	private PayeeInfoService payeeInfoService;
	
	
	@PostMapping("/customer/changePassword")
	public String saveCustomerQuestions(@ModelAttribute ChangePasswordVO changePasswordVO, Model model,HttpSession session) {
		LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
		String loginid=loginVO2.getUsername();
		changePasswordVO.setLoginid(loginid);
		String viewName ="customer/dashboard";
		boolean status=loginService.checkPasswordValid(loginid,changePasswordVO.getCurrentPassword());
		if(status) {
			if(changePasswordVO.getNewPassword().equals(changePasswordVO.getConfirmPassword())) {
				 viewName ="customer/dashboard";
				 loginService.changePassword(changePasswordVO);
			}else {
				model.addAttribute("error","Sorry , your new password and confirm passwords are not same!");
				return "customer/login";	//login.html	
			}
		}else {
			model.addAttribute("error","Sorry , your username and password are not valid!");
			return "customer/login";	//login.html	
		}
		return viewName;
	}
	
	@PostMapping("/customer/securityQuestion")
	public String saveCustomerQuestions(@ModelAttribute("customerSecurityQueAnsVO") CustomerSecurityQueAnsVO customerSecurityQueAnsVO, Model model,HttpSession session) {
		LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
		String loginid=loginVO2.getUsername();
		customerSecurityQueAnsVO.setLoginid(loginid);
		securityQuestionService.save(customerSecurityQueAnsVO);
		//
		return "customer/chagePassword";
	}

	// http://localhost:444/customer/account/registration?cuid=1585a34b5277-dab2-475a-b7b4-042e032e8121603186515
	@GetMapping("/customer/account/registration")
	public String showCustomerRegistrationPage(@RequestParam String cuid, Model model) {

		logger.debug("cuid = " + cuid);
		Optional<CustomerSavingVO> optional = customerEnquiryService.findCustomerEnquiryByUuid(cuid);
		CustomerVO customerVO = new CustomerVO();

		if (!optional.isPresent()) {
			return "customer/error";
		} else {
			// model is used to carry data from controller to the view =- JSP/
			CustomerSavingVO customerSavingVO = optional.get();
			customerVO.setEmail(customerSavingVO.getEmail());
			customerVO.setName(customerSavingVO.getName());
			customerVO.setMobile(customerSavingVO.getMobile());
			customerVO.setAddress(customerSavingVO.getLocation());
			customerVO.setToken(cuid);
			logger.debug(customerSavingVO.toString());
			// model - is hash map which is used to carry data from controller to thyme
			// leaf!!!!!
			// model is similar to request scope in jsp and servlet
			model.addAttribute("customerVO", customerVO);
			return "customer/customerRegistration"; // thyme leaf
		}
	}

	@PostMapping("/customer/account/registration")
	public String createCustomer(@ModelAttribute CustomerVO customerVO, Model model) {
		// saving customer into database
		logger.debug(customerVO.toString());
		customerVO = customerService.createAccount(customerVO);
		// Write code to send email

		EmailVO mail = new EmailVO(customerVO.getEmail(), "javahunk2020@gmail.com",
				"Regarding Customer " + customerVO.getName() + "  userid and password", "", customerVO.getName());
		mail.setUsername(customerVO.getUserid());
		mail.setPassword(customerVO.getPassword());
		emailService.sendUsernamePasswordEmail(mail);
		System.out.println(customerVO);
		model.addAttribute("loginVO", new LoginVO());
		model.addAttribute("message", "Your account has been setup successfully , please check your email.");
		return "customer/login";
	}

	@GetMapping(value = { "/customer/account/enquiry", "/", "/mocha", "/welcome" })
	public String showCustomerEnquiryPage(Model model) {
		CustomerSavingVO customerSavingVO = new CustomerSavingVO();
		// model is map which is used to carry object from controller to view
		model.addAttribute("customerSavingVO", customerSavingVO);
		return "customer/customerEnquiry"; // customerEnquiry.html
	}

	@PostMapping("/customer/account/enquiry")
	public String submitEnquiryData(@ModelAttribute CustomerSavingVO customerSavingVO, Model model) {
		boolean status = customerEnquiryService.emailNotExist(customerSavingVO.getEmail());
		logger.info("Executing submitEnquiryData");
		if (status) {
			CustomerSavingVO response = customerEnquiryService.save(customerSavingVO);
			logger.debug("Hey Customer , your enquiry form has been submitted successfully!!! and appref "
					+ response.getAppref());
			model.addAttribute("message",
					"Hey Customer , your enquiry form has been submitted successfully!!! and appref "
							+ response.getAppref());
		} else {
			model.addAttribute("message", "Sorry , this email is already in use " + customerSavingVO.getEmail());
		}
		return "customer/success"; // customerEnquiry.html

	}
	@GetMapping("/customer/myProfile") 
	public String myProfilePage (Model model, HttpSession session) {
		LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
		if (loginVO2 !=null) {
		String loginid=loginVO2.getUsername();	
		CustomerVO customer= customerService.getCustomer(loginid);
		CustomerSecurityQueAnsVO quesAns=securityQuestionService.findQuestionAnswer(loginid);
		customer.setQuestion1(quesAns.getSecurityQuestion1());
		customer.setQuestion2(quesAns.getSecurityQuestion2());
		customer.setAnswer1(quesAns.getSecurityQuestionAnswer1());
		customer.setAnswer2(quesAns.getSecurityQuestionAnswer2());
		model.addAttribute("customerVO",customer);

		
		List<SecurityQuestionsVO> questionsVOs= securityQuestionService.findAll();
		model.addAttribute("securityQuestionsVO", questionsVOs);
	


		return "customer/myProfile"; 
		} else {
			//go to login
			return "customer/login";
		}
	}
		
		
	@PostMapping("/customer/update") 
	public String updateMyProfile (@ModelAttribute CustomerVO customer, Model model, HttpSession session) {
		LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
		if (loginVO2 !=null) {
		CustomerSecurityQueAnsVO csq= new CustomerSecurityQueAnsVO();
		csq.setLoginid(loginVO2.getUsername());
		csq.setSecurityQuestion1(customer.getQuestion1());
		csq.setSecurityQuestion2(customer.getQuestion2());
		csq.setSecurityQuestionAnswer1(customer.getAnswer1());
		csq.setSecurityQuestionAnswer2(customer.getAnswer2());
		customerService.save(customer);
		securityQuestionService.update(csq);
	
		model.addAttribute("message", "user has been updated");
		model.addAttribute("customer", customer);
		
//		List<SecurityQuestionsVO> questionsVOs= securityQuestionService.findAll();
//		model.addAttribute("securityQuestionsVO", questionsVOs);
//		
		return "redirect:/customer/myProfile";
		}else {
			return "customer/login";
		}
	}
		
	
		@GetMapping("/customer/addPayee") // by sahara
		public String addPayee (Model model, HttpSession session) {
			LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
			if (loginVO2 !=null) {
			CustomerAccountInfoVO caiVO=customerAccountInfoService.findCustomer(loginVO2.getUsername());
			if (customerAccountInfoService==null) {
				model.addAttribute("message", "you do not have an accout. Please contact you bank to create one");	
			}
			
		return "customer/addPayee"; 
		} else {
			//go to login
			return "customer/login";
		}
			
	}
		
		@PostMapping("/customer/savePayee") //by sahara
		public String updateMyProfile (@ModelAttribute PayeeInfoVO payeeInfo, Model model, HttpSession session) {
			LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
			if (loginVO2 !=null) {
				payeeInfo.setCustomerId(loginVO2.getUsername());
				String message= payeeInfoService.savePayee(payeeInfo);	
				model.addAttribute("message", message);
				return "customer/addPayee"; 

			} else {
				return "customer/login";

		
			}
		}
			// transfer funds to another customer get mapping // by sahara
			@GetMapping("/customer/transfer") 
			public String transfer (Model model, HttpSession session) {
				LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
				if (loginVO2 !=null) {			
					List <PayeeInfoVO> payeeInfoVO=payeeInfoService.findByCustomerId(loginVO2.getUsername());
					model.addAttribute("payeeInfoVOs",payeeInfoVO);
					System.out.println(payeeInfoVO+ "_______________________--------------------__________________________________");
				return "customer/transferMoney";
				} else {
				return "customer/login";

		
			}
			}

		
				@PostMapping("/customer/transaction")
			public String transfer (@ModelAttribute CustomerAccountTransactionVO customerAccountTransactionVO, Model model, HttpSession session) {
				LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
				if (loginVO2 !=null) {	
				customerAccountTransactionVO.setCustomerId(loginVO2.getUsername());	
				String message= customerAccountInfoService.save(customerAccountTransactionVO);
				model.addAttribute("message", message);
				System.out.println(message+ "__________*******");
				List <PayeeInfoVO> payeeInfoVO=payeeInfoService.findByCustomerId(loginVO2.getUsername());
				model.addAttribute("payeeInfoVOs",payeeInfoVO);
				return "customer/transferMoney";
				} else {
				return "customer/login";
				}
				
				}
				
			@GetMapping("/customer/showStatement") 
			public String showStatements (Model model, HttpSession session) {
				LoginVO  loginVO2=(LoginVO)session.getAttribute("userSessionVO");
				if (loginVO2 !=null) {
					List<AccountStatementVO> statements= customerAccountInfoService.findCustomerByAccountNumber(loginVO2.getUsername());
					model.addAttribute("customerAccountInfoVO", statements);
					System.out.println(statements + "____________________**************");

				return "customer/showStatement";
				} else {
				return "customer/login";

		
			}
			}
			
			@GetMapping("/customer/payeeManagement")
			public String managePayee(Model model, HttpSession session) {
				LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
				if (loginVO2 != null) {
					List<PayeeInfoVO> payeeInfoVO = payeeInfoService.findByCustomerId(loginVO2.getUsername());
						model.addAttribute("payeeInfoVO", payeeInfoVO);
					return "/customer/payeeManagement";
				}else
				return "customer/login";
			}
				
				@PostMapping("/customer/editPayee")
				public String editPayee(@ModelAttribute PayeeInfoVO payeeInfoVO, Model model, HttpSession session) {
					LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
					if (loginVO2 != null) {
						payeeInfoService.editPayee(payeeInfoVO);
						List<PayeeInfoVO> payeeInfoVO1 = payeeInfoService.findByCustomerId(loginVO2.getUsername());
						model.addAttribute("payeeInfoVO", payeeInfoVO1);
						model.addAttribute("message", "Payee has been updated successfully!");
						return "/customer/payeeManagement";
					} else {
						return "customer/login";
					}
				}
			
}




				


	


