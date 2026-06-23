package jp.co.f1.spring.bms.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import jp.co.f1.spring.bms.dao.OrderDao;
import jp.co.f1.spring.bms.entity.User;
import jp.co.f1.spring.bms.entity.Item;
import jp.co.f1.spring.bms.entity.Order;
import jp.co.f1.spring.bms.repository.ItemRepository;
import jp.co.f1.spring.bms.repository.OrderRepository;
import jp.co.f1.spring.bms.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDate;

@Controller
public class OrderController {

	// Repositoryインターフェースを自動インスタンス化
	@Autowired
	private ItemRepository iteminfo;
	@Autowired
	private OrderRepository orderinfo;
	@Autowired
	private UserRepository userinfo;
	@Autowired
	private MailSender mailSender;

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
			mav.addObject("cmd", "listOrder");
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

		// orderidでorderを検索
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

		if (request.getParameter("ispaid").equals("2")) {
			String insertMessage = optionalOrder.get().getUser().getName() + "様" + "\n" + "\n" + "商品の代金のご入金が確認できました。"
					+ "\n"
					+ "発送まで今しばらくお待ちください。" + "\n" + "ご不明点等ございましたらご連絡ください。" + "\n"+ "\n";

			insertMessage += "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆" + "\n"
					+ "株式会社神田ユニフォーム" + "\n"
					+ "mail：test.sender@kanda-it-school-system.com" + "\n"
					+ "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆";

			try { // メール送信
				SimpleMailMessage msg = new SimpleMailMessage();
				// 送信元
				msg.setFrom("test.sender@kanda-it-school-system.com");
				// 送信先 optionalOrder.get().getUser().getEmail()変更
				msg.setTo("t_shinju@rg8.so-net.ne.jp");
				// 件名
				msg.setSubject("入金完了");
				// 本文
				msg.setText(insertMessage);

				mailSender.send(msg);

			} catch (MailSendException e) { // メール送信エラー
				mav.addObject("errorMessage", "メールの送信ができませんでした。");
				mav.addObject("cmd", "logout");
				mav.addObject("next", "[ログイン画面へ戻る]");
				mav.setViewName("view/error");
			}
		}

		if (request.getParameter("isshipped").equals("2")) {
			String insertMessage = optionalOrder.get().getUser().getName() + "様" + "\n" + "\n" + "商品の	発送が完了しました"
					+ "\n"
					+ "商品の到着をお待ちください。" + "\n" + "ご不明点等ございましたらご連絡ください。" + "\n";

			insertMessage += "\n" + "またのご利用をお待ちしております。" + "\n" + "\n";

			insertMessage += "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆" + "\n"
					+ "株式会社神田ユニフォーム" + "\n"
					+ "mail：test.sender@kanda-it-school-system.com" + "\n"
					+ "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆";

			try { // メール送信
				SimpleMailMessage msg = new SimpleMailMessage();
				// 送信元
				msg.setFrom("test.sender@kanda-it-school-system.com");
				// 送信先 user.getEmailに()変更
				msg.setTo("t_shinju@rg8.so-net.ne.jp");
				// 件名
				msg.setSubject("発送完了");
				// 本文
				msg.setText(insertMessage);

				mailSender.send(msg);

			} catch (MailSendException e) { // メール送信エラー
				mav.addObject("errorMessage", "メールの送信ができませんでした。");
				mav.addObject("cmd", "logout");
				mav.addObject("next", "[ログイン画面へ戻る]");
				mav.setViewName("view/error");
			}
		}

		// 入金状況、発送状況を更新
		Order order = optionalOrder.get();
		order.setIspaid(Integer.parseInt(request.getParameter("ispaid")));
		order.setIsshipped(Integer.parseInt(request.getParameter("isshipped")));
		orderinfo.saveAndFlush(order);

		// 出品中の商品一覧にリダイレクト
		mav.setViewName("redirect:/listOrder");

		// 表示する画面
		return mav;

	}

	/**
	 * 「/listOrder」へGET送信された場合
	 * @param mav
	 * @param order
	 * @return 受注状況一覧の表示
	 */
	@GetMapping("/listOrder")
	public ModelAndView listOrder(ModelAndView mav, Order order) {

		// DBから全件取得
		Iterable<Order> allOrders = orderDao.sort(order.getDate());

		LocalDate today = LocalDate.now();
		
//		today.getMonth();
//		today.getMonthValue();
		
		//発送済み
		ArrayList<Order> ordermonth_now2 = orderDao.findByMonth2(String.valueOf(today.getYear()),String.valueOf(today.getMonthValue()));
		ArrayList<Order> ordermonth_before2 = orderDao.findByMonth2(String.valueOf(today.getYear()),String.valueOf(today.getMonthValue() -1));
		
		int nowTotal2 = 0;
		int beforeTotal2 = 0;
		
		//取得した合計の計算をする
		for (int i = 0; i < ordermonth_now2.size(); i++) {
			//※ここでOrderの情報を取り出し、金額を取り出せるようにしておく
			Order order_now2 = ordermonth_now2.get(i);
			nowTotal2 += order_now2.getTotal();
		}
		
		//取得した合計の計算をする
		for (int i = 0; i < ordermonth_before2.size(); i++) {
			//※ここでOrderの情報を取り出し、金額を取り出せるようにしておく
			Order order_before2 = ordermonth_before2.get(i);
			beforeTotal2 += order_before2.getTotal();
		}
		
		mav.addObject("nowTotal2", nowTotal2);
		mav.addObject("beforeTotal2", beforeTotal2);
		
		//予定
		ArrayList<Order> ordermonth_now = orderDao.findByMonth(String.valueOf(today.getYear()),String.valueOf(today.getMonthValue()));
		ArrayList<Order> ordermonth_before = orderDao.findByMonth(String.valueOf(today.getYear()),String.valueOf(today.getMonthValue() - 1));
		
		int nowTotal = 0;
		int beforeTotal = 0;
		
		//取得した合計の計算をする
		for (int i = 0; i < ordermonth_now.size(); i++) {
			//※ここでOrderの情報を取り出し、金額を取り出せるようにしておく
			Order order_now = ordermonth_now.get(i);
			nowTotal += order_now.getTotal();
		}
		
		//取得した合計の計算をする
		for (int i = 0; i < ordermonth_before.size(); i++) {
			//※ここでOrderの情報を取り出し、金額を取り出せるようにしておく
			Order order_before = ordermonth_before.get(i);
			beforeTotal += order_before.getTotal();
		}
		
		mav.addObject("nowTotal", nowTotal);
		mav.addObject("beforeTotal", beforeTotal);
		
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
		Item item = optionalItem.get();

		//在庫数を超えるため購入
		if (Integer.parseInt(request.getParameter("quantity")) > item.getStock()) {
			//エラーメッセージ
			mav.addObject("errorMessage", "在庫数を超えるため購入できません。");
			mav.addObject("cmd", "listItem");
			mav.addObject("next", "[商品一覧画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;

		}

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
		mav.addObject("item", item);

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
		Item item = optionalItem.get();

		//在庫数を超えるため購入
		if (Integer.parseInt(request.getParameter("quantity")) > item.getStock()) {
			//エラーメッセージ
			mav.addObject("errorMessage", "在庫数を超えるため購入できません。");
			mav.addObject("cmd", "itemList");
			mav.addObject("next", "[商品一覧画面へ]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;

		}

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
		mav.addObject("item", item);
		User user = new User();
		user.setAuthority(3);
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
	 * @return 購入後画面を表示、メールを送信
	 */
	@GetMapping("/buyConfirm")
	public ModelAndView buyConfirm(HttpServletRequest request, ModelAndView mav) {

		// ユーザーとオーダーを新たに宣言
		User user = new User();
		Order order = new Order();

		// itemidを基にアイテムの詳細情報を取得
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));
		Item item = optionalItem.get();

		// 購入した分を在庫から減らす
		item.setStock(item.getStock() - Integer.parseInt(request.getParameter("quantity")));
		iteminfo.saveAndFlush(item);

		if (request.getParameter("authority").equals("3")) { // 非会員の場合

			// 入力内容を非会員としてusersに登録
			user.setPassword("非会員");
			user.setEmail(request.getParameter("email"));
			user.setName(request.getParameter("name"));
			user.setAddress(request.getParameter("address"));
			user.setAuthority(3);
			userinfo.saveAndFlush(user);

		} else { // 会員の場合
			//セッションからUserの値を取得する
			user = (User) session.getAttribute("user");

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

		}

		// orderに登録
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

		String insertMessage = user.getName() + "様" + "\n" + "\n" + "ユニフォームのご購入ありがとうございます。" + "\n"
				+ "以下内容で注文を受け付けましたので、ご連絡いたします。" + "\n";

		insertMessage += optionalItem.get().getItemname() + "　" + order.getQuantity() + "枚　" + order.getTotal() + "円"
				+ "\n" + "\n";

		insertMessage += "商品の代金の振込先はこちらになります。" + "\n";

		insertMessage += "三菱UFJ銀行　神田支店(123)　普通預金" + "\n" + "4567890" + "\n" + "\n";

		insertMessage += "ご入金が確認でき次第、商品を発送いたします。" + "\n" + "ご不明点などございましたら以下へご連絡ください。" + "\n" +
				"この度はご利用いただきありがとうございました。" + "\n" + "またのご利用お待ちしております。" + "\n" + "\n";

		insertMessage += "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆" + "\n"
				+ "株式会社神田ユニフォーム" + "\n"
				+ "mail：test.sender@kanda-it-school-system.com" + "\n"
				+ "☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆";

		try { // メール送信
			SimpleMailMessage msg = new SimpleMailMessage();
			// 送信元
			msg.setFrom("test.sender@kanda-it-school-system.com");
			// 送信先 user.getEmailに()変更
			msg.setTo("t_shinju@rg8.so-net.ne.jp");
			// 件名
			msg.setSubject("ユニフォーム購入完了");
			// 本文
			msg.setText(insertMessage);

			mailSender.send(msg);

		} catch (MailSendException e) { // メール送信エラー
			mav.addObject("errorMessage", "メールの送信ができませんでした。");
			mav.addObject("cmd", "logout");
			mav.addObject("next", "[ログイン画面へ戻る]");
			mav.setViewName("view/error");
		}

		//  Viewに渡す変数をModelに格納
		mav.addObject("user", user);
		mav.addObject("order", order);
		mav.addObject("item", item);
		mav.addObject("total", total);

		// 画面に出力するViewを指定
		mav.setViewName("view/buyConfirm");

		// ModelとView情報を返す
		return mav;
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
