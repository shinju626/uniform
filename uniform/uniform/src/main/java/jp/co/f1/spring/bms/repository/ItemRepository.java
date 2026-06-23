package jp.co.f1.spring.bms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.f1.spring.bms.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Integer> {
	
	public Optional<Item> findByItemid(int itemid);
	
	public Optional<Item> findByItemname(String itemname);

}