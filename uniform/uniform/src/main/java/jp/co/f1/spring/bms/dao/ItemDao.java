package jp.co.f1.spring.bms.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import jp.co.f1.spring.bms.entity.Item;

import org.springframework.stereotype.Repository;

@Repository
public class ItemDao {
	
	// エンティティマネージャー
	private EntityManager entityManager;

	// クエリ生成用インスタンス
	private CriteriaBuilder builder;

	// クエリ実行用インスタンス
	private CriteriaQuery<Item> query;

	// 検索されるエンティティのルート
	private Root<Item> root;

	//DB接続準備
	public ItemDao(EntityManager entityManager) {
		// EntityManager取得
		this.entityManager = entityManager;
		// クエリ生成用インスタンス
		builder = entityManager.getCriteriaBuilder();
		// クエリ実行用インスタンス
		query = builder.createQuery(Item.class);
		// 検索されるエンティティのルート
		root = query.from(Item.class);
	}
}
