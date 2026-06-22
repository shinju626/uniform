package jp.co.f1.spring.bms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.f1.spring.bms.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}
