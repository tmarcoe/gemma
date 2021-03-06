package com.gemma.spring.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gemma.spring.web.dao.Inventory;
import com.gemma.spring.web.dao.InventoryContainer;
import com.gemma.spring.web.dao.InventoryDao;

@Service("inventoryService")
public class InventoryService {

	@Autowired
	private InventoryDao inventoryDao;
	
	public List<Inventory> listProducts() {
		return inventoryDao.listProducts();
	}
	
	public List<Inventory> listProducts(String category) {
		return inventoryDao.listProducts(category);
	}
	
	public List<Inventory> listProducts(String category, String subCategory) {
		return inventoryDao.listProducts(category, subCategory);
	}
	
	public List<Inventory> listSaleItems() {
		return inventoryDao.listSaleItems();
	}
	public void create(Inventory inventory) {
		inventoryDao.create(inventory);
	}
	
	public void saveOrUpdate(List<Inventory> inventory) {
		inventoryDao.saveOrUpdate(inventory);
	}
	
	public void update(Inventory inventory) {
		inventoryDao.update(inventory);
	}
	
	public boolean delete(String skuNum){
		return inventoryDao.delete(skuNum);
	}
	
	public Inventory getItem(String skuNum) {
		return inventoryDao.getItem(skuNum);
	}
	
	public InventoryContainer getContainer(){
		InventoryContainer container = inventoryDao.getContainer();
		
		return container;
	}
}
