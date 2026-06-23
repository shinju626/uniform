package jp.co.f1.spring.bms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "items")
public class Item {
	// 商品ID
	@Id
	@Column(length = 11)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int itemid;

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public int getItemid() {
		return itemid;
	}

	// 商品名
	@Column(length = 100)
	@NotEmpty(message = "商品名を入力してください")
	private String itemname;

	public void setItemname(String itemname) {
		this.itemname = itemname;
	}

	public String getItemname() {
		return itemname;
	}

	// 画像名
	@Column(length = 100)
	private String itemphoto;

	public void setItemphoto(String itemphoto) {
		this.itemphoto = itemphoto;
	}

	public String getItemphoto() {
		return itemphoto;
	}

	// 在庫数
	@Column(length = 11)
	@NotNull(message = "数量を入力してください")
	private int stock;

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getStock() {
		return stock;
	}

	// 価格
	@Column(length = 11)
	@NotNull(message = "価格を入力してください")
	private int price;
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public int getPrice() {
		return price;
	}

}
