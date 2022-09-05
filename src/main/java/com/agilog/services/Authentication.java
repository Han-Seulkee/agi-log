package com.agilog.services;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import com.agilog.beans.AuthBean;
import com.agilog.beans.CompanyBean;
import com.agilog.interfaces.ServiceRule;
import com.agilog.utils.Encryption;
import com.agilog.utils.ProjectUtils;

@Service
public class Authentication implements ServiceRule {
	@Autowired
	private SqlSessionTemplate session;
	@Autowired
	private Encryption enc;
	@Autowired
	private ProjectUtils pu;
	private JavaMailSenderImpl mail;
	@Autowired
	private DashBoard dashBoard;

	public Authentication() {
	}

	public void backController(ModelAndView mav, int serviceCode) {
		switch (serviceCode) {
		case 1:
			this.moveLoginCtl(mav);
			break;
		case 2:
			this.moveJoinCtl(mav);
			break;
		case 3:
			this.moveCompanyPageCtl(mav);
			break;
		case 10:
			this.moveJoinFormPageCtl(mav);
			break;
		case 13:
			this.moveCompanyJoinPageCtl(mav);
			break;
		case 14:
			this.naverLoginCtl(mav);
			break;
		case 15:
			this.kakaoLoginCtl(mav);
			break;
		case 20:
			this.socialJoin(mav);
			break;
		case 21:
			this.companyJoinCtl(mav);
			break;
		case 29:
			this.companyLogin(mav);
			break;
		case 93:
			this.logoutCtl(mav);
			break;
		case 94:
			this.companyLogout(mav);
		}
	}

	public void backController(Model model, int serviceCode) {
		switch (serviceCode) {
		case 18:
			this.checkPersonalOverlap(model);
			break;
		}
	}

	private void checkPersonalOverlap(Model model) {
		System.out.println("중복체크 메소드 진입");
		String code = (String) model.getAttribute("code");
		AuthBean ab = (AuthBean) model.getAttribute("authBean");
		int result = 0;
		switch (code) {
		case "0":
			;
			break;
		case "1":
			result = Integer.parseInt(this.session.selectOne("checkNickName", ab).toString());
			break;
		}

		if (this.convertToBoolean(result)) {
			model.addAttribute("check", "중복된 닉네임");
		} else {
			model.addAttribute("check", "ok");
		}
	}

	
//	네이버는 금지!!!!!
	@Transactional(rollbackFor = SQLException.class)
	private void socialJoin(ModelAndView mav) {
		AuthBean ab = (AuthBean) mav.getModel().get("authBean");
		boolean type = false;

		if (ab.getSuCode().length() == 10) {
			type = true;
		}

		try {
			ab.setSuName(this.enc.aesEncode(ab.getSuName(), ab.getSuCode()));
			ab.setSuEmail(this.enc.aesEncode(ab.getSuEmail(), ab.getSuCode()));
			ab.setSuPhone(this.enc.aesEncode(ab.getSuPhone(), ab.getSuCode()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mav.addObject(ab);
		if (this.convertToBoolean(this.session.insert("insSocialUser", ab))) {
			if (type) {
				this.kakaoLoginCtl(mav);
			} else {
				this.naverLoginCtl(mav);
			}
		}
	}

	// 기업회원 로그아웃 제어
	@Transactional(rollbackFor = SQLException.class)
	private void companyLogout(ModelAndView mav) {
		CompanyBean cb;
		try {
			cb = ((CompanyBean) this.pu.getAttribute("companyAccessInfo"));
			if (cb != null) {
				String accessState = this.session.selectOne("isCompanyAccess", cb);
				if (accessState != null) {
					cb.setClAction(-1);
					if (!this.convertToBoolean(this.session.insert("insCompanyAuthLog", cb))) {
					}
					this.pu.removeAttribute("companyAccessInfo");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mav.setViewName("companyLogin");
	}

	// 추가해주기~~~~
	@Transactional(rollbackFor = SQLException.class)
	private void companyLogin(ModelAndView mav) {
		CompanyBean cb = ((CompanyBean) mav.getModel().get("companyBean"));
		String companyPw = cb.getCoPassword();

		// 아이디로 패스워드 가져옴
		this.session.selectOne("isCompanyMember", cb);
		// 암호 일치 확인 this.enc.matches(companyPw, cb.getCoPassword())
		if (companyPw == cb.getCoPassword()) {
			// 일치함
			// 로그인상태인지 로그아웃상태인지 확인
			String accessState = this.session.selectOne("isCompanyAccess", cb);

			if (accessState != null) {
				// 로그아웃 안된 상태, 로그아웃 AccessLog찍어주기
				cb.setClAction(-1);
				this.session.insert("insCompanyAuthLog", cb);
			}

			// AccessLog insert
			cb.setClAction(1);

			if (this.convertToBoolean(this.session.insert("insCompanyAuthLog", cb))) {
				try {
					// 세션에 저장할 로그인 유저 정보 가져오기
					CompanyBean company = (CompanyBean) this.session.selectList("getCompanyAccessInfo", cb).get(0);
					System.out.println(company);
					// 세션에 userCode저장
					this.pu.setAttribute("companyAccessInfo", company);
					// 병원이름 mav에 저장
					((CompanyBean) mav.getModel().get("companyBean")).setCoName(company.getCoName());

					mav.setViewName("checkManager");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			// 일치하지 않음
			mav.setViewName("companyLogin");
			mav.addObject("message", "로그인에 실패하였습니다");
		}
	}

	private void moveLoginCtl(ModelAndView mav) {
		try {
			String page = "";
			AuthBean ab = (AuthBean) this.pu.getAttribute("accessInfo");
			if (ab != null) {
				mav.addObject("accessInfo", ab);
				this.dashBoard.backController(mav, 4);
			} else {
				page = "login";
			}
			mav.setViewName(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void moveJoinCtl(ModelAndView mav) {
		try {
			String page = "";
			AuthBean ab = (AuthBean) this.pu.getAttribute("accessInfo");
			if (ab != null) {
				mav.addObject("accessInfo", ab);
				this.dashBoard.backController(mav, 4);
			} else {
				page = "login";
			}
			mav.setViewName("socialJoin");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void moveCompanyPageCtl(ModelAndView mav) {
		mav.setViewName("companyLogin");
	}

	private void moveJoinFormPageCtl(ModelAndView mav) {
		AuthBean ab = (AuthBean) mav.getModel().get("authBean");
		String page = "login";
		if (ab.getSuCode() != null) {
			int result = Integer.parseInt(this.session.selectOne("isMember", ab).toString());

			if (this.convertToBoolean(result)) {
				mav.addObject("message", "이미 가입된 회원입니다.");
				page = "login";
			} else {
				page = "joinForm";
			}

			mav.addObject("socialInfo", ab);
		}
		mav.setViewName(page);
	}

	private void naverJoinCtl(ModelAndView mav) {

	}

	private void insertNaverMemberCtl(ModelAndView mav) {

	}

	private void kakaoJoinCtl(ModelAndView mav) {

	}

	private void insertKakaoMemberCtl(ModelAndView mav) {

	}

	// 기업 회원가입 페이지 이동
	private void moveCompanyJoinPageCtl(ModelAndView mav) {
		mav.setViewName("companyJoin");
	}

	@Transactional(rollbackFor = SQLException.class)
	private void naverLoginCtl(ModelAndView mav) {
		try {
			String page = "login", action = "";
			if (this.pu.getAttribute("accessInfo") != null) {
				this.dashBoard.backController(mav, 4);
			} else {
				AuthBean ab = (AuthBean) mav.getModel().get("authBean");
				int result = Integer.parseInt(this.session.selectOne("isMember", ab).toString());
				if (this.convertToBoolean(result)) {
					action = this.session.selectOne("isAccess", ab);
					if (action != null && Integer.parseInt(action) > 0) {
						ab.setAlAction(-1);
						this.session.insert("insAuthLog", ab);
					}
					ab.setAlAction(1);
					if (this.convertToBoolean(this.session.insert("insAuthLog", ab))) {
						AuthBean a = (AuthBean) this.session.selectOne("getAccessInfo", ab);
						if (a != null) {
							if (a.getSuCode().length() == 10) {
								a.setType("kakao");
							} else {
								a.setType("naver");
							}
							this.pu.setAttribute("accessInfo", a);
							mav.addObject("accessInfo", a);
							page = "dashBoard";
						}
					}
				} else {
					mav.addObject("message", "가입된 회원이 아닙니다.");
					page = "login";
				}
			}
			mav.setViewName(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional(rollbackFor = SQLException.class)
	private void kakaoLoginCtl(ModelAndView mav) {
		try {
			String page = "login";
			if (this.pu.getAttribute("accessInfo") != null) {
				this.dashBoard.backController(mav, 4);
			} else {
				AuthBean ab = (AuthBean) mav.getModel().get("authBean");
				int result = Integer.parseInt(this.session.selectOne("isMember", ab).toString());
				if (this.convertToBoolean(result)) {
					String action = this.session.selectOne("isAccess", ab);
					if (action != null && Integer.parseInt(action) > 0) {
						ab.setAlAction(-1);
						this.session.insert("insAuthLog", ab);
					}
					ab.setAlAction(1);
					if (this.convertToBoolean(this.session.insert("insAuthLog", ab))) {
						AuthBean a = (AuthBean) this.session.selectOne("getAccessInfo", ab);
						if (a != null) {
							if (a.getSuCode().length() == 10) {
								a.setType("kakao");
							} else {
								a.setType("naver");
							}
							this.pu.setAttribute("accessInfo", a);
							mav.addObject("accessInfo", a);
							System.out.println("여기야");
							this.dashBoard.backController(mav, 4);
						}
					}
				} else {
					mav.addObject("message", "가입된 회원이 아닙니다.");
					mav.setViewName(page);
				}
			}
//			mav.setViewName(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void moveFindCompanyPwCtl(ModelAndView mav) {

	}

	private void findIdCtl(ModelAndView mav) {

	}

	private void sendEmail(AuthBean ab, String title, String content) {

	}

	private void findPersonalPwCtl(ModelAndView mav) {

	}

	private void findCompanyPwCtl(ModelAndView mav) {

	}

	private void moveFindPwPageCtl(ModelAndView mav) {

	}

	// 기업 회원가입
	@Transactional(rollbackFor = SQLException.class)
	private void companyJoinCtl(ModelAndView mav) {
		CompanyBean cb = (CompanyBean) mav.getModel().get("companyBean");

		try {
			// 섀도우 방식 암호화 : 비밀번호, 메일
			cb.setCoPassword(this.enc.encode(cb.getCoPassword()));
			cb.setCoEmail(this.enc.encode(cb.getCoEmail()));

			// aes 방식 암호화 : 이름, 연락처, 주소, 관리자코드 -> 키값은 사업자등록번호
			cb.setCoName(this.enc.aesEncode(cb.getCoName(), cb.getCoCode()));
			cb.setCoPhone(this.enc.aesEncode(cb.getCoPhone(), cb.getCoCode()));
			cb.setCoAddress(this.enc.aesEncode(cb.getCoAddress(), cb.getCoCode()));
			cb.setCoManagerCode(this.enc.aesEncode(cb.getCoManagerCode(), cb.getCoCode()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.convertToBoolean(this.session.insert("insCompanyInfo", cb))) {
			System.out.println("SHY : 기업 회원가입 성공");
			mav.setViewName("companyLogin");
		} else {
			System.out.println("SHY : 기업 회원가입 실패");
		}
		mav.setViewName("companyLogin");
	}

	private void checkCompanyCodeCtl(ModelAndView mav) {

	}

	private void checkCompanyAddressCtl(ModelAndView mav) {

	}

	private void companyLoginCtl(ModelAndView mav) {

	}

	@Transactional
	private void logoutCtl(ModelAndView mav) {
		try {
			AuthBean ab = (AuthBean) this.pu.getAttribute("accessInfo");
			if (ab != null) {
				if (this.session.selectOne("isAccess", ab) != null) {
					ab.setAlAction(-1);
					if (this.convertToBoolean(this.session.insert("insAuthLog", ab))) {
						this.pu.removeAttribute("accessInfo");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.dashBoard.backController(mav, 4);
	}

	private void insertDoctorCtl(ModelAndView mav) {

	}

	private void checkDoctorCtl(ModelAndView mav) {

	}

	private void personalLoginCtl(ModelAndView mav) {

	}

	private String makeRandomPw(String random) {
		String randomPw = null;

		return randomPw;
	}

	private boolean convertToBoolean(int booleanCheck) {
		boolean result = false;
		result = booleanCheck == 0 ? false : true;
		return result;
	}
}
