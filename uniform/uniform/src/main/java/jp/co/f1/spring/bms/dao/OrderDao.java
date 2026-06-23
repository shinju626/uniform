package jp.co.f1.spring.bms.dao;

import java.util.ArrayList;
import java.util.Date;

import jakarta.persistence.EntityManager;
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

}
