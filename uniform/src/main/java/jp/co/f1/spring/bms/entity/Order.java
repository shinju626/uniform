package jp.co.f1.spring.bms.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "orders")
public class Order {
	// 注文ID
	@Id
	@Column(length = 11)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int orderid;

	public void setOrderid(int orderid) {
		this.orderid = orderid;
	}

	public int getOrderid() {
		return orderid;
	}
	
	// 購入日時
	private Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	// 入金情報判定
	@Column(length = 1)
	private int ispaid;

	public void setIspaid(int ispaid) {
		this.ispaid = ispaid;
	}

	public int getIspaid() {
		return ispaid;
	}

	// 発送状況判定
	@Column(length = 1)
	private int isshipped;

	public void setIsshipped(int isshipped) {
		this.isshipped = isshipped;
	}

	public int getIsshipped() {
		return isshipped;
	}
	
	// 備考
	@Column(length = 200)
	private String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	//ユーザーID
	@Column(length = 11)
	private int userid;

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public int getUserid() {
		return userid;
	}
	
	// 商品ID
	@Column(length = 11)
	private int itemid;

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public int getItemid() {
		return itemid;
	}
	
	// 数量
	@Column(length = 11)
	@NotNull(message = "数量を入力してください")
	private int quantity;

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getQuantity() {
		return quantity;
	}
	
	// 合計金額
	@Column(length = 11)
	private int total;

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTotal() {
		return total;
	}

	// Itemオブジェクト
	@ManyToOne
	@JoinColumn(name = "itemid", referencedColumnName = "itemid", insertable = false, updatable = false)
	private Item item;

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	// Userオブジェクト
	@ManyToOne
	@JoinColumn(name = "userid", referencedColumnName = "userid", insertable = false, updatable = false)
	private User user;

	public void setUserS(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}
	
}
