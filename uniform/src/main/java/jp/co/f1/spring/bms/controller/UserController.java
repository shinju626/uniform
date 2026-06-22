package jp.co.f1.spring.bms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityManager;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;

import jp.co.f1.spring.bms.repository.UserRepository;
import jp.co.f1.spring.bms.entity.User;
import jp.co.f1.spring.bms.entity.CheckUser;
import jp.co.f1.spring.bms.entity.Item;
import jp.co.f1.spring.bms.dao.UserDao;

import java.util.ArrayList;
import java.util.Optional;

@Controller
public class UserController {

	// Repositoryインターフェースを自動インスタンス化
	@Autowired
	private UserRepository userinfo;

	// EntityManager自動インスタンス化
	@PersistenceContext
	private EntityManager entityManager;

	// DAO自動インスタンス化
	@Autowired
	private UserDao userDao;

	// セッションを使うためセッションオブジェクトを生成する
	@Autowired
	private HttpSession session;

	/**
	 * 「/login」へGET送信された場合
	 * @param mav
	 * @param request
	 * @return ログイン画面の表示
	 */
	@GetMapping("/login")
	public ModelAndView login(ModelAndView mav, HttpServletRequest request) {

		// Cookieの情報が残っているか確認
		Cookie[] cookies = request.getCookies();

		// 変数の宣言
		String userid = "";
		String password = "";

		// Cookieの情報取得
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if ("userid".equals(cookie.getName())) {
					userid = cookie.getValue();
				} else if ("password".equals(cookie.getName())) {
					password = cookie.getValue();
				}
			}
		}

		// ModelにCookieの値を追加
		mav.addObject("userid", userid);
		mav.addObject("password", password);

		// 画面に出力するViewを指定
		mav.setViewName("view/login");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/login」へPOST送信された場合
	 * @param user
	 * @param result
	 * @param mav
	 * @param request
	 * @param response
	 * @return ログインしてメニュー画面へ遷移
	 */
	@PostMapping(value = "/login")
	public ModelAndView loginPost(@ModelAttribute User user, BindingResult result, ModelAndView mav,
			HttpServletRequest request, HttpServletResponse response) {

		// UserRepository内の関連メソッドでデータベースからユーザーの情報を検索
		Optional<User> optionalUser = userinfo.findByEmailAndPassword(user.getEmail(), user.getPassword());

		// ユーザー情報があるかチェック
		if (!(optionalUser.isPresent())) {
			mav.addObject("errorMessage", "入力内容に誤りがあります。");
			// 画面に出力するViewを指定
			mav.setViewName("view/login");

			// ModelとView情報を返す
			return mav;
		}

		// 該当のユーザーが存在した場合、Cookieの設定をする
		Cookie emailCookie = new Cookie("email", user.getEmail());
		emailCookie.setMaxAge(7200); // 5日間
		response.addCookie(emailCookie);

		Cookie passCookie = new Cookie("password", user.getPassword());
		passCookie.setMaxAge(7200); // 5日間
		response.addCookie(passCookie);

		// セッションの登録
		// 現在ログインしている情報をセッションに追加
		user = optionalUser.get();
		session.setAttribute("user", user);

		// パスワードを変更する際に必要となるセッションを追加
		CheckUser checkUser = new CheckUser();
		checkUser.setEmail(user.getEmail());
		checkUser.setAddress(user.getAddress());
		checkUser.setName(user.getName());
		checkUser.setOldPassword(user.getPassword());
		checkUser.setAuthority(user.getAuthority());
		session.setAttribute("checkUser", checkUser);

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/menu");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/logout」へGET送信された場合
	 * ログアウト
	 * @param mav
	 * @param request
	 * @return ログアウトし、ログイン画面を表示
	 */
	@GetMapping("/logout")
	public ModelAndView logout(ModelAndView mav, HttpServletRequest request) {

		// セッション情報をクリアする
		session.invalidate();

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/login");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/menu」へアクセスがあった場合
	 * @param mav
	 * @return メニュー画面を表示
	 */
	@GetMapping("/menu")
	public ModelAndView menu(ModelAndView mav) {//セッションからユーザー情報取得

		User user = (User) session.getAttribute("user");//セッション切れの場合
		if (user == null) {//エラーメッセージ
			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
			mav.addObject("cmd", "logout");
			mav.addObject("next", "[ログイン画面へ]");// 画面に出力するViewを指定
			mav.setViewName("view/error");// ModelとView情報を返す
			return mav;
		}
		mav.addObject("user", user);

		//Modelにクッキーの値を追加
		mav.addObject("email", user.getEmail());
		mav.addObject("password", user.getPassword());
		mav.addObject("authority", user.getAuthority());

		// 画面に出力するViewを指定
		mav.setViewName("view/menu");

		// ModelとView情報を返す
		return mav;

	}

	/**
	 * 「/menuNormal」へアクセスがあった場合
	 * @param mav
	 * @return メニュー画面を表示
	 */
	@GetMapping("/menuNormal")
	public ModelAndView menuNormal(ModelAndView mav) {//セッションからユーザー情報取得
		// 画面に出力するViewを指定
		mav.setViewName("view/menuNormal");

		// ModelとView情報を返す
		return mav;

	}

	/**
	 * 「/changeUserinfo」へGET送信された場合
	 * ユーザー情報の更新
	 * @param checkUser
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/changeUserinfo")
	public ModelAndView changeUserinfo(@ModelAttribute CheckUser checkUser,
			HttpServletRequest request, ModelAndView mav) {

		// パラメータで取得したユーザーIDを基に各情報を取得する
		Optional<User> optionalUser = userinfo.findByUserid(request.getParameter("userid"));

		// エラーチェック
		if (!(optionalUser.isPresent())) {
			mav.addObject("errorMessage", "更新対象のユーザーが存在しない為、変更画面は表示出来ませんでした。");
			mav.addObject("cmd", "listUser");
			mav.addObject("next", "[ユーザー一覧画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// 各変数をModelに格納
		User oldUser = optionalUser.get();
		mav.addObject("oldUser", oldUser);
		mav.addObject("checkUser", checkUser);

		// 画面に出力するViewを指定
		mav.setViewName("view/changeUserinfo");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/changeUserinfo」へPOST送信された場合
	 * @param checkUser
	 * @param result
	 * @param request
	 * @param mav
	 * @return
	 * ユーザー情報を更新し、ユーザー一覧画面へ遷移
	 */
	@PostMapping(value = "/changeUserinfo")
	public ModelAndView changeUserinfoPost(@ModelAttribute @Validated CheckUser checkUser, BindingResult result,
			HttpServletRequest request, ModelAndView mav) {

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、更新できませんでした。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// ユーザーを検索し、エラーがないか確認する
		Optional<User> optionalUser = userinfo.findByUserid(request.getParameter("userid"));

		// 対象のユーザーが存在しない場合、エラーメッセージを表示

		// ユーザーが存在する場合、oldUserとしてModelに追加
		User oldUser = optionalUser.get();

		// エラーチェック
		// 空白チェック
		if (result.hasErrors()) {

			// 新パスワードはValidationを行っていないので別途エラーチェック
			if (checkUser.getPassword() == "") {
				mav.addObject("passwordError", "パスワードを入力してください");
			}

			// エラーメッセージ
			mav.addObject("errorMessage", "入力内容に誤りがあります");
			mav.addObject("oldUser", oldUser);
			mav.addObject("checkUser", checkUser);

			// 画面に出力するViewを指定
			mav.setViewName("view/changeUserinfo");

			// ModelとView情報を返す
			return mav;
		}

		// ユーザーが存在しない場合
		if (!optionalUser.isPresent()) {
			// エラーメッセージ
			mav.addObject("errorMessage", "更新対象のユーザーが存在しない為、変更画面は表示出来ませんでした。");
			mav.addObject("cmd", "listUser");
			mav.addObject("next", "[ユーザー一覧画面へ戻る]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");

			// ModelとView情報を返す
			return mav;
		}

		// フォームから受け取ったパスワードと確認用のパスワードが一致しているか確認
		if (!checkUser.getPassword().equals(checkUser.getConfirmPassword())) {
			// エラーメッセージ
			mav.addObject("errorMessage", "新パスワードと確認パスワードが合っていません");

			// 各変数をModelに格納
			mav.addObject("oldUser", oldUser);
			mav.addObject("checkUser", checkUser);

			// 画面に出力するViewを指定
			mav.setViewName("view/changeUserinfo");

			// ModelとView情報を返す
			return mav;
		}

		// 確認用のクラスからoldUserへ格納しDBに登録
		oldUser.setName(checkUser.getName());
		oldUser.setEmail(checkUser.getEmail());
		oldUser.setAddress(checkUser.getAddress());
		oldUser.setPassword(checkUser.getPassword());
		oldUser.setAuthority(checkUser.getAuthority());
		userinfo.saveAndFlush(oldUser);

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/listUser");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/listUser」へアクセスがあった場合
	 * @param mav
	 * @return DBからユーザー情報を取得し、
	            管理者ユーザー一覧と会員ユーザー一覧に振り分けて表示する
	 */
	@GetMapping("/listUser")
	public ModelAndView listUser(ModelAndView mav) {

		// DBから全件取得
		Iterable<User> allUsers = userinfo.findAll();

		// HTML に渡す
		mav.addObject("user", allUsers);

		// 表示する画面名
		mav.setViewName("view/listUser");

		return mav;
	}

	/**
	 * 「/deleteUser」へアクセスがあった場合
	 * 対象商品を削除
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/deleteUser")
	public ModelAndView deleteUser(HttpServletRequest request, ModelAndView mav) {

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、削除できませんでした。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}
		mav.addObject("user", user); // ユーザーごとの画面の映し分けに必要

		// 書籍の検索
		Optional<User> optionalUser = userinfo.findById(Integer.parseInt(request.getParameter("userid")));

		// エラーチェック
		if (!(optionalUser.isPresent())) {
			mav.addObject("errorMessage", "削除対象の商品が存在しない為、削除処理は行えませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// isbn入力パラメータを取得し、対象の情報を削除
		userinfo.deleteById(Integer.parseInt(request.getParameter("userid")));

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/listUser");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * エラーが発生した場合
	 * @param e
	 * @return エラー画面とエラーメッセージを表示する
	 *          ログアウトさせる
	 */
	@ExceptionHandler(Exception.class)
	public ModelAndView ExceptionHandler(Exception e) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("cmd", "logout");
		mav.addObject("next", "[ログイン画面へ]");
		mav.addObject("errorMessage", "エラー内容：" + e.getMessage());
		mav.setViewName("view/error");
		return mav;
	}

}
