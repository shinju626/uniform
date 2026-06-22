package jp.co.f1.spring.bms.controller;

import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.f1.spring.bms.dao.OrderDao;
import jp.co.f1.spring.bms.entity.User;
import jp.co.f1.spring.bms.entity.Order;
import jp.co.f1.spring.bms.repository.ItemRepository;
import jp.co.f1.spring.bms.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OrderController {

	// Repositoryインターフェースを自動インスタンス化
	@Autowired
	private ItemRepository iteminfo;
	@Autowired
	private OrderRepository orderinfo;

	// EntityManager自動インスタンス化
	@PersistenceContext
	private EntityManager entityManager;

	// DAO自動インスタンス化
	@Autowired
	private OrderDao orderDao;

	// セッションを使うためセッションオブジェクトを生成する
	@Autowired
	private HttpSession session;

	/**
	 * 「/detailOrder」へアクセスがあった場合
	 * 対象書籍の詳細画面の表示
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/detailOrder")
	public ModelAndView detailOrder(HttpServletRequest request, ModelAndView mav) {

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、詳細を表示できません。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}
		mav.addObject("user", user); // ユーザーごとの画面の映し分けに必要

		// 受注情報の検索
		Optional<Order> optionalOrder = orderinfo.findById(Integer.parseInt(request.getParameter("orderid")));

		// 書籍情報があるかどうかチェック
		if (!(optionalOrder.isPresent())) {
			mav.addObject("errorMessage", "表示対象の受注情報が存在しない為、詳細情報は表示出来ませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// 検索によって得られた変数をModelに格納
		mav.addObject("order", optionalOrder.get());

		// 画面に出力するViewを指定
		mav.setViewName("view/detailOrder");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/updateOrder」へアクセスがあった場合
	 * 対象書籍の詳細画面の表示
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/updateOrder")
	public ModelAndView updateOrder(HttpServletRequest request, ModelAndView mav) {

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、詳細を表示できません。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}
		mav.addObject("user", user); // ユーザーごとの画面の映し分けに必要

		// 受注情報の検索
		Optional<Order> optionalOrder = orderinfo.findById(Integer.parseInt(request.getParameter("orderid")));

		// 書籍情報があるかどうかチェック
		if (!(optionalOrder.isPresent())) {
			mav.addObject("errorMessage", "表示対象の受注情報が存在しない為、詳細情報は表示出来ませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// 検索によって得られた変数をModelに格納
		mav.addObject("order", optionalOrder.get());

		// 画面に出力するViewを指定
		mav.setViewName("view/updateOrder");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/updateOrder」へPOST送信された場合
	 * @param request
	 * @param mav
	 * @return 入金状況・発送状況更新ボタン
	 */
	@PostMapping(value = "/updateOrder")
	public ModelAndView updateOrderPost(HttpServletRequest request, ModelAndView mav) {

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、発送を行えませんでした。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// 入金状況、発送状況をそれぞれ入力された値に変更して更新
		Optional<Order> optionalOrder = orderinfo.findById(Integer.parseInt(request.getParameter("orderid")));

		if (!(optionalOrder.isPresent())) {
			//エラーメッセージ
			mav.addObject("errorMessage", "更新対象の注文が存在しない為、更新処理は行えません。");
			mav.addObject("cmd", "menu");
			mav.addObject("next", "[メニュー画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;
		}

		Order order = optionalOrder.get();
		order.setIspaid(Integer.parseInt(request.getParameter("ispaid")));
		order.setIsshipped(Integer.parseInt(request.getParameter("isshipped")));
		orderinfo.saveAndFlush(order);

		// 出品中の商品一覧にリダイレクト
		mav.setViewName("redirect:/listOrder");

		return mav;

	}
	
	@GetMapping("/listOrder")
	public ModelAndView listOrder(ModelAndView mav) {

	    // DBから全件取得
	    Iterable<Order> allOrders = orderinfo.findAll();

	    // HTML に渡す
	    mav.addObject("orderList", allOrders);

	    // 表示する画面
	    mav.setViewName("view/listOrder");

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
