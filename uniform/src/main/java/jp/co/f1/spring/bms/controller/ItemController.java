package jp.co.f1.spring.bms.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import jp.co.f1.spring.bms.dao.ItemDao;
import jp.co.f1.spring.bms.entity.Item;
import jp.co.f1.spring.bms.entity.User;
import jp.co.f1.spring.bms.repository.ItemRepository;
import jp.co.f1.spring.bms.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ItemController {

	// Repositoryインターフェースを自動インスタンス化
	@Autowired
	private ItemRepository iteminfo;

	@Autowired
	private UserRepository userinfo;

	// EntityManager自動インスタンス化
	@PersistenceContext
	private EntityManager entityManager;

	// DAO自動インスタンス化
	@Autowired
	private ItemDao itemDao;

	// セッションを使うためセッションオブジェクトを生成する
	@Autowired
	private HttpSession session;

	/**
	 * 「/changeItem」へGet送信された場合
	 */
	@GetMapping(value = "/changeItem")
	public ModelAndView update(@ModelAttribute Item item, HttpServletRequest request, ModelAndView mav) {

		//セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		//セッション切れの場合
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "ログイン画面へ戻る");
			mav.setViewName("view/error");
			return mav;
		}

		//値を取得
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));

		if (!optionalItem.isPresent()) {
			// エラーメッセージ
			mav.addObject("errorMessage", "更新対象の商品が存在しない為、変更画面は表示出来ませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			// 画面に出力するViewを指定
			mav.setViewName("view/error");
			// ModelとView情報を返す
			return mav;
		}

		//Viewに渡す変数をModelに格納
		Item old_item = optionalItem.get();
		mav.addObject("old_item", old_item);
		mav.addObject("item", item);

		//画面に出力するViewを指定
		mav.setViewName("view/changeItem");

		//ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/changeItem」へpost送信された場合
	 */
	@PostMapping(value = "/changeItem")
	public ModelAndView update(@ModelAttribute @Validated Item item, BindingResult result,
			HttpServletRequest request, ModelAndView mav) {

		//セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		//セッション切れの場合
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "ログイン画面へ戻る");
			mav.setViewName("view/error");
			return mav;
		}
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));
		Item old_item = optionalItem.get(); // Optionalから値を取得

		// 書籍が存在しない場合
		if (!optionalItem.isPresent()) {
			mav.addObject("errorMessage", "変更対象の商品が存在しないため、商品変更処理は行えませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}
		// 入力内容にエラーがある場合
		if (result.hasErrors()) {
			//エラーメッセージ
			mav.addObject("message", "入力内容に誤りがあります");
			// バリデーションエラー後は、入力内容を再表示する
			mav.addObject("old_book", old_item);
			mav.setViewName("view/changeItem");
			return mav;
		}
		//書籍の検索

		//Viewに渡す変数をModelに格納
		iteminfo.saveAndFlush(item);

		//リダイレクト先を指定
		mav = new ModelAndView("redirect:/listItem");

		//ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/insertItem」へアクセスがあった場合
	 */
	@GetMapping("/insertItem")
	public ModelAndView insert(@ModelAttribute Item item, ModelAndView mav) {

		//セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		//セッション切れの場合
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "ログイン画面へ戻る");
			mav.setViewName("view/error");
			return mav;
		}

		//画面に出力するViewを指定
		mav.setViewName("view/insertItem");

		//Viewに渡す変数をModelに格納
		mav.addObject("item", item);

		//ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/insertItem」へPOST送信された場合
	 */
	@PostMapping(value = "/insertItem")
	//POSTデータをItemインスタンスとして受け取る
	public ModelAndView insertPost(HttpSession session, @ModelAttribute @Validated Item item,
			BindingResult result, @RequestParam(name = "realimage") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response, ModelAndView mav) {

		//セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		//セッション切れの場合
		if (user == null) {
			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
			mav.addObject("cmd", "login");
			mav.addObject("next", "ログイン画面へ戻る");
			mav.setViewName("view/error");
			return mav;
		}

		//検索
		Optional<Item> optionalItem = iteminfo.findByItemid(item.getItemid());

		try {
			// ファイル名（別名をつけても良い）
			String filename = file.getOriginalFilename();
			if (filename.matches(".*(.png|.jpeg|.jpg).*")) {
				// 保存先パス
				String filePath = "src/main/resources/static/image/" + filename;

				if (file.getSize() > 2097152) {
					mav.addObject("message", "アップロード出来るのは2MB未満のファイルのみです");
					mav.addObject("user", user);
					mav.setViewName("view/itemInsert");
					return mav;
				}

				// ファイルをバイナリデータとして取得
				byte[] content = file.getBytes();
				// 保存
				Files.write(Paths.get(filePath), content);
				item.setItemphoto(filename);
			} else if (filename.equals("")) {
				// 画像が空欄の場合は何もせずに下へ
			} else {
				mav.addObject("message", "アップロード出来るのはpngまたはjpeg、jpg形式のみです");
				mav.addObject("user", user);
				mav.setViewName("view/itemInsert");
				return mav;
			}
		} catch (Exception e) {
			// エラー時
			e.printStackTrace();
		}
		// 空欄時
		if (!(result.hasErrors())) {
			mav.addObject("message", "入力内容に空欄があります");
			mav.addObject("user", user);
			mav.setViewName("view/itemInsert");
			return mav;

		}


		//入力されたデータをDBに保存
		iteminfo.saveAndFlush(item);

		//リダイレクト先を指定
		mav = new ModelAndView("redirect:/listItem");

		//ModelとView情報を返す
		return mav;
	}

	/**
	 * 「/deleteItem」へアクセスがあった場合
	 * 対象商品を削除
	 * @param request
	 * @param mav
	 * @return
	 */
	@GetMapping("/deleteItem")
	public ModelAndView deleteItem(HttpServletRequest request, ModelAndView mav) {

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
		Optional<Item> optionalItem = iteminfo.findById(Integer.parseInt(request.getParameter("itemid")));

		// エラーチェック
		if (!(optionalItem.isPresent())) {
			mav.addObject("errorMessage", "削除対象の商品が存在しない為、削除処理は行えませんでした。");
			mav.addObject("cmd", "list");
			mav.addObject("next", "[一覧表示へ戻る]");
			mav.setViewName("view/error");
			return mav;
		}

		// isbn入力パラメータを取得し、対象の情報を削除
		iteminfo.deleteById(Integer.parseInt(request.getParameter("itemid")));

		// リダイレクト先を指定
		mav = new ModelAndView("redirect:/listItem");

		// ModelとView情報を返す
		return mav;
	}

	//商品詳細「/detailItem」にアクセスがあった場合
	@GetMapping("/detailItem")
	public ModelAndView detail(HttpServletRequest request, ModelAndView mav) {

		//パラメータで取得したitemidを基に、各情報を取得する
		Optional<Item> optionalItem = iteminfo.findByItemid(Integer.parseInt(request.getParameter("itemid")));

		//データが存在しない（空である）場合
		if (!(optionalItem.isPresent())) {
			mav.addObject("errorMessage", "表示対象の商品が存在しない為、詳細情報は表示出来ませんでした。");

			mav.addObject("cmd", "list");

			mav.addObject("next", "[一覧表示へ戻る]");

			mav.setViewName("view/error");

			return mav;
		}

		//セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		//getメソッドを用いてModelに格納
		mav.addObject("item", optionalItem.get());

		mav.addObject("user", user);

		//画面に出力するViewを指定
		mav.setViewName("view/detailItem");

		//ModelとView情報を返す
		return mav;

	}

	/**
	 * 「/listItem」へGET送信された場合
	 * @param mav
	 * @return 商品一覧
	 */
	@GetMapping("/listItem")
	public ModelAndView listItem(ModelAndView mav) {

		// bookinfoテーブルから全件取得
		Iterable<Item> item_list = iteminfo.findAll();

		// セッションからUserの値を取得する
		User user = (User) session.getAttribute("user");

		// セッションタイムアウト
//		if (user == null) {
//			mav.addObject("errorMessage", "セッション切れの為、再度ログインしてください。");
//			mav.addObject("cmd", "login");
//			mav.addObject("next", "[ログイン画面へ戻る]");
//			mav.setViewName("view/error");
//			return mav;
//		}

		//  Viewに渡す変数をModelに格納
		mav.addObject("item_list", item_list);

		// 画面に出力するViewを指定
		mav.setViewName("view/listItem");

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
