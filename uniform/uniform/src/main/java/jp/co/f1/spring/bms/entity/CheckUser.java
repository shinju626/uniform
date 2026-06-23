package jp.co.f1.spring.bms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
public class CheckUser {

	// 名前
	@Column(length = 20)
	@NotEmpty(message = "名前を入力してください。")
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// メールアドレス
	@Id
	@Column(length = 100)
	@NotEmpty(message = "メールアドレスを入力してください。")
	private String email;

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	// 住所
	@Column(length = 200)
	@NotEmpty(message = "住所を入力してください。")
	private String address;

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	// 旧パスワード
	@Column(length = 100)
	@NotEmpty(message = "旧パスワードを入力してください")
	private String oldPassword;

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	// パスワード
	@Column(length = 20)
	private String password;

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	// パスワード(確認用)
	@Column(length = 20)
	@NotEmpty(message = "確認用のパスワードを入力してください。")
	private String confirmPassword;

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	// 権限
	@Column(length = 11)
	@NotNull(message = "権限を選択してください。")
	private int authority;

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	public int getAuthority() {
		return authority;
	}
}
