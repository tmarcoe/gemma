/**
 * 
 */
package com.gemma.spring.web.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Timothy Marcoe
 *
 */
@Entity
public class Inventory {
	@Id
	@NotBlank
	private String skuNum;
	@Size(min=2, max=255)
	private String productName;
	private String category;
	private String subCategory;
	private int amtInStock;
	private int amtCommitted;
	private float salePrice;
	private float discountPrice;
	private float taxAmt;
	private boolean onSale;
	private String image;
	@Size(min=10, max=1000)
	private String description;
	
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSkuNum() {
		return skuNum;
	}
	public void setSkuNum(String skuNum) {
		this.skuNum = skuNum;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public int getAmtInStock() {
		return amtInStock;
	}
	public void setAmtInStock(int amtInStock) {
		this.amtInStock = amtInStock;
	}
	public int getAmtCommitted() {
		return amtCommitted;
	}
	public void setAmtCommitted(int amtCommitted) {
		this.amtCommitted = amtCommitted;
	}
	public float getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(float salePrice) {
		this.salePrice = salePrice;
	}
	public float getDiscountPrice() {
		return discountPrice;
	}
	public void setDiscountPrice(float discountPrice) {
		this.discountPrice = discountPrice;
	}
	public float getTaxAmt() {
		return taxAmt;
	}
	public void setTaxAmt(float taxAmt) {
		this.taxAmt = taxAmt;
	}
	public boolean isOnSale() {
		return onSale;
	}
	public void setOnSale(boolean onSale) {
		this.onSale = onSale;
	}
	
	
		
}
