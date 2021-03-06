package bg.car_wash.areas.user.controllers;

import bg.car_wash.areas.role.entity.Role;
import bg.car_wash.areas.user.entity.User;
import bg.car_wash.areas.user.entity.UserType;
import bg.car_wash.areas.user.models.bindingModels.UserLoginBindingModel;
import bg.car_wash.areas.user.models.bindingModels.UserRegisterBindingModel;
import bg.car_wash.areas.user.models.viewModels.UserEditViewModel;
import bg.car_wash.areas.user.models.viewModels.UserViewModel;
import bg.car_wash.areas.user.service.UserService;
import bg.car_wash.configurations.error.Errors;
import bg.car_wash.configurations.site.PageTitle;
import bg.car_wash.configurations.user.UserConfiguration;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/user")
public class UserController {

	private UserService userService;
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	private ModelMapper modelMapper;

	@Autowired
	public UserController(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, ModelMapper modelMapper) {
		this.userService = userService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.modelMapper = modelMapper;
	}

	@GetMapping("/status")
	public String getUserStatusPage(HttpServletRequest httpServletRequest, Model model) {

		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserViewModel userViewModel = this.modelMapper.map(user, UserViewModel.class);

		model.addAttribute("pageTitle", PageTitle.USER_STATUS_PAGE);
		model.addAttribute("userViewModel", userViewModel);

		return "user/user-status";
	}

	@GetMapping("/login")
	public String getLoginPage(
			Model model,
			@ModelAttribute UserLoginBindingModel userLoginBindingModel,
			@RequestParam(required = false) String error) {
		model.addAttribute("pageTitle", PageTitle.LOGIN_PAGE);

		if (error != null) {
			model.addAttribute("error", Errors.INVALID_CREDENTIALS);
		}

		return "user/user-login";
	}

	@GetMapping("/register")
	public String getRegisterPage(Model model, @ModelAttribute UserRegisterBindingModel userRegisterBindingModel) {
		model.addAttribute("pageTitle", PageTitle.REGISTER_PAGE);
		return "user/user-register";
	}

	@PostMapping("/register")
	public String registerUser(@Valid @ModelAttribute UserRegisterBindingModel userRegisterBindingModel, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "user/user-register";
		}

		User user = this.modelMapper.map(userRegisterBindingModel, User.class);
		user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
		user.setUserType(UserType.WORKER);
		user.setRemuneration(new BigDecimal(UserConfiguration.WORKER_REMUNERATION));
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);

		Role role = new Role();
		role.setAuthority("WORKER");
		Set<Role> roleSet = new HashSet<>();
		roleSet.add(role);
		user.setAuthorities(roleSet);

		this.userService.createUser(user);

		return "redirect:/user/login";
	}

	@GetMapping("/edit/{id}")
	public String getEditServiceByIdPage(
			@PathVariable(value = "id") Long id,
			Model model,
			@Valid @ModelAttribute UserRegisterBindingModel userRegisterBindingModel,
			BindingResult bindingResult) {

		model.addAttribute("pageTitle", PageTitle.USER_EDIT_PAGE);
		model.addAttribute("userViewModel", getUserViewModel(id));

		return "user/user-edit";
	}

	@PostMapping("/edit/{id}")
	public String editServiceByIdPage(
			@PathVariable(value = "id") Long id,
			Model model,
			@Valid @ModelAttribute UserRegisterBindingModel userRegisterBindingModel,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("pageTitle", PageTitle.USER_EDIT_PAGE);
			model.addAttribute("userViewModel", getUserViewModel(id));

			return "user/user-edit";
		}

		User user = this.modelMapper.map(userRegisterBindingModel, User.class);
		user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
		user.setUserType(UserType.WORKER);
		user.setRemuneration(new BigDecimal(UserConfiguration.WORKER_REMUNERATION));
		user.setAccountNonExpired(true);
		user.setAccountNonLocked(true);
		user.setCredentialsNonExpired(true);
		user.setEnabled(true);

		this.userService.updateUser(user);

		return "redirect:/car/all";
	}

	@GetMapping("/delete/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public String deleteUser(@PathVariable(value = "id") Long id) {
		this.userService.deleteUserById(id);

		return "redirect:/logout";
	}

	@GetMapping("/logout")
	public String logoutUser(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			Authentication authentication) {
		Cookie[] cookies = httpServletRequest.getCookies();

		authentication.setAuthenticated(false);

		for (Cookie cookie : cookies) {
			cookie.setMaxAge(0);
			httpServletResponse.addCookie(cookie);
		}

		HttpSession session = httpServletRequest.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		return "redirect:/user/login";
	}

	private UserEditViewModel getUserViewModel(Long id) {
		User user = this.userService.findUserById(id);
		UserEditViewModel userViewModel = this.modelMapper.map(user, UserEditViewModel.class);

		return userViewModel;
	}

}
