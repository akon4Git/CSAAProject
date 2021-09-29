/**
 * '###############################################################################
'# DESCRIPTION       :	CP116 , coded all ACs except 14800_01(V3)
 # Scenario Path     :
'# AUTHOR            :Anusha Konchada
'# CREATION DATE     :14th Aug,2015
'# REVISED BY : 	
'# REVISED DATE      : 
'# MODIFICATION HISTORY:
'-----------------------------------------------------------------
'Modified By         Modified Date              Description
'------------------------------------------------------------------
'################################################################################## 
 */

package regression.CommonPatterns;

import helpers.BillingTabHelper;
import helpers.CustomerHelper;
import helpers.FileHelper;
import helpers.HssHelper;
import helpers.HssPolicyData;
import helpers.PolicyTabHelper;
import helpers.TimePointsHelper;
import http.HttpStub;
import java.util.ArrayList;
import java.util.HashMap;
import org.testng.annotations.Test;
import regression.UtilityClasses.BillingAndPayments;
import regression.UtilityClasses.CancellationActions;
import regression.UtilityClasses.CommonUtilityFunctions;
import regression.UtilityClasses.ReinstatementActions;
import regression.commonMetadata.BillingTabMeta;
import regression.commonMetadata.PolicyActionMeta;
import regression.commonMetadata.PolicyActionMeta.Endorsement;
import regression.commonMetadata.PremiumTabMeta;
import toolkit.selenium.controls.WaitMode;
import toolkit.utils.datetime.DateTime;
import toolkit.verification.CustomAssert;
import ui.admin.metadata.Jobs;
import ui.app.metadata.BillingMeta;
import ui.app.metadata.BillingMeta.PaymentPlan;
import ui.app.metadata.BillingMeta.PaymentsColumns;
import ui.app.metadata.BillingMeta.PaymentsStatus;
import ui.app.metadata.BillingMeta.PendingTransColumns;
import ui.app.metadata.DocumentsMeta.OnDemandDocuments;
import ui.app.metadata.PolicyMeta;
import ui.app.metadata.PolicyMeta.PolicyAction;
import ui.app.metadata.PolicyMeta.PolicyStatus;
import ui.app.metadata.PolicyMeta.RenewAction;
import ui.app.metadata.PolicyMeta.RenewalStatus;
import ui.app.metadata.QuoteMeta;
import ui.app.metadata.SearchMeta.SearchBy;
import ui.app.metadata.SearchMeta.SearchFor;
import ui.app.metadata.TabNames;
import ui.app.metadata.TabNames.TopPanelTabs;
import ui.app.model.panel.SearchPanel;
import ui.app.model.panel.TopPanel;
import ui.app.model.panel.edit.EditorPanel;
import ui.app.model.panel.edit.OnDemandDocumentsEditorPanel;
import ui.app.model.panel.edit.billing.AcceptPaymentEditorPanel;
import ui.app.model.panel.edit.billing.AddPaymentMethodEditorPanel;
import ui.app.model.panel.edit.billing.DeclinePayment;
import ui.app.model.panel.edit.billing.RefundEditorPanel;
import ui.app.model.panel.edit.policy.EndorsementEditorPanel;
import ui.app.model.panel.edit.quote.BindTabEditorPanel;
import ui.app.model.panel.edit.quote.PaymentEditorPanel;
import ui.app.model.panel.edit.quote.QuotesPanel;
import ui.app.model.panel.edit.quote.hss.ApplicantTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.DocumentsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.GeneralTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PremiumsAndCoveragesTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PropertyInfoTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ReportsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.UnderwritingApprovalTabEditorPanel;
import ui.app.model.panel.summary.BillingSummaryPanel;
import ui.app.model.panel.summary.PolicySummaryPanel;
import ui.app.model.panel.summary.RenewalSummaryPanel;
import ui.meta.attributes.hss.HssPremiumMetaData;
import ui.meta.attributes.hss.HssPropertyInfoMetaData.HssPPC;
import utilities.Dollar;
import utilities.JobUtils;
import utilities.time.TimeSetterUtil;
import base.BaseTest;
import com.exigen.automation.helper.AutomationUtils;
import docgen.DocGenHelper;
import docgen.DocGenMeta.XPathInfo;

public class CP116 extends BaseTest {

	public String policyNumber = "", premiumDownPaymentAmt;
	public ArrayList<DateTime> dates = FileHelper.getInstallmentsDueDates(PaymentPlan.MONTHLY_STANDARD);
	public DateTime installmentDD1 = dates.get(0), installmentDD2 = dates.get(1);
	public DateTime cancelTransactionDate, expirationDate, effectiveDate;
	public Dollar totalPaidAtRplus1_PolicyActive, totalPaidPolicyAmt;

	// DD0
	@Test(priority = 1)
	public void TC_CreatePolicy() {

		EUApp().open();
		HssPolicyData policyData = new HssPolicyData(getDataSuite(), getPolicyType() + "_Dataset1");
		policyData.setPropertyInfoDataset("HO4_Fire_Theft_Dataset1");

		CustomerHelper.createCustomer("Dataset1");
		HssHelper.initiateQuoteCreation();

		GeneralTabEditorPanel.fillGeneralTab(getPolicyType() + "_Dataset1");
		GeneralTabEditorPanel.next();

		ApplicantTabEditorPanel.fillApplicantTab(getPolicyType() + "_Dataset1");
		ApplicantTabEditorPanel.continueBtn();
		ReportsTabEditorPanel.orderAllReports();
		ReportsTabEditorPanel.overrideInsuranceScore("925");
		ReportsTabEditorPanel.continueBtn();
		PropertyInfoTabEditorPanel.fillPropertyInfoTab(getPolicyType() + "_Dataset2");
		PropertyInfoTabEditorPanel.fillFireProtectiveDeviceDiscount(policyData.propertyInfoFireProtectiveDeviceDiscount);
		PropertyInfoTabEditorPanel.fillTheftProtectiveDeviceDiscount(policyData.propertyInfoTheftProtectiveDeviceDiscount);
		PropertyInfoTabEditorPanel.continueBtn();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		PremiumsAndCoveragesTabEditorPanel.quotePremiumAndCoverages.getControl(HssPremiumMetaData.HssPremium.PAYMENT_PLAN.getLabel()).setValue(PremiumTabMeta.PaymentPlan.PLAN_SEMI_ANNUAL.get(), WaitMode.AJAX);
		PremiumsAndCoveragesTabEditorPanel.rate();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.UNDERWRITING_AND_APPROVAL.get());
		UnderwritingApprovalTabEditorPanel.fillUnderwritingApprovalTab(getPolicyType() + "_Dataset1");
		UnderwritingApprovalTabEditorPanel.continueBtn();

		DocumentsTabEditorPanel.fillDocumentsTab(getPolicyType() + "_Dataset1");
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
		BindTabEditorPanel.purchase();

		if(QuotesPanel.tbError.isPresent()) {
			BindTabEditorPanel.overridePolicy();
			BindTabEditorPanel.purchase();
		}

		premiumDownPaymentAmt = PaymentEditorPanel.tblPaymentPlan.getRow(1).getCell(2).getValue();
		PaymentEditorPanel.btnAddPaymentMethod.click();
		AddPaymentMethodEditorPanel.addEFTPaymentMethod();
		PaymentEditorPanel.setActivateAutopayChb(true);
		PaymentEditorPanel.setActivateAutopayChb(false);
		Dollar amount = new Dollar(PaymentEditorPanel.balanceDue.getValue());
		PaymentEditorPanel.getAmountTextBox(2).setValue(amount.toString());
		PaymentEditorPanel.btnApplyPayment.click();
		PaymentEditorPanel.confirm();

		policyNumber = PolicySummaryPanel.getPolicyNumber();
		PolicySummaryPanel.verifyPolicyStatus(PolicyMeta.PolicyStatus.ACTIVE);
		expirationDate = PolicyTabHelper.getExpirationDate();
		effectiveDate = PolicyTabHelper.getEffectiveDate();
		log.info("==========================================");
		log.info(getState() + " Home policy is created: " + policyNumber);
		log.info("==========================================");

		TopPanel.navigateTo(TopPanelTabs.BILLING);
		BillingSummaryPanel.tblPaymentsAndOtherTransactions.getRow(BillingMeta.PendingTransColumns.SUBTYPE_REASON.get(), BillingTabMeta.PaymentOtherTransactions.DEPOSITE_PAYMENT.get()).getCell(BillingMeta.PaymentsColumns.ACTION.get()).controls.links.get(BillingMeta.PaymentActionEnum.DECLINE.get()).click(WaitMode.AJAX);
		DeclinePayment.declinePayment.getControl(BillingTabMeta.Decline.DECLINE_REASON.get()).setValue(BillingTabMeta.Decline.FEE_RESTRICTION.get(), WaitMode.AJAX);
		DeclinePayment.btnOk.click(WaitMode.AJAX);
		BillingTabHelper.verifyPaymentDeclined(effectiveDate);

		// #L - NSF posted on the policy.
		HashMap<PaymentsColumns, String> nsf = new HashMap<BillingMeta.PaymentsColumns, String>();
		nsf.put(PaymentsColumns.TRANSACTION_DATE, AutomationUtils.getCurrentDate());
		nsf.put(PaymentsColumns.TYPE, BillingTabMeta.TransactionType.FEE.get());
		nsf.put(PaymentsColumns.SUBTYPE_REASON, BillingMeta.AdvancedAllocationPanel.NSF_FEE_RESTRICTION.get());
		BillingTabHelper.getPaymentsRow(nsf).verify.present();

		EUApp().close();
	}

	@Test(priority = 2)
	public void TC_Cancellation() {

		cancelTransactionDate = TimePointsHelper.getBillGenerationDate(dates.get(0));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(cancelTransactionDate, null);

		EUApp().open();
		CancellationActions.setAndVerifyManualCancellation(effectiveDate, policyNumber, PolicyActionMeta.Cancellation.NEW_BUSINESS_RECISSION_NSF.get());

		// CH_VP_HO_15065_1_V1
		PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.CANCELLED);

		TopPanel.navigateTo(TopPanelTabs.BILLING);

		// CH_VP_HO_15065_1_V2
		HashMap<PaymentsColumns, String> cancellationWithNSF = new HashMap<BillingMeta.PaymentsColumns, String>();
		cancellationWithNSF.put(PaymentsColumns.TYPE, BillingTabMeta.PaymentOtherTransactions.PREMIUM.get());
		cancellationWithNSF.put(PaymentsColumns.SUBTYPE_REASON, BillingTabMeta.PaymentOtherTransactions.CANCELLATION_UW_REASONS_NSF_DOWNPAYMENT.get());
		cancellationWithNSF.put(PaymentsColumns.STATUS, PaymentsStatus.APPLIED.get());
		cancellationWithNSF.put(PaymentsColumns.AMOUNT, "(" + premiumDownPaymentAmt + ")");
		BillingTabHelper.getPaymentsRow(cancellationWithNSF).verify.present();

		EUApp().close();
	}

	@Test(priority = 4)
	public void TC_ReinstatementwithoutLapse() {

		DateTime reinsWithLapseEffDate = dates.get(0);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(reinsWithLapseEffDate, null);

		EUApp().open();

		// CH_VP_HO_15481_1_V3
		// CH_VP_HO_15481_1_V4
		// CH_VP_HO_15481_1_V5
		ReinstatementActions.reinstatePolicyWithoutLapse(reinsWithLapseEffDate, effectiveDate, policyNumber);
		EUApp().close();
	}

	// DD6-20
	@Test(priority = 12)
	public void TC_Bill_DD6() {

		BillingAndPayments.generateBillAndVerify(dates.get(5), policyNumber);
		EUApp().close();
	}

	// DD6
	@Test(priority = 13)
	public void TC_PayDD6() {

		DateTime billDueDate = TimePointsHelper.getBillDueDate(dates.get(5));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(billDueDate, null);

		// #L__Manual payment is done
		BillingAndPayments.payBillManualAndCheck(billDueDate, policyNumber);
		EUApp().close();
	}

	// DD10-20
	@Test(priority = 20)
	public void TC_Bill_DD10() {

		DateTime endorsementEffDate = TimePointsHelper.getBillGenerationDate(dates.get(9));
		TimeSetterUtil.getInstance().nextPhase(endorsementEffDate);

		//BillingSummaryPanel.lnkPolicyNumber.click();
		PolicySummaryPanel.setActionAndGo(PolicyAction.ENDORSEMENT);
		EndorsementEditorPanel.endorsementList.getControl("Endorsement Date").setValue(endorsementEffDate.toString());
		EndorsementEditorPanel.endorsementList.getControl("Endorsement Reason").setValue(Endorsement.ENDORSEMENT_VALUE.get(), WaitMode.AJAX);
		EditorPanel.ok();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
		ReportsTabEditorPanel.overrideInsuranceScore("650");
		ReportsTabEditorPanel.reorderReports();
		ReportsTabEditorPanel.continueBtn();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage E - Personal Liability Each Occurrence").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$400,000");
		wait(3000);

		PremiumsAndCoveragesTabEditorPanel.rate();

		if(QuotesPanel.tbError.isPresent()) {
			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
			ReportsTabEditorPanel.reorderReports();
			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PROPERTY_INFO.get());
			PropertyInfoTabEditorPanel.quotePropertyInfoPPCList.getControl(HssPPC.FIRE_DEPARTMENT_TYPE.getLabel()).setValue("S - Subscription based");
			// PropertyInfoTabEditorPanel.quotePropertyInfoPPCList.getControl(HssPPC.SUBSCRIPTION_TO_FIRE_DEPARTMENT_STATION.getLabel()).setValue("Yes");
			PropertyInfoTabEditorPanel.quotePropertyInfoPPCList.getControl(HssPPC.PUBLIC_PROTECTION_CLASS.getLabel()).setValue("5");
			PropertyInfoTabEditorPanel.quotePropertyInfoPPCList.getControl(HssPPC.DISTANCE_TO_FIRE_HYDRANT.getLabel()).setValue("<=1000ft");
			PropertyInfoTabEditorPanel.quotePropertyInfoPPCList.getControl(HssPPC.FIRE_PROTECTION_AREA.getLabel()).setValue("None");

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
			PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage E - Personal Liability Each Occurrence").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$400,000");
			wait(3000);
			PremiumsAndCoveragesTabEditorPanel.rate();

		}
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
		BindTabEditorPanel.purchase();

		if(QuotesPanel.tbError.isPresent()) {
			BindTabEditorPanel.overrideAllRules("Term", "Other");
			BindTabEditorPanel.purchase();
		}
		PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.ACTIVE);
		EUApp().close();
	}

	@Test(priority = 21)
	public void TC_R_R1() {

		EUApp().open();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber);
		expirationDate = PolicyTabHelper.getExpirationDate();
		effectiveDate = PolicyTabHelper.getEffectiveDate();

		DateTime renewDate73 = TimePointsHelper.getRenewImageGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDate73, null);

		JobUtils.executeJob(Jobs.renewalOfferGenerationPart1);
		HttpStub.executeAllBatches();
		JobUtils.executeJob(Jobs.renewalOfferGenerationPart2);

		EUApp().open();

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		// CH_VP_15949_01_V1
		PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.ACTIVE);

		// CH_VP_#L
		PolicyTabHelper.verifyAutomatedRenewalGenerated(renewDate73);
		PolicySummaryPanel.openRenewals();
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.GATHERING_INFO);
		EUApp().close();
	}

	@Test(priority = 23)
	public void TC_R_R5() {

		DateTime renewR5_45 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR5_45, Jobs.renewalOfferGenerationPart2);

		EUApp().open();

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.verifyRenewalLnkIsPresent();
		PolicySummaryPanel.openRenewals();

		// #L_Check renewal image status
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.PREMIUM_CALCULATED);

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		String discountValue = PremiumsAndCoveragesTabEditorPanel.tblDiscounts.getRow("Discount Category", QuoteMeta.DiscountCategory.SAFEHOME.get()).getCell("Discounts Applied").getValue();
		CustomAssert.assertTrue("ERROR: Membership discount is not present. CH_VP_HO_17726_07_V7", discountValue.contains("Green Home"));
		CustomAssert.assertTrue("ERROR: Membership discount is not present. CH_VP_HO_17726_05_V5", discountValue.contains("Theft Protection"));
		CustomAssert.assertTrue("ERROR: Membership discount is not present.  CH_VP_HO_17726_04_V4", discountValue.contains("Fire Protection"));

		TopPanel.btnCancel.click();
	}

	@Test(priority = 24)
	public void TC_R_R7() {

		DateTime renewR7_35 = TimePointsHelper.getRenewOfferGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR7_35, Jobs.renewalOfferGenerationPart2);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);

		EUApp().open();

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		// CH_VP_#L
		PolicySummaryPanel.openRenewals();
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.PROPOSED);

		PolicySummaryPanel.setActionAndGo(PolicyAction.ON_DEMAND_DOCUMENTS);
		// CH_VP_17410_04_V13
		OnDemandDocumentsEditorPanel.verifyDocumentIsPresent(OnDemandDocuments.AHAUXX);

		// TODO
		String fileSuffix = DocGenHelper.downloadAndRemoveXMLToLocalFolder(policyNumber, XPathInfo.POLICY_MISCELLANEOUS);
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AHAUXX.getIDInXML(), policyNumber, fileSuffix, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("HS 04 59 is not generated", true);
		}

		EUApp().close();
	}

	@Test(priority = 25)
	public void TC_Renewals() {

		CommonUtilityFunctions.shiftTimeAndExecuteJob(expirationDate, null);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		String overPaymentAmt = "$1500";
		BillingSummaryPanel.btnAcceptPayment.click();
		AcceptPaymentEditorPanel.fillAcceptPaymentTab("Cash", overPaymentAmt);
		EditorPanel.btnOk.click();

		EUApp().close();
	}

	@Test(priority = 26)
	public void TC_R_1_Update_PolicyStatus() {

		DateTime renewDatePlus1 = TimePointsHelper.getUpdatePolicyStatusDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDatePlus1, Jobs.policyStatusUpdateJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber);
		BillingSummaryPanel.showPriorTerms();

		// CH_VP_#L
		BillingTabHelper.verifyPolicyStatus(effectiveDate, PolicyStatus.EXPIRED);
		BillingTabHelper.verifyPolicyStatus(expirationDate, PolicyStatus.PROPOSED);

		totalPaidAtRplus1_PolicyActive = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Active").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());
		totalPaidPolicyAmt = new Dollar(BillingSummaryPanel.tblBillingGeneralInformation.getRow(1).getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());

		EUApp().close();
	}

	@Test(priority = 27)
	public void TC_R_R12_Refund() {

		DateTime refundPlus12 = expirationDate.addDays(10);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(refundPlus12, Jobs.refundGenerationJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber);

		BillingSummaryPanel.showPriorTerms();
		Dollar totalPaidAtRplus12_PolicyActive = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Active").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());
		Dollar totalPaidAt12 = new Dollar(BillingSummaryPanel.tblBillingGeneralInformation.getRow(1).getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());

		HashMap<PendingTransColumns, String> refundQuery = new HashMap<BillingMeta.PendingTransColumns, String>();
		refundQuery.put(PendingTransColumns.TYPE, "Refund");
		refundQuery.put(PendingTransColumns.SUBTYPE_REASON, BillingTabMeta.PendingTansactions.AUTOMATED_REFUND.get());
		refundQuery.put(PendingTransColumns.STATUS, BillingTabMeta.PendingTansactions.STATUS_PENDING.get());

		// CH_VP_HO_16276_6_V10
		BillingTabHelper.getPendingPaymentsRow(refundQuery).verify.present();
		Dollar refundAmt = new Dollar(BillingTabHelper.getPendingPaymentsRow(refundQuery).getCell(PendingTransColumns.AMOUNT.get()).getValue());

		CustomAssert.assertEquals("CH_VP_HO_16276_6_V12_01", totalPaidAtRplus1_PolicyActive.subtract(totalPaidAtRplus12_PolicyActive), refundAmt);
		CustomAssert.assertEquals("CH_VP_HO_16276_6_V12_02", totalPaidPolicyAmt.subtract(totalPaidAt12), refundAmt);

		BillingTabHelper.getPendingPaymentsRow(refundQuery).getCell(PendingTransColumns.TYPE.get()).controls.links.get("Refund", WaitMode.AJAX).click();

		CustomAssert.assertTrue("CH_VP_HO_16276_6_V11_01", RefundEditorPanel.billingRefund.getControl("Payment Method").getValue().equalsIgnoreCase("Check"));
		CustomAssert.assertFalse("CH_VP_HO_16276_6_V11_02", RefundEditorPanel.billingRefund.getControl("Check Number").getValue().isEmpty());

		CustomAssert.assertAll();
		EUApp().close();
	}

}
