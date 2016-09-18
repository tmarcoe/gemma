package com.peachy.web.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.peachy.web.beans.Audit;
import com.peachy.web.beans.DatePicker;

@Transactional
@Repository
@Component("GeneralLedgerDao")
public class GeneralLedgerDao {

	@Autowired
	private SessionFactory sessionFactory;

	public Session session() {
		return sessionFactory.getCurrentSession();
	}

	public void addEntry(GeneralLedger ledger) {
		Date today = DateUtils.truncate(new Date(), Calendar.DATE);

		ledger.setEntryDate(today);
		session().save(ledger);
		session().disconnect();
	}

	@SuppressWarnings("unchecked")
	public List<GeneralLedger> getList() {
		String hql = "FROM GeneralLedger";
		List<GeneralLedger> gl = session().createQuery(hql).list();
		session().disconnect();

		return gl;
	}

	@SuppressWarnings("unchecked")
	public List<GeneralLedger> getList(DatePicker picker) {
		String hql = "FROM GeneralLedger AS g where g.entryDate BETWEEN :start AND :end";
		List<GeneralLedger> gl = session().createQuery(hql)
				.setParameter("start", picker.getStart())
				.setParameter("end", picker.getEnd()).list();
		session().disconnect();

		return gl;
	}

	public Audit getAudit() {
		Audit audit = new Audit();
		Double debit = 0.0;
		Double credit = 0.0;

		for (GeneralLedger item : getList()) {
			debit += item.getDebitAmt();
			credit += item.getCreditAmt();
		}
		audit.setCreditColumn(credit);
		audit.setDebitColumn(debit);

		return audit;
	}

}