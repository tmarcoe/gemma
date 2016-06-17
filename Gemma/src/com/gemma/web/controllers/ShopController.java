package com.gemma.web.controllers;

import java.io.Serializable;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.gemma.web.beans.Categories;
import com.gemma.web.beans.FileLocations;
import com.gemma.web.dao.Inventory;
import com.gemma.web.dao.InvoiceContainer;
import com.gemma.web.dao.InvoiceHeader;
import com.gemma.web.dao.InvoiceItem;
import com.gemma.web.dao.Returns;
import com.gemma.web.dao.UserProfile;
import com.gemma.web.service.InventoryService;
import com.gemma.web.service.InvoiceHeaderService;
import com.gemma.web.service.InvoiceService;
import com.gemma.web.service.ReturnsService;
import com.gemma.web.service.UserProfileService;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;


@Controller
@Scope(value="session")
public class ShopController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ShopController.class.getName());
	
	@Autowired
	private ReturnsService returnsService;
	
	@Autowired
	private InvoiceHeaderService invoiceHeaderService;

	@Autowired
	private InventoryService inventoryService;
	
	@Autowired
	private PagedListHolder<Inventory> inventoryList;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private FileLocations fileLocations;
	
	@Autowired
	private UserProfileService userProfileService;
	
	private PagedListHolder<Returns> returnsList;
	
	private Categories categories = null;

	private SimpleDateFormat dateFormat;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, false));
	}
	
	@RequestMapping(value="/products")
	public String products(Model model) {
		String fileLoc = fileLocations.getImageLoc();
		
		if (categories == null) {
			categories = new Categories();
		}
		
		inventoryList = inventoryService.getPagedList(categories);
		inventoryList.setPageSize(6);
		inventoryList.setPage(0);
		model.addAttribute("inventoryList",inventoryList);

		model.addAttribute("filter", buildFilter(categories));
		model.addAttribute("fileLoc", fileLoc);
		
		return "products";
	}
	
	@RequestMapping(value="/productdetails", method=RequestMethod.GET)
	public String showProductDetails(@ModelAttribute("skuNum") String skuNum, Model model ) {
		String fileLoc = fileLocations.getImageLoc();
		
		Inventory inventory = inventoryService.getItem(skuNum);
		InvoiceItem item = new InvoiceItem(inventory);
		
		model.addAttribute("invoiceItem", item);
		model.addAttribute("fileLoc", fileLoc);

		return "productdetails";
	}
	
	@RequestMapping("/orderproduct")
	public String orderProduct(@ModelAttribute("invoiceItem")InvoiceItem item, Model model, Principal principal ) {
		UserProfile user = userProfileService.getUser(principal.getName());
		InvoiceHeader header = invoiceHeaderService.getOpenOrder(user.getUserID());
		if (header == null) {
			header = new InvoiceHeader();
			header.setModified(new Date());
			header.setUserID(user.getUserID());
			header = invoiceHeaderService.createHeader(header);
		}
		item.setInvoiceNum(header.getInvoiceNum());
		item = invoiceService.addLineItem(item);
		logger.info("'" + item.getSkuNum() + "' was just added to the shopping cart.");

		List<InvoiceItem> invoiceList = invoiceService.getInvoice(header);
		InvoiceContainer invoice = new InvoiceContainer(header, invoiceList);
		String errorMsg = "";
		String couponNum = "CPN";
		
		model.addAttribute("errorMsg", errorMsg);
		model.addAttribute("couponNum", couponNum);
		
		model.addAttribute("invoice", invoice);
		
		return "cart";
	}
	
	@RequestMapping("/pickcategory")
	public String pickCategory(Model model) {
		List<String> catList = inventoryService.getCategory();
		
		model.addAttribute("catList", catList);
		
		return "pickcategory";
	}
	@RequestMapping("/setcategory")
	public String setCategory(@ModelAttribute("cat") String cat, Model model){
		String fileLoc = fileLocations.getImageLoc();
		
		if (categories == null) {
			categories = new Categories();
		}

		if (cat.length()== 0) {
			categories.setCategory("");
			categories.setSubCategory("");
			inventoryList = inventoryService.getPagedList(categories);
			inventoryList.setPageSize(6);
			inventoryList.setPage(0);
			model.addAttribute("inventoryList",inventoryList);
			model.addAttribute("fileLoc", fileLoc);

			return "products";
		}
		categories.setCategory(cat);
		categories.setSubCategory("");
		List<String> catList = inventoryService.getSubCategory(categories.getCategory());
		model.addAttribute("catList", catList);

		return "picksubcategory";
	}
	
	
	@RequestMapping("/setsubcategory")
	public String setSubCategory(@ModelAttribute("cat") String cat, Model model){
		String fileLoc = fileLocations.getImageLoc();
		categories.setSubCategory(cat);
		inventoryList = inventoryService.getPagedList(categories);

		inventoryList.setPageSize(6);
		inventoryList.setPage(0);
		model.addAttribute("inventoryList",inventoryList);
		
		model.addAttribute("filter", buildFilter(categories));
		model.addAttribute("fileLoc", fileLoc);
		
		return "products";
	}
	
	@RequestMapping("/returns-getlookup")
	public String returnsLookup(Model model) {
		Returns returns = new Returns();
		
		model.addAttribute("returns", returns);
		
		return "returns-getlookup";
	}
	
	@RequestMapping("/returns-submit")
	public String returnsSubmit(@Valid @ModelAttribute("returns") Returns returns, Model model, BindingResult result) {
		InvoiceItem invoice = invoiceService.getInvoiceItem(returns.getInvoiceNum(), returns.getSkuNum());
		if (result.hasErrors() == true ){
			return "returns-getlookup";
		}
		if (invoice == null) {
			result.rejectValue("invoiceNum","NotFound.returns.invoiceNum");
			return "returns-getlookup";
		}
		InvoiceHeader header = invoiceHeaderService.getInvoiceHeader(returns.getInvoiceNum());
		if (returns.getAmtReturned() > invoice.getAmount()) {
			result.rejectValue("amtReturned","Amount.returns.amtReturned");
			return "returns-getlookup";
		}
		returns.setPurchasePrice((double) (invoice.getPrice() * returns.getAmtReturned()));
		returns.setPurchaseTax((double) (invoice.getTax() * returns.getAmtReturned()));
		returns.setDatePurchased(header.getProcessed());
		invoice.setAmount(invoice.getAmount() - returns.getAmtReturned());
		invoiceService.updateItem(invoice);
		
		model.addAttribute("returns", returns);
		
		return "returns-submit";
	}
	
	@RequestMapping("/returns-save")
	public String returnsSave(@ModelAttribute("returns") Returns returns, Model model, Principal principal) {
		
		returns.setDateReturned(new Date());
		returns.setUsername(principal.getName());
		
		returnsService.create(returns);
		logger.info(" RMA # '" + returns.getRmaId() + "' has been created.");
		
		model.addAttribute("returns", returns);
		
		return "returns-getrma";
	}
	@RequestMapping("/returns-status")
	public String showReturnsStatus(Model model, Principal principal) {
		returnsList = returnsService.getReturnsList(principal.getName());
		returnsList.setPage(0);
		returnsList.setPageSize(10);
		model.addAttribute("returns", returnsList);
		return "returns-status";
	}
/***************************************************************************************************************
 * 	Pageination handlers
 **************************************************************************************************************/
	@RequestMapping(value="/returnsstatuspaging", method=RequestMethod.GET)
	public ModelAndView handleReturnsPaginRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int pgNum;
	    String keyword = request.getParameter("keyword");
	    if (keyword != null) {
	        if (!StringUtils.hasLength(keyword)) {
	            return new ModelAndView("Error", "message", "Please enter a keyword to search for, then press the search button.");
	        }

	        returnsList.setPageSize(3);
	        request.getSession().setAttribute("SearchProductsController_productList", inventoryList);
	        return new ModelAndView("products", "returns", returnsList);
	    }
	    else {
	        String page = request.getParameter("page");
	        
	        if (returnsList == null) {
	            return new ModelAndView("Error", "message", "Your session has timed out. Please start over again.");
	        }
	        pgNum = isInteger(page);
	        
	        if ("next".equals(page)) {
	        	returnsList.nextPage();
	        }
	        else if ("prev".equals(page)) {
	        	returnsList.previousPage();
	        }else if (pgNum != -1) {
	        	returnsList.setPage(pgNum);
	        }
	        
	        return new ModelAndView("returns-status", "returns", returnsList);
	    }
	}
	
	@RequestMapping(value="/paging", method=RequestMethod.GET)
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String fileLoc = fileLocations.getImageLoc();
		int pgNum;
	    String keyword = request.getParameter("keyword");
	    if (keyword != null) {
	        if (!StringUtils.hasLength(keyword)) {
	            return new ModelAndView("Error", "message", "Please enter a keyword to search for, then press the search button.");
	        }

	        inventoryList.setPageSize(4);
	        request.getSession().setAttribute("SearchProductsController_productList", inventoryList);
	        ModelAndView model = new ModelAndView("products");
	        model.addObject("inventoryList", inventoryList);
	        model.addObject("fileLoc", fileLoc);
	        return model;
	    }
	    else {
	        String page = request.getParameter("page");
	        
	        if (inventoryList == null) {
	            return new ModelAndView("Error", "message", "Your session has timed out. Please start over again.");
	        }
	        pgNum = isInteger(page);
	        
	        if ("next".equals(page)) {
	        	inventoryList.nextPage();
	        }
	        else if ("prev".equals(page)) {
	        	inventoryList.previousPage();
	        }else if (pgNum != -1) {
	        	inventoryList.setPage(pgNum);
	        }
	        
	        request.setAttribute("filter", buildFilter(categories));
	        ModelAndView model = new ModelAndView("products");
	        model.addObject("inventoryList", inventoryList);
	        model.addObject("fileLoc", fileLoc);
	        
	        return model;
	    }
	}
	private String buildFilter(Categories categories) {
		String filter = "";
		
		if (categories.getCategory().length() > 0) {
			filter = "Category: " + categories.getCategory();
		}
		if (categories.getSubCategory().length() > 0) {
			filter = filter + " >> Sub Category: " + categories.getSubCategory();
		}
		
		return filter;
	}
	private int isInteger(String s) {
		int retInt;
	    try { 
	    	retInt = Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return -1; 
	    } catch(NullPointerException e) {
	        return -1;
	    }
	    // only got here if we didn't return false
	    return retInt;
	}
}
