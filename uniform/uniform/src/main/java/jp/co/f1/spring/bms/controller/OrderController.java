package jp.co.f1.spring.bms.controller;

import java.util.Date;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import jp.co.f1.spring.bms.dao.OrderDao;
import jp.co.f1.spring.bms.entity.User;
import jp.co.f1.spring.bms.entity.Item;
import jp.co.f1.spring.bms.entity.Order;
import jp.co.f1.spring.bms.repository.ItemRepository;
import jp.co.f1.spring.bms.repository.OrderRepository;
import jp.co.f1.spring.bms.repository.UserRepository;

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

	@Autowired
	private UserRepository userinfo;

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
	public ModelAndView listOrder(ModelAndView mav, Order order) {

		// DBから全件取得
		Iterable<Order> allOrders = orderDao.sort(order.getDate());

		// HTML に渡す
		mav.addObject("orderList", allOrders);

		// 表示する画面
		mav.setViewName("view/listOrder");

		return mav;
	}

	/**
	 * 「/orderHistory」へGET送信された場合
	 * @param mav
	 * @return 注文履歴
	 */
	@GetMapping("/orderHistory")
	public ModelAndView orderHistory(ModelAndView mav) {
		//セッションからユーザー情報取得
		User user = (User) session.getAttribute("user");
		//セッション切れの場合
		if (user == null) {//エラーメッセージ
			mav.addObject("errorMessage", "セッション切れの為、");
			mav.addObject("cmd", "logout");
			mav.addObject("next", "[ログイン画面へ]");// 画面に出力するViewを指定
			mav.setViewName("view/error");// ModelとView情報を返す
			return mav;
		}

		// UserRepositoryのメソッドを使用し、ユーザーの情報を全件取得する
		Iterable<Order> order_list = orderinfo.findByUserid(user.getUserid());

		mav.addObject("order_list", order_list);
		//画面に出力するViewを指定
		mav.setViewName("view/orderHistory");
		//ModelとView情報を返す
		return mav;
	}

	/**
	 * 「buyItem」へアクセスがあった場合
	 * @param request
	 * @param mav
	 * @return 購入画面(会員)を表示
	 */
	@GetMapping("/buyItem")
	public ModelAndView buyItem(HttpServletRequest request, ModelAndView mav) {
		//セッションからユーザー情報取得
		User user = (User) session.getAttribute("user");
		//セッション切れの場合
		if (user == null) {
			//エラーメッセージ
			mav.addObject("errorMessage", "セッション切れの為、詳細を表示できません。");
			mav.addObject("cmd", "logout");
			mav.addObject("next", "[ログイン画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;

		}
		//Viewに渡す変数をModelに格納
		mav.addObject("user", user);

		//パラメータで取得した値を基に商品を検索
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));

		Order order = new Order();
		order.setQuantity(Integer.parseInt(request.getParameter("quantity")));
		mav.addObject("order", order);

		//一覧画面のリンクをクリック時、表示対象の商品が存在しない
		if (!(optionalItem.isPresent())) {
			//エラーメッセージ
			mav.addObject("errorMessage", "詳細対象の商品が存在しない為、詳細情報処理は行えません。");
			mav.addObject("cmd", "itemList");
			mav.addObject("next", "[商品一覧画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;

		}

		//Viewに渡す変数をModelに格納
		mav.addObject("item", optionalItem.get());

		//画面に出力するViewを指定
		mav.setViewName("view/buyItem");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「buyNormal」へアクセスがあった場合
	 * @param request
	 * @param mav
	 * @return 購入画面(会員)を表示
	 */
	@GetMapping("/buyNormal")
	public ModelAndView buyNormal(HttpServletRequest request, ModelAndView mav) {
		//パラメータで取得した値を基に商品を検索
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));

		Order order = new Order();
		order.setQuantity(Integer.parseInt(request.getParameter("quantity")));
		mav.addObject("order", order);

		//一覧画面のリンクをクリック時、表示対象の商品が存在しない
		if (!(optionalItem.isPresent())) {
			//エラーメッセージ
			mav.addObject("errorMessage", "詳細対象の商品が存在しない為、詳細情報処理は行えません。");
			mav.addObject("cmd", "itemList");
			mav.addObject("next", "[商品一覧画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;

		}

		//Viewに渡す変数をModelに格納
		mav.addObject("item", optionalItem.get());
		User user = new User();
		mav.addObject("user", user);

		//画面に出力するViewを指定
		mav.setViewName("view/buyNormal");

		// ModelとView情報を返す
		return mav;
	}

	/**
	 * 「buyConfirm」へアクセスがあった場合
	 * @param request
	 * @param mav
	 * @return 購入後画面を表示
	 */
	@GetMapping("/buyConfirm")
	public ModelAndView buyConfirm(HttpServletRequest request, ModelAndView mav) {
		if (request.getParameter("authority").equals("3")) {
			User newUser = new User();
			newUser.setPassword("非会員");
			newUser.setEmail(request.getParameter("email"));
			newUser.setName(request.getParameter("name"));
			newUser.setAddress(request.getParameter("address"));
			newUser.setAuthority(3);
			userinfo.saveAndFlush(newUser);

			mav.addObject("user", newUser);

			Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));
			Item item = optionalItem.get();

			Order order = new Order();
			Date date = new Date();
			order.setQuantity(Integer.parseInt(request.getParameter("quantity")));
			order.setUserid(newUser.getUserid());
			order.setItemid(Integer.parseInt(request.getParameter("itemid")));
			order.setDescription(request.getParameter("description"));
			order.setIspaid(0);
			order.setIsshipped(0);
			order.setDate(date);
			int total = Integer.parseInt(request.getParameter("quantity")) * item.getPrice();
			order.setTotal(total);
			orderinfo.saveAndFlush(order);

			mav.addObject("order", order);

			mav.addObject("total", total);

			// 画面に出力するViewを指定
			mav.setViewName("view/buyConfirm");

			// ModelとView情報を返す
			return mav;

		} else {
			//セッションからUserの値を取得する
			User user = (User) session.getAttribute("user");

			//セッション切れの場合
			if (user == null) {
				//エラーメッセージ
				mav.addObject("errorMessage", "セッション切れの為、購入は出来ません。");
				mav.addObject("cmd", "logout");
				mav.addObject("next", "[ログイン画面へ]");
				// 画面に出力するViewを指定
				mav.setViewName("view/error");
				// ModelとView情報を返す
				return mav;
			}
			mav.addObject("user", user);

			Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));
			Item item = optionalItem.get();

			Order order = new Order();
			Date date = new Date();
			order.setQuantity(Integer.parseInt(request.getParameter("quantity")));
			order.setUserid(user.getUserid());
			order.setItemid(Integer.parseInt(request.getParameter("itemid")));
			order.setDescription(request.getParameter("description"));
			order.setIspaid(0);
			order.setIsshipped(0);
			order.setDate(date);
			int total = Integer.parseInt(request.getParameter("quantity")) * item.getPrice();
			order.setTotal(total);
			orderinfo.saveAndFlush(order);

			mav.addObject("order", order);

			mav.addObject("item", item);

			mav.addObject("total", total);

			// 画面に出力するViewを指定
			mav.setViewName("view/buyConfirm");

			// ModelとView情報を返す
			return mav;

		}
	}

	/**
	 * 「/deleteOrder」へアクセスがあった場合
	 * 対象商品を削除
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/deleteOrder")
	public ModelAndView deleteOrder(HttpServletRequest request, ModelAndView mav) {

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
		Optional<Order> optionalOrder = orderinfo.findById(Integer.parseInt(request.getParameter("orderid")));

		// エラーチェック
		if (!(optionalOrder.isPresent())) {
			mav.addObject("errorMessage", "削除対象の注文が存在しない為、削除処理は行えませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// isbn入力パラメータを取得し、対象の情報を削除
		orderinfo.deleteById(Integer.parseInt(request.getParameter("orderid")));

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/listOrder");

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
