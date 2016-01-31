package com.gemma.spring.web.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
@Component("InvoiceHeaderDao")
public class InvoiceHeaderDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public Session session() {
		return sessionFactory.getCurrentSession();
	}

	public InvoiceHeader getOpenOrder(int userID) {
		String hql = "from InvoiceHeader where userID = :userID and processed = null";
		
		session().createQuery(hql).setInteger("userID", userID).uniqueResult();
		
		return (InvoiceHeader) session().createQuery(hql).setInteger("userID", userID).uniqueResult();
	}

	public InvoiceHeader createHeader(InvoiceHeader header) {
		session().save(header);
		
		return header;
	}

	public InvoiceHeader getInvoiceHeader(int invoiceNum) {
		String hql = "from InvoiceHeader where invoiceNum = :invoiceNum";
		
		
		return (InvoiceHeader) session().createQuery(hql).setInteger("invoiceNum", invoiceNum).uniqueResult();
	}

	public void updateHeader(InvoiceHeader header) {
		session().update(header);
	}

}