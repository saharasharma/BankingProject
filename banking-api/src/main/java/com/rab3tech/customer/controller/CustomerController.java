package com.rab3tech.customer.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rab3tech.admin.service.PayeeInfoService;
import com.rab3tech.email.service.EmailService;
import com.rab3tech.vo.ApplicationResponseVO;
import com.rab3tech.vo.CustomerVO;
import com.rab3tech.vo.EmailVO;
import com.rab3tech.vo.PayeeInfoVO;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v3")
public class CustomerController {
	
	@Autowired
	PayeeInfoService payeeInfoService;
	
	@Autowired 
	private EmailService emailService;
	
	@GetMapping(value="/customer/managePayees/{username}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public List<PayeeInfoVO> managePayee(@PathVariable String username ) {
			List<PayeeInfoVO> payeeInfoVO = payeeInfoService.findByCustomerId(username);
			return payeeInfoVO;
	}
	
	@GetMapping(value="/customer/manageAllPayees", produces = {MediaType.APPLICATION_JSON_VALUE})
	public List<PayeeInfoVO> managePayee() {
			List<PayeeInfoVO> payeeInfoVO = payeeInfoService.findAllPayee();
			return payeeInfoVO;
	}
	
	
	@GetMapping(value="/customer/managePayee/{payeeId}", produces = {MediaType.APPLICATION_JSON_VALUE})
	public PayeeInfoVO findPayee(@PathVariable int payeeId) {
			PayeeInfoVO payeeInfoVO = new PayeeInfoVO();
			payeeInfoVO= payeeInfoService.findById(payeeId);
			return payeeInfoVO;
	}
	
	
	@GetMapping("/customer/deletePayee/{payeeId}")
	public ApplicationResponseVO deletePayee (@PathVariable int payeeId) {
		ApplicationResponseVO payeeVo= new ApplicationResponseVO();
		payeeInfoService.deletePayeeById(payeeId);
		payeeVo.setCode(200);
		payeeVo.setMessage("payee has been deleted sucessfully");
		payeeVo.setStatus("success");
		return payeeVo;
	}

	
		@PostMapping(value="/customer/editPayee", produces = {MediaType.APPLICATION_JSON_VALUE})
		public ApplicationResponseVO editPayee(@RequestBody PayeeInfoVO payeeInfoVO) {
			ApplicationResponseVO response= new ApplicationResponseVO();
				payeeInfoService.editPayee(payeeInfoVO);
				response.setCode(200);
				response.setStatus("Success");
				response.setMessage("Payee had been edited");
				
				//send email after success
				
				PayeeInfoVO payee=findPayee(payeeInfoVO.getId());
				EmailVO email= new EmailVO(payee.getCustomerId(), "saharasharma@gmail.com", "Edit Payee Email", payee.getPayeeAccountNo(), payee.getPayeeName());
				emailService.sendEditPayeeEmail(email);

			return response;
		}
			
		
		}

		
		
		




	


