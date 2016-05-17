package com.gemma.web.controllers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.antlr.v4.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenRequest;
import com.gemma.web.beans.AddressLabel;
import com.gemma.web.beans.FileLocations;
import com.gemma.web.beans.FileUpload;
import com.gemma.web.dao.Inventory;
import com.gemma.web.dao.InvoiceContainer;
import com.gemma.web.dao.InvoiceHeader;
import com.gemma.web.dao.InvoiceItem;
import com.gemma.web.dao.UserProfile;
import com.gemma.web.payment.BraintreeGatewayFactory;
import com.gemma.web.payment.Checkout;
import com.gemma.web.payment.Payment;
import com.gemma.web.service.AccountingService;
import com.gemma.web.service.GeneralLedgerService;
import com.gemma.web.service.InventoryService;
import com.gemma.web.service.InvoiceHeaderService;
import com.gemma.web.service.InvoiceService;
import com.gemma.web.service.UserProfileService;

@Controller
@Scope(value = "session")
public class ShoppingCartController implements Serializable {
	private static final long serialVersionUID = 4725326820861092920L;
	private static Logger logger = Logger.getLogger(AccountingService.class
			.getName());
	private BraintreeGateway gateway;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private InvoiceHeaderService invoiceHeaderService;

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private GeneralLedgerService generalLedgerService;

	@Autowired
	private FileLocations fileLocations;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(true);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
	}

	@RequestMapping("/saveitem")
	public String saveInvoiceItem(
			@Valid @ModelAttribute("item") InvoiceItem item, Model model,
			BindingResult result) {
		if (result.hasErrors()) {
			return "editcart";
		}
		int invoiceNum = item.getInvoiceNum();
		invoiceService.updateItem(item);
		InvoiceHeader header = invoiceHeaderService
				.getInvoiceHeader(invoiceNum);
		if (header == null) {
			return "nocart";
		}
		List<InvoiceItem> invoiceList = invoiceService.getInvoice(header);
		InvoiceContainer invoice = new InvoiceContainer(header, invoiceList);

		model.addAttribute("invoice", invoice);

		return "cart";
	}

	@RequestMapping("/cancelsale")
	public String cancelSale(Principal principal, Model model) {
		String fileLoc = fileLocations.getImageLoc();

		UserProfile user = userProfileService.getUser(principal.getName());
		InvoiceHeader header = invoiceHeaderService.getOpenOrder(user
				.getUserID());
		invoiceService.deleteInvoice(header);

		List<Inventory> inventory = inventoryService.listSaleItems();
		model.addAttribute("inventory", inventory);
		model.addAttribute("fileLoc", fileLoc);

		return "home";
	}

	@RequestMapping("/deleteinvoiceitem")
	public String deleteInvoiceItem(int invoiceNum, String skuNum, Model model) {
		invoiceService.deleteInvoiceItem(invoiceNum, skuNum);
		logger.info(String.format(
				"'%s' has been removed from invoice # '%08d'.", skuNum,
				invoiceNum));
		InvoiceHeader header = invoiceHeaderService
				.getInvoiceHeader(invoiceNum);
		if (header == null) {
			return "nocart";
		}
		List<InvoiceItem> invoiceList = invoiceService.getInvoice(header);
		InvoiceContainer invoice = new InvoiceContainer(header, invoiceList);

		model.addAttribute("invoice", invoice);

		return "cart";
	}

	@RequestMapping("/cart")
	public String showCart(Principal principal, Model model) {
		UserProfile user = userProfileService.getUser(principal.getName());
		InvoiceHeader header = invoiceHeaderService.getOpenOrder(user
				.getUserID());
		if (header == null) {
			return "nocart";
		}
		List<InvoiceItem> invoiceList = invoiceService.getInvoice(header);
		InvoiceContainer invoice = new InvoiceContainer(header, invoiceList);

		model.addAttribute("invoice", invoice);

		return "cart";
	}

	@RequestMapping("/viewcart")
	public String viewCart(@ModelAttribute("invoiceNum") int invoiceNum,
			Model model) {
		InvoiceHeader header = invoiceHeaderService
				.getInvoiceHeader(invoiceNum);
		List<InvoiceItem> invoiceList = invoiceService.getInvoice(header);
		InvoiceContainer invoice = new InvoiceContainer(header, invoiceList);

		model.addAttribute("invoice", invoice);

		return "cart";
	}

	@RequestMapping("/pcinfo")
	public String processPayment(Principal principal, Model model) throws URISyntaxException {
		Payment payment = new Payment();
		gateway = BraintreeGatewayFactory.fromConfigFile(new File(new URI(fileLocations
				.getPaymentConfig() + "braintree.properties")));
		@SuppressWarnings("unused")
		ClientTokenRequest clientTokenRequest = new ClientTokenRequest()
				.customerId(principal.getName());

		model.addAttribute("payment", payment);
		model.addAttribute("clientToken", gateway.clientToken().generate());

		return "pcinfo";
	}

	@RequestMapping("/editcart")
	public String editCart(int invoiceNum, String skuNum, Model model) {
		InvoiceItem item = invoiceService.getInvoiceItem(invoiceNum, skuNum);

		model.addAttribute("item", item);

		return "editcart";
	}

	@RequestMapping("/pod")
	public String paymentOnDelivery(Principal principal, Model model) {
		UserProfile user = userProfileService.getUser(principal.getName());
		InvoiceHeader header = invoiceHeaderService.getOpenOrder(user
				.getUserID());
		if (header == null) {
			return "nocart";
		}
		header = invoiceHeaderService.totalHeader(header);
		header.setPod(true);
		header.setAddedCharges((header.getTotal() * .1));

		try {
			logger.info("Processing shopping cart.");
			invoiceHeaderService.podProcessShoppingCart(header);
		} catch (IOException | RuntimeException e) {
			return "error";
		}

		model.addAttribute("invoiceHeader", header);

		return "thankyou";
	}

	@RequestMapping("/processcart")
	public String processShoppingCart(
			@ModelAttribute("payment") Payment payment,
			@ModelAttribute("payment_method_nonce") String nonce,
			Principal principal, Model model) throws RecognitionException  {

		UserProfile user = userProfileService.getUser(principal.getName());
		InvoiceHeader header = invoiceHeaderService.getOpenOrder(user
				.getUserID());
		Checkout checkout = new Checkout();
		header = invoiceHeaderService.totalHeader(header);
		header.setUserID(user.getUserID());
		BigDecimal total = BigDecimal.valueOf(header.getTotal()
				+ header.getTotalTax() + header.getShippingCost());
		if (checkout.postForm(payment, gateway, total, nonce) == true) {
			logger.info("Processing shopping cart.");
			try {
				invoiceHeaderService.processShoppingCart(header);
			} catch (IOException | RuntimeException e ) {
				return "error";
			}
		} else {
			return "pcdenied";
		}
		model.addAttribute("invoiceHeader", header);

		return "thankyou";
	}

	@RequestMapping("/pcdenied")
	public String showPcDenied() {
		return "pcdenied";
	}

	@RequestMapping("/filepicker")
	public String filePicker(Model model) {
		FileUpload filePrint = new FileUpload();

		model.addAttribute("filePrint", filePrint);

		return "filepicker";
	}

	@RequestMapping("/processorders")
	public String processOrders() throws IOException, URISyntaxException {
		AddressLabel lbl = new AddressLabel();

		String[] label = { "firstname", "lastname", "address1", "address2",
				"city", "region", "postalCode", "country", "invoiceNum" };
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");

		String fileName = fileLocations.getOutPath() + sdf.format(new Date())
				+ ".csv";
		Writer hdr = new FileWriter(new File(new URI(fileName)));
		CsvBeanWriter csvWriter = new CsvBeanWriter(hdr,
				CsvPreference.STANDARD_PREFERENCE);

		List<InvoiceHeader> headers = invoiceHeaderService
				.getProcessedInvoices();
		csvWriter.writeHeader(label);
		for (InvoiceHeader header : headers) {
			UserProfile user = userProfileService.getUserByID(header
					.getUserID());
			lbl.setFirstname(user.getFirstname());
			lbl.setLastname(user.getLastname());
			lbl.setAddress1(user.getaddress1());
			lbl.setAddress2(user.getaddress2());
			lbl.setCity(user.getcity());
			lbl.setRegion(user.getregion());
			lbl.setPostalCode(user.getpostalCode());
			lbl.setCountry(user.getcountry());
			lbl.setInvoiceNum(String.format("%06d", header.getInvoiceNum()));
			csvWriter.write(lbl, label);

			Writer inv = new FileWriter(new File(new URI(fileLocations.getOutPath() + 
										String.format("%08d", header.getInvoiceNum()) + ".inv"))
					);
			List<InvoiceItem> invoices = invoiceService.getInvoice(header);
			String address2 = "";
			if ("".compareTo(user.getaddress2()) != 0) {
				address2 = user.getaddress2() + "\n";
			}
			String invoiceHeading = user.getFirstname() + " "
					+ user.getLastname() + "\n" + user.getaddress1() + "\n"
					+ address2 + user.getcity() + "," + user.getregion() + " "
					+ user.getpostalCode() + " " + user.getcountry() + "\n"
					+ "Invoice # "
					+ String.format("%06d", header.getInvoiceNum()) + "\n";
			if (header.isPod() == true) {
				invoiceHeading = invoiceHeading + "Payment ON Delivery\n\n";
			} else {
				invoiceHeading = invoiceHeading + "\n";
			}
			inv.write(invoiceHeading);
			double total = 0;
			double totalTax = 0;

			for (InvoiceItem invoice : invoices) {
				double price = invoice.getAmount() * invoice.getPrice();
				double tax = invoice.getAmount() * invoice.getTax();
				total += price;
				totalTax += tax;
				inv.write(String.format("%s\t%d\tP%.2f [SKU - %s]\n",
						invoice.getProductName(), invoice.getAmount(), price,
						invoice.getSkuNum()));
			}
			inv.write("\n\n\n");
			inv.write("Subtotal................. "
					+ String.format("P%.2f\n", total));
			inv.write("POD Charge............... "
					+ String.format("P%.2f\n", header.getAddedCharges()));
			inv.write("Tax...................... "
					+ String.format("P%.2f\n", totalTax));
			inv.write("Total.................... "
					+ String.format("P%.2f\n",
							total + totalTax + header.getAddedCharges()));
			inv.close();
			header.setDateShipped(new Date());
			invoiceHeaderService.updateHeader(header);
		}
		csvWriter.close();

		return "admin";
	}
}
