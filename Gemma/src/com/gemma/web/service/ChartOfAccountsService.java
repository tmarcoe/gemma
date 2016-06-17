package com.gemma.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gemma.web.dao.ChartOfAccounts;
import com.gemma.web.dao.ChartOfAccountsContainer;
import com.gemma.web.dao.ChartOfAccountsDao;


@Service("chartOfAccountsService")
public class ChartOfAccountsService {
	
	@Autowired
	private ChartOfAccountsDao chartOfAccountsDao;

	public List<ChartOfAccounts> listAccounts() {
		return chartOfAccountsDao.listAccounts();
	}

	public void create(ChartOfAccounts chartOfAccounts) {
		chartOfAccountsDao.create(chartOfAccounts);
	}

	public void update(List<ChartOfAccounts> accountsList) {
		
		chartOfAccountsDao.update(accountsList);
		
	}
	
	public void update(ChartOfAccounts chartOfAccounts) {
		chartOfAccountsDao.update(chartOfAccounts);
	}

	public boolean delete(String deleteKey) {
		return chartOfAccountsDao.delete(deleteKey);
	}
	
	public ChartOfAccountsContainer getContainer() {
		ChartOfAccountsContainer container = new ChartOfAccountsContainer();
		
		container.setAccountsList(listAccounts());
		
		return container;
	}

	public void save(ChartOfAccounts charOfaccounts) {
		chartOfAccountsDao.save(charOfaccounts);
	}

	public ChartOfAccounts getAccount(String detailKey) {
		return chartOfAccountsDao.getAccount(detailKey);
	}

	public void creditAccount(ChartOfAccounts accounts, double amount) {
		chartOfAccountsDao.creditAccount(accounts, amount);
	}

	public void creditAccount(String account, double amount) {
		ChartOfAccounts accounts = chartOfAccountsDao.getAccount(account);
		chartOfAccountsDao.creditAccount(accounts, amount);
	}

	public void debitAccount(ChartOfAccounts accounts, double amount) {
		chartOfAccountsDao.debitAccount(accounts, amount);
	}

	public void debitAccount(String account, double amount) {
		ChartOfAccounts accounts = chartOfAccountsDao.getAccount(account);
		chartOfAccountsDao.debitAccount(accounts, amount);
	}
	
	public boolean exists(String key) {
		return chartOfAccountsDao.exists(key);
	}

	public double getBalance(String account) {
		ChartOfAccounts chartOfAccounts = chartOfAccountsDao.getAccount(account);
		
		return chartOfAccounts.getAccountBalance();
	}

}
