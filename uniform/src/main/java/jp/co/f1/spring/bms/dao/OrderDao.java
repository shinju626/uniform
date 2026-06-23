package jp.co.f1.spring.bms.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import jp.co.f1.spring.bms.entity.Order;

@Repository
public class OrderDao {

	// エンティティマネージャー
	private EntityManager entityManager;

	// クエリ生成用インスタンス
	private CriteriaBuilder builder;

	// クエリ実行用インスタンス
	private CriteriaQuery<Order> query;

	// 検索されるエンティティのルート
	private Root<Order> root;

	//DB接続準備
	public OrderDao(EntityManager entityManager) {
		// EntityManager取得
		this.entityManager = entityManager;
		// クエリ生成用インスタンス
		builder = entityManager.getCriteriaBuilder();
		// クエリ実行用インスタンス
		query = builder.createQuery(Order.class);
		// 検索されるエンティティのルート
		root = query.from(Order.class);

	}
	
	public ArrayList<Order> sort(Date date) {
		// SELECT句設定
		query.select(root);

		// WHERE句設定
		query.orderBy(builder.desc(root.get("date")));

		// クエリ実行
		return (ArrayList<Order>) entityManager.createQuery(query).getResultList();

	}
	
	/**
	 * 年、月の売り上げ状況の情報検索
	 */
	public ArrayList<Order> findByMonth(String year, String month) {
		
		//　【発行したいクエリをまず確認する！】
		//		SELECT b.isbn, title, price, sum(quantity) as quantity
		//		FROM orderinfo o inner join bookinfo b
		//		ON o.isbn=b.isbn
		//		WHERE date LIKE '2025-06%'
		//		GROUP BY b.isbn
		//		ORDER BY b.isbn;

	    // JPQL文のベースを作成（SELECT句とJOIN句）
		
		// ※JPQLでは、テーブル名ではなくEntity名を使う（Order,Book）
		// orderinfoのisbnカラムがすでにbookinfoテーブルと外部キーで紐づいていて
		//　この関係性をentity内でも明記しているため (@ManyToOne　@JoinColumn)
		// JOIN ONがなくても「o.book」でJOINされる
	    StringBuilder jpql = new StringBuilder(
	        "SELECT total " +
	        "FROM Order "
	    );

	    // WHERE句に追加する条件を格納するリストを作成
	    ArrayList<String> conditions = new ArrayList<>();

	    
	    // ========================== 解説 ==========================
	    // FUNCTION('DATE_FORMAT', o.date, '%Y-%m')
	    // → o.date（日付型）を 'yyyy-MM' の形式（例：2024-06）で文字列に変換する
	    //
	    // CAST(... AS string)
	    // → DATE_FORMATの結果は「Object型」として認識される可能性があるため、
	    //    明示的に「文字列型（String）」として扱うように変換（キャスト）している
	    //
	    // LIKE :datePrefix
	    // → :datePrefix という名前のplaceholder（バインド変数）に
	    //    「2024-06%」のような値をバインドし、月単位の曖昧検索（前方一致）を行う
	    //
	    // つまり：o.date が 2024年6月の任意の日付（例：2024-06-01、2024-06-30など）に
	    //         該当する注文だけを抽出する条件
	    // =========================================================
	    
	    // 条件①：年と月の両方が入力された場合 → '2024-06%' のような月単位で検索
	    if (year != null && !year.isEmpty() && month != null && !month.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%Y-%m') AS string) LIKE :datePrefix");
	    }
	    // 条件②：年だけが入力された場合 → '2024' 年の売上すべてを検索
	    else if (year != null && !year.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%Y') AS string) = :yearStr");
	    }
	    // 条件③：月だけが入力された場合 → すべての年に対して、'%-MM' で検索（例: '-06'）
	    else if (month != null && !month.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%m') AS string) = :monthStr");
	    }

	    // WHERE句をJPQLに追加（条件がある場合のみ）
	    if (!conditions.isEmpty()) {
	        jpql.append(" WHERE ");
	        jpql.append(String.join(" AND ", conditions));
	    }

	    // GROUP BY句とORDER BY句を追加（ISBN順に並べ替え）
//	    jpql.append(" GROUP BY b.isbn");
//	    jpql.append(" ORDER BY b.isbn ASC");

	    // 完成したJPQL文を使ってTypedQueryを作成
	    // TypedQueryはpom.xmlに既に記載されている「spring-boot-starter-data-jpa」があれば使える為、validationの追加は不要
	    // import文の追加は必要　➡　import jakarta.persistence.TypedQuery;
	    TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

	    String paddedMonth = month;
	    // パラメータをバインド（実際にクエリに値を挿入）
	    if (year != null && !year.isEmpty() && month != null && !month.isEmpty()) {
	    	
	    	// 「6」と入力しても「06」月の履歴が検索できるように編集する
	        if(month.length() == 1) {
	        	paddedMonth = "0" + month;
	        }
	        String datePrefix = year + "-" + paddedMonth; // 例: '2024-06'
	        query.setParameter("datePrefix", datePrefix + "%"); // LIKE検索用にワイルドカード付き
	        
	    } else if (year != null && !year.isEmpty()) {
	        query.setParameter("yearStr", year); // 年だけの検索
	        
	    } else if (month != null && !month.isEmpty()) {
	    	
	    	// 「6」と入力しても「06」月の履歴が検索できるように編集する
	        if(month.length() == 1) {
	        	paddedMonth = "0" + month;
	        }
	        query.setParameter("monthStr", paddedMonth); // 月だけの検索（2桁）
	    }

	    // クエリを実行して結果を取得（1レコード = Object[]）
	    List<Object[]> result = query.getResultList();

	    // 取得したデータをSalesエンティティに変換してリストに格納
	    ArrayList<Order> orderList = new ArrayList<>();
	    
	    for (Object[] row : result) {
	    	Order order = new Order();
	    		order.setTotal((int) row[0]);
//	        sales.setIsbn((String) row[0]); // ISBN（文字列）
//	        sales.setTitle((String) row[1]); // 書名（文字列）
//	        sales.setPrice(Integer.parseInt((String)row[2])); // 価格（整数）
//	        sales.setQuantity(((Number) row[3]).intValue()); // 数量（int型に変換）※SUMの結果、Longをintに変換
	    		orderList.add(order);
	    }

	    // 最終的な売上リストを返す
	    return orderList;
	}
	
	/**
	 * 年、月の売り上げ状況の情報検索
	 */
	public ArrayList<Order> findByMonth2(String year, String month) {
		
		//　【発行したいクエリをまず確認する！】
		//		SELECT b.isbn, title, price, sum(quantity) as quantity
		//		FROM orderinfo o inner join bookinfo b
		//		ON o.isbn=b.isbn
		//		WHERE date LIKE '2025-06%'
		//		GROUP BY b.isbn
		//		ORDER BY b.isbn;

	    // JPQL文のベースを作成（SELECT句とJOIN句）
		
		// ※JPQLでは、テーブル名ではなくEntity名を使う（Order,Book）
		// orderinfoのisbnカラムがすでにbookinfoテーブルと外部キーで紐づいていて
		//　この関係性をentity内でも明記しているため (@ManyToOne　@JoinColumn)
		// JOIN ONがなくても「o.book」でJOINされる
	    StringBuilder jpql = new StringBuilder(
	        "SELECT total " +
	        "FROM Order "
	    );

	    // WHERE句に追加する条件を格納するリストを作成
	    ArrayList<String> conditions = new ArrayList<>();

	    
	    // ========================== 解説 ==========================
	    // FUNCTION('DATE_FORMAT', o.date, '%Y-%m')
	    // → o.date（日付型）を 'yyyy-MM' の形式（例：2024-06）で文字列に変換する
	    //
	    // CAST(... AS string)
	    // → DATE_FORMATの結果は「Object型」として認識される可能性があるため、
	    //    明示的に「文字列型（String）」として扱うように変換（キャスト）している
	    //
	    // LIKE :datePrefix
	    // → :datePrefix という名前のplaceholder（バインド変数）に
	    //    「2024-06%」のような値をバインドし、月単位の曖昧検索（前方一致）を行う
	    //
	    // つまり：o.date が 2024年6月の任意の日付（例：2024-06-01、2024-06-30など）に
	    //         該当する注文だけを抽出する条件
	    // =========================================================
	    
	    // 条件①：年と月の両方が入力された場合 → '2024-06%' のような月単位で検索
	    if (year != null && !year.isEmpty() && month != null && !month.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%Y-%m') AS string) LIKE :datePrefix");
	    }
	    // 条件②：年だけが入力された場合 → '2024' 年の売上すべてを検索
	    else if (year != null && !year.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%Y') AS string) = :yearStr");
	    }
	    // 条件③：月だけが入力された場合 → すべての年に対して、'%-MM' で検索（例: '-06'）
	    else if (month != null && !month.isEmpty()) {
	        conditions.add("CAST(FUNCTION('DATE_FORMAT', date, '%m') AS string) = :monthStr");
	    }

	    // WHERE句をJPQLに追加（条件がある場合のみ）
	    if (!conditions.isEmpty()) {
	        jpql.append(" WHERE ");
	        jpql.append(" isshipped=2 AND ");
	        jpql.append(String.join(" AND ", conditions));
	    }

	    // GROUP BY句とORDER BY句を追加（ISBN順に並べ替え）
//	    jpql.append(" GROUP BY b.isbn");
//	    jpql.append(" ORDER BY b.isbn ASC");

	    // 完成したJPQL文を使ってTypedQueryを作成
	    // TypedQueryはpom.xmlに既に記載されている「spring-boot-starter-data-jpa」があれば使える為、validationの追加は不要
	    // import文の追加は必要　➡　import jakarta.persistence.TypedQuery;
	    TypedQuery<Object[]> query = entityManager.createQuery(jpql.toString(), Object[].class);

	    String paddedMonth = month;
	    // パラメータをバインド（実際にクエリに値を挿入）
	    if (year != null && !year.isEmpty() && month != null && !month.isEmpty()) {
	    	
	    	// 「6」と入力しても「06」月の履歴が検索できるように編集する
	        if(month.length() == 1) {
	        	paddedMonth = "0" + month;
	        }
	        String datePrefix = year + "-" + paddedMonth; // 例: '2024-06'
	        query.setParameter("datePrefix", datePrefix + "%"); // LIKE検索用にワイルドカード付き
	        
	    } else if (year != null && !year.isEmpty()) {
	        query.setParameter("yearStr", year); // 年だけの検索
	        
	    } else if (month != null && !month.isEmpty()) {
	    	
	    	// 「6」と入力しても「06」月の履歴が検索できるように編集する
	        if(month.length() == 1) {
	        	paddedMonth = "0" + month;
	        }
	        query.setParameter("monthStr", paddedMonth); // 月だけの検索（2桁）
	    }

	    // クエリを実行して結果を取得（1レコード = Object[]）
	    List<Object[]> result = query.getResultList();

	    // 取得したデータをSalesエンティティに変換してリストに格納
	    ArrayList<Order> orderList = new ArrayList<>();
	    
	    for (Object[] row : result) {
	    	Order order = new Order();
	    		order.setTotal((int) row[0]);
//	        sales.setIsbn((String) row[0]); // ISBN（文字列）
//	        sales.setTitle((String) row[1]); // 書名（文字列）
//	        sales.setPrice(Integer.parseInt((String)row[2])); // 価格（整数）
//	        sales.setQuantity(((Number) row[3]).intValue()); // 数量（int型に変換）※SUMの結果、Longをintに変換
	    		orderList.add(order);
	    }

	    // 最終的な売上リストを返す
	    return orderList;
	}


}
