package jp.co.f1.spring.bms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import jp.co.f1.spring.bms.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	public Optional<User> findByEmailAndPassword(String email, String password);

	public Optional<User> findByUserid(int userid);
}
