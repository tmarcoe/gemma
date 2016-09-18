package com.peachy.web.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
@Component("InvoiceItemDao")
public class InvoiceItemDao {

	@Autowired
	private InventoryDao inventoryDao;

	@Autowired
	private SessionFactory sessionFactory;

	public Session session() {
		return sessionFactory.getCurrentSession();
	}

	public InvoiceItem addLineItem(InvoiceItem item) {
		String hql = "from InvoiceItem where invoiceNum = :invoiceNum and skuNum = :skuNum";

		InvoiceItem oldItem = (InvoiceItem) session().createQuery(hql)
				.setInteger("invoiceNum", item.getInvoiceNum())
				.setString("skuNum", item.getSkuNum()).uniqueResult();
		if (oldItem == null) {
			session().save(item);
		} else {
			String hqlUpdate = "update InvoiceItem set amount = :amount where invoiceNum = :invoiceNum and skuNum = :skuNum";
			item.setAmount(oldItem.getAmount() + item.getAmount());

			session().createQuery(hqlUpdate)
					.setInteger("amount", item.getAmount())
					.setInteger("invoiceNum", item.getInvoiceNum())
					.setString("skuNum", item.getSkuNum()).executeUpdate();
		}
		session().disconnect();

		return item;
	}

	@SuppressWarnings("unchecked")
	public List<InvoiceItem> getInvoice(InvoiceHeader header) {
		String hql = "from InvoiceItem where invoiceNum = :invoiceNum";
		List<InvoiceItem> itmList = (List<InvoiceItem>) session()
				.createQuery(hql)
				.setInteger("invoiceNum", header.getInvoiceNum()).list();
		session().disconnect();
		
		return itmList;

	}

	@Transactional
	public void deleteInvoiceItem(int invoiceNum, String skuNum) {
		String hqlDeleteItem = "delete from InvoiceItem where invoiceNum = :invoiceNum and skuNum = :skuNum";
		String hqlExistsItem = "from InvoiceItem where invoiceNum = :invoiceNum";
		String hqlDeleteHeader = "delete from InvoiceHeader where invoiceNum = :invoiceNum";
		session().createSQLQuery(hqlDeleteItem)
				.setInteger("invoiceNum", invoiceNum)
				.setString("skuNum", skuNum).executeUpdate();
		@SuppressWarnings("unchecked")
		List<InvoiceHeader> qry = session().createQuery(hqlExistsItem)
				.setInteger("invoiceNum", invoiceNum).list();
		if (qry.size() == 0) {
			session().createQuery(hqlDeleteHeader)
					.setInteger("invoiceNum", invoiceNum).executeUpdate();
		}
		session().disconnect();
		
	}

	public InvoiceItem getInvoiceItem(int invoiceNum, String skuNum) {
		String hql = "FROM InvoiceItem WHERE invoiceNum = :invoiceNum AND skuNum = :skuNum";
		InvoiceItem itm = (InvoiceItem) session().createQuery(hql)
				.setInteger("invoiceNum", invoiceNum)
				.setString("skuNum", skuNum).uniqueResult();
		session().disconnect();
		
		return itm;
	}

	public void updateItem(InvoiceItem item) {
		session().update(item);
		session().disconnect();
		
	}

	public double totalShoppingCart(InvoiceHeader header) {

		double total = 0;
		double tax = 0;

		List<InvoiceItem> invoiceItems = getInvoice(header);
		for (InvoiceItem item : invoiceItems) {
			Inventory product = inventoryDao.getItem(item.getSkuNum());
			total += item.getPrice() * item.getAmount();
			tax += product.getTaxAmt() * item.getAmount();

		}
		session().disconnect();
		
		return total + tax;
	}

	public void deleteInvoice(InvoiceHeader header) {
		String deleteInvoice = "DELETE FROM InvoiceItem where invoiceNum = :invoiceNum";
		String deleteHeader = "DELETE FROM InvoiceHeader where invoiceNum = :invoiceNum";

		session().beginTransaction();
		session().createQuery(deleteInvoice)
				.setInteger("invoiceNum", header.getInvoiceNum())
				.executeUpdate();
		session().createQuery(deleteHeader)
				.setInteger("invoiceNum", header.getInvoiceNum())
				.executeUpdate();
		session().getTransaction().commit();
		session().disconnect();

	}

	public boolean hasCoupons(int invoiceNum) {
		String hql = "SELECT count(*) FROM InvoiceItem WHERE invoiceNum = :invoiceNum AND skuNum LIKE 'CPN%' ";
		Long result = (Long) session().createQuery(hql)
				.setLong("invoiceNum", invoiceNum).uniqueResult();
		session().disconnect();

		return (result > 0);
	}

	public InvoiceItem getCouponFromInvoice(int invoiceNum) {
		String hql = "FROM InvoiceItem WHERE invoiceNum = :invoiceNum AND skuNum LIKE 'CPN%' ";
		InvoiceItem itm = (InvoiceItem) session().createQuery(hql)
				.setInteger("invoiceNum", invoiceNum).uniqueResult();
		session().disconnect();
		
		return itm;
	}
}