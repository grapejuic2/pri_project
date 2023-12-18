package com.project.gogi.member.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.project.gogi.common.base.BaseController;
import com.project.gogi.member.service.MemberService;
import com.project.gogi.vo.MemberVO;

@Controller("memberController")
@RequestMapping(value = "/member")
public class MemberControllerImpl extends BaseController implements MemberController {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberVO memberVO;

	@Autowired
	private MemberRegisterMail memberRegisterMail;

	@Override
	@RequestMapping(value = "/loginForm.do", method = RequestMethod.POST)
	public ModelAndView loginForm(@RequestParam Map<String, String> loginMap, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView mav = new ModelAndView();
		memberVO = memberService.loginForm(loginMap);
		if (memberVO != null && memberVO.getMem_id() != null) {
			if (memberVO.getMem_del_yn().equals("Y")) {
				String message = "탈퇴한 회원입니다.";
				mav.addObject("message", message);
				mav.setViewName("/member/loginForm");
			} else if (memberVO.getMem_del_yn().equals("B")) {
				String message = "비활성화된 회원입니다";
				mav.addObject("message", message);
				mav.setViewName("/member/loginForm");
			} else {
				HttpSession session = request.getSession();
				session.setAttribute("isLogon", true);
				session.setAttribute("memberInfo", memberVO);
				System.out.println("로그인" + memberVO.getMem_id());

				if (memberVO.getMem_id().equals("admin")) {
					session.setAttribute("isAdmin", true);
				} else {
					session.setAttribute("isAdmin", false);
				}
				System.out.println(memberVO.getMem_id() + " 로그인 완료");
				session = request.getSession(true);
				mav.setViewName("redirect:/main/main.do");
			}
		} else {
			String message = "아이디나 비밀번호가 틀립니다. 다시 로그인해주세요";
			mav.addObject("message", message);
			mav.setViewName("/member/loginForm");
		}
		return mav;
	}

	// 로그아웃 사용 시 컨트롤러
	@Override
	@RequestMapping(value = "/logout.do", method = RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView();
		HttpSession session = request.getSession(false);

		if (session != null) {
			session.invalidate();
			session = request.getSession(true);
			session.setAttribute("isLogOn", false);
			System.out.println(memberVO.getMem_id() + " 로그아웃 완료");
		}

		mav.setViewName("redirect:/main/main.do");
		return mav;
	}

	// 회원가입 사용 시 컨트롤러
	@Override
	@RequestMapping(value = "/memberForm.do", method = RequestMethod.POST)
	public ResponseEntity addMember(@ModelAttribute("memberVO") MemberVO _memberVO, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("utf-8");
		String message = null;
		ResponseEntity resEntity = null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			// 회원 정보를 SQL문으로 전달합니다.
			memberService.insertNewMember(_memberVO);

			message = "<script>";
			message += " alert('회원 가입이 완료됐습니다! 로그인창으로 이동합니다.');";
			message += " location.href='" + request.getContextPath() + "/member/loginForm.do';";
			message += " </script>";

		} catch (Exception e) {
			message = "<script>";
			message += " alert('작업 중 오류가 발생했습니다. 다시 시도해 주세요.');";
			message += " location.href='" + request.getContextPath() + "/member/memberForm.do';";
			message += " </script>";
			e.printStackTrace();
		}
		resEntity = new ResponseEntity(message, responseHeaders, HttpStatus.OK);
		return resEntity;
	}

	// 아이디 중복 검사 시 실행
	@Override
	@RequestMapping(value = "/checkId.do", method = RequestMethod.POST)
	public ResponseEntity checkId(@RequestParam("mem_id") String id, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ResponseEntity resEntity = null;
		String result = memberService.checkId(id); // ID 중복 검사를 합니다.
		resEntity = new ResponseEntity(result, HttpStatus.OK);
		return resEntity;
	}

	//이메일 인증
	@ResponseBody
	@RequestMapping(value ="/emailConfirm.do", method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> mailConfirm(@RequestParam("mem_email") String email) {
		   try {
		       String code = memberRegisterMail.sendSimpleMessage(email);
		       System.out.println("인증코드: " + code);
		       Map<String, String> responseObject = new HashMap<>();
		       responseObject.put("code", code);
		       return ResponseEntity.ok().body(responseObject);
		   } catch (Exception e) {
		       e.printStackTrace();
		       // 예외가 발생한 경우 오류 메시지를 반환
		       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		   }
	}

	@RequestMapping(value = "/*Form.do", method = RequestMethod.GET)
	public ModelAndView form(@RequestParam(value = "result", required = false) String result,
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "goods_id", required = false) String goods_id, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String viewName = (String) request.getAttribute("viewName");
		HttpSession session = request.getSession();
		session.setAttribute("action", action);
		if (goods_id != null) {
			session.setAttribute("goods_id", goods_id);
		}
		ModelAndView mav = new ModelAndView();
		mav.addObject("result", result);
		mav.setViewName(viewName);
		return mav;
	}

}