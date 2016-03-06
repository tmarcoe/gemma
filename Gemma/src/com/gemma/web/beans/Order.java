package com.gemma.web.beans;

import com.gemma.web.dao.Inventory;

public class Order {
	private Inventory inventory;
	private int amount;
	private double price;
	private double tax;

	public Order(Inventory inventory) {
		this.inventory = inventory;
	}
	public Order() {

	}

	public Inventory getInventory() {
		return inventory;
	}
	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getTax() {
		return tax;
	}
	public void setTax(double tax) {
		this.tax = tax;
	}
	
	

}