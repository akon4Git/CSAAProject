/**
 * '###############################################################################
'# DESCRIPTION       :	CP100 , coded all ACs except 14800_01(V3)
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
import helpers.PolicyTabHelper;
import helpers.TimePointsHelper;
import http.HttpStub;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.testng.annotations.Test;
import regression.UtilityClasses.BillingAndPayments;
import regression.UtilityClasses.CancellationActions;
import regression.UtilityClasses.CommonPolicyActions;
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
import ui.app.metadata.BillingMeta.BillColumns;
import ui.app.metadata.BillingMeta.GeneralInfoColumns;
import ui.app.metadata.BillingMeta.PaymentPlan;
import ui.app.metadata.BillingMeta.PaymentsColumns;
import ui.app.metadata.BundleMeta;
import ui.app.metadata.DocumentsMeta.OnDemandDocuments;
import ui.app.metadata.PolicyMeta;
import ui.app.metadata.PolicyMeta.PolicyAction;
import ui.app.metadata.PolicyMeta.PolicyStatus;
import ui.app.metadata.PolicyMeta.RenewAction;
import ui.app.metadata.PolicyMeta.RenewalStatus;
import ui.app.metadata.QuoteMeta;
import ui.app.metadata.QuoteMeta.EndorsementForms;
import ui.app.metadata.SearchMeta.SearchBy;
import ui.app.metadata.SearchMeta.SearchFor;
import ui.app.metadata.TabNames;
import ui.app.metadata.TabNames.QuoteHSSTabs;
import ui.app.metadata.TabNames.TopPanelTabs;
import ui.app.model.panel.SearchPanel;
import ui.app.model.panel.TopPanel;
import ui.app.model.panel.edit.EditorPanel;
import ui.app.model.panel.edit.ErrorPanel;
import ui.app.model.panel.edit.billing.AcceptPaymentEditorPanel;
import ui.app.model.panel.edit.billing.AddPaymentMethodEditorPanel;
import ui.app.model.panel.edit.dialogs.RatingDetailsDialog;
import ui.app.model.panel.edit.policy.EndorsementEditorPanel;
import ui.app.model.panel.edit.quote.BindTabEditorPanel;
import ui.app.model.panel.edit.quote.PaymentEditorPanel;
import ui.app.model.panel.edit.quote.QuoteEditorPanel;
import ui.app.model.panel.edit.quote.QuotesPanel;
import ui.app.model.panel.edit.quote.hss.ApplicantTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.DocumentsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.EndorsementTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.GeneralTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PremiumsAndCoveragesTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PropertyInfoTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ReportsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.UnderwritingApprovalTabEditorPanel;
import ui.app.model.panel.summary.BillingSummaryPanel;
import ui.app.model.panel.summary.PolicySummaryPanel;
import ui.app.model.panel.summary.RenewalSummaryPanel;
import ui.common.LoginPanel.LoginFields;
import ui.common.metadata.LoginMeta;
import ui.meta.attributes.hss.HssPremiumMetaData;
import utilities.Dollar;
import utilities.JobUtils;
import base.BaseTest;
import com.exigen.automation.helper.AutomationUtils;
import docgen.DocGenHelper;
import docgen.DocGenMeta.XPathInfo;

public class CP100 extends BaseTest {

	public String policyNumber = "";
	public ArrayList<DateTime> dates = FileHelper.getInstallmentsDueDates(PaymentPlan.MONTHLY_STANDARD);
	public DateTime installmentDD1 = dates.get(0), installmentDD2 = dates.get(1);
	public DateTime cancelNoticeDate, cancellationEffDateIReq1, cancelDateC1, expirationDate, effectiveDate;
	public Dollar amtReceivedOnAutopayWithdrawl, premiumAtR_R5_01, premiumAtR_R5_02, premiumAtR_R5_03, amtToPayR_10;
	DateTime date_DD2_20, canTransactionDateIReq1;
	private static DecimalFormat df = new DecimalFormat("#");

	// DD0
	@Test(priority = 1)
	public void TC_CreatePolicy() {

		EUApp().open();
		CustomerHelper.createCustomer("DefaultDataset");
		HssHelper.initiateQuoteCreation();

		GeneralTabEditorPanel.fillGeneralTab("DefaultDataset");
		GeneralTabEditorPanel.next();

		ApplicantTabEditorPanel.fillApplicantTab("DefaultDataset");
		ApplicantTabEditorPanel.continueBtn();
		ReportsTabEditorPanel.orderAllReports();
		ReportsTabEditorPanel.continueBtn();
		PropertyInfoTabEditorPanel.fillPropertyInfoTab("DefaultDataset");
		PropertyInfoTabEditorPanel.continueBtn();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_ENDORSEMENT.get());
		EndorsementTabEditorPanel.fillEndorsementTab("HO4_Dataset1");

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		PremiumsAndCoveragesTabEditorPanel.quotePremiumAndCoverages.getControl(HssPremiumMetaData.HssPremium.PAYMENT_PLAN.getLabel()).setValue(PremiumTabMeta.PaymentPlan.PLAN_ELEVEN_PAY_STANDARD.get(), WaitMode.AJAX);
		PremiumsAndCoveragesTabEditorPanel.rate();

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.UNDERWRITING_AND_APPROVAL.get());
		UnderwritingApprovalTabEditorPanel.fillUnderwritingApprovalTab("DefaultDataset");
		UnderwritingApprovalTabEditorPanel.continueBtn();

		DocumentsTabEditorPanel.fillDocumentsTab("DefaultDataset");
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
		BindTabEditorPanel.purchase();

		CustomAssert.assertTrue(ErrorPanel.tbErrorsList.getRowContains("Message->Underwriting approval is required to add Additional Insured - Special Event e...").isPresent());
		if(QuotesPanel.tbError.isPresent()) {
			BindTabEditorPanel.referForApproval();
			BindTabEditorPanel.purchase();
			BindTabEditorPanel.overridePolicy();
			BindTabEditorPanel.purchase();
		}

		PaymentEditorPanel.btnAddPaymentMethod.click();
		AddPaymentMethodEditorPanel.addEFTPaymentMethod();
		PaymentEditorPanel.fillAutopaySection();
		PaymentEditorPanel.cbAutopaySelection.setValue("index=1");
		if(PaymentEditorPanel.rgSignatureOnFileIndicator.isPresent()) PaymentEditorPanel.rgSignatureOnFileIndicator.setValue(PremiumTabMeta.Payment.YES.get());

		Dollar minDwnpayment = new Dollar(2);
		PaymentEditorPanel.setChangeMinDownPaymentCB(true);
		PaymentEditorPanel.tbMinRequiredDownPayment.setValue(minDwnpayment.toPlaingString(), WaitMode.AJAX);
		PaymentEditorPanel.fillPaymentTab();

		policyNumber = PolicySummaryPanel.getPolicyNumber();
		PolicySummaryPanel.verifyPolicyStatus(PolicyMeta.PolicyStatus.ACTIVE);
		expirationDate = PolicyTabHelper.getExpirationDate();
		effectiveDate = PolicyTabHelper.getEffectiveDate();
		log.info("==========================================");
		log.info(getState() + " Home policy is created: " + policyNumber);
		log.info("==========================================");

	}

	// DD1-20
	@Test(priority = 2)
	public void TC_Bill_DD1() {

		BillingAndPayments.generateBillAndVerify(dates.get(0), policyNumber);
		EUApp().close();
	}

	// DD1
	@Test(priority = 3)
	public void TC_PayDD1() {

		BillingAndPayments.runRecurringJob(dates.get(0), policyNumber);
		EUApp().close();
	}

	// DD1
	@Test(priority = 4)
	public void TC_EndorseByRerating() {

		EUApp().open();

		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber);
		PolicySummaryPanel.setActionAndGo(PolicyAction.ENDORSEMENT);

		if(!getState().equals("CA")) {
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Date").setValue(dates.get(0).toString());
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Reason").setValue(Endorsement.ENDORSEMENT_VALUE.get(), WaitMode.AJAX);
			EditorPanel.ok();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
			ReportsTabEditorPanel.reorderReports();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_ENDORSEMENT.get());

			EndorsementTabEditorPanel.verifyEndorsementOptional(EndorsementForms.HS_09_04);
			EndorsementTabEditorPanel.addEndorsementForm(EndorsementForms.HS_09_04);
			EndorsementTabEditorPanel.quoteEndorsementHS0904.getControl("Is this an extension of a prior Structural Alteration Coverage endorsement?").setValue("Yes");
			EndorsementTabEditorPanel.quoteEndorsementHS0904.getControl("Reason for extension").setValue("Test");
			EndorsementTabEditorPanel.btnSaveForm.click();

			EndorsementTabEditorPanel.verifyEndorsementIncluded(EndorsementForms.HS_09_04);
			EndorsementTabEditorPanel.continueBtn();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
			PremiumsAndCoveragesTabEditorPanel.rate();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
			BindTabEditorPanel.purchase();
			if(QuotesPanel.tbError.isPresent()) {
				BindTabEditorPanel.overrideAllRules("Term", "Other");
				BindTabEditorPanel.purchase();
			}
			PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.ACTIVE);

		}

	}

	// DD1+NOC
	@Test(priority = 5)
	public void TC_SetManualCancelNotice() {

		cancelNoticeDate = TimePointsHelper.getCancellationNoticeDate(installmentDD1);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(cancelNoticeDate, null);

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);
		CancellationActions.setAndVerifyCancelNoticeManual(null, policyNumber, PolicyActionMeta.Cancellation.MATERIAL_MISREPRESENTATION.get());

		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);
		String fileSuffix_AH61XX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, XPathInfo.POLICY_CANCELLATION, true);

		// CH_CP_GD-87_1_V1
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AH61XX.getIDInXML(), policyNumber, fileSuffix_AH61XX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AH61XX is not generated", true);
		}
	}

	@Test(priority = 6)
	public void TC_RemoveCancelNotice() {

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);
		CancellationActions.removeCancelNotice(policyNumber);
	}

	// DD2-20
	@Test(priority = 7)
	public void TC_EndorsementDD2_20() {

		DateTime date_DD2_20 = TimePointsHelper.getBillGenerationDate(dates.get(1));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(date_DD2_20, null);
		EUApp().open();

		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber);
		PolicySummaryPanel.setActionAndGo(PolicyAction.ENDORSEMENT);

		if(!getState().equals("CA")) {
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Date").setValue(date_DD2_20.toString());
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Reason").setValue(Endorsement.ENDORSEMENT_VALUE.get(), WaitMode.AJAX);
			EditorPanel.ok();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
			PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage E - Personal Liability Each Occurrence").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$2,000,000");
			wait(3000);
			PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage F - Medical Payments to Others").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$25,000");
			wait(3000);
			PremiumsAndCoveragesTabEditorPanel.getCoverageLimitCell(BundleMeta.HssBundleControls.DEDUCTIBLE.get()).controls.comboBoxes.get(1, WaitMode.AJAX).setValueByIndex("0");

			PremiumsAndCoveragesTabEditorPanel.rate();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
			BindTabEditorPanel.purchase();
			PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.ACTIVE);
		}

	}

	@Test(priority = 8)
	public void TC_Bill_DD2() {

		BillingAndPayments.generateBillAndVerify(dates.get(1), policyNumber);
		EUApp().close();
	}

	@Test(priority = 9)
	public void TC_Cancellation_InsuredRequest() {

		canTransactionDateIReq1 = TimePointsHelper.getBillGenerationDate(dates.get(1));
		cancellationEffDateIReq1 = TimePointsHelper.getBillDueDate(dates.get(1));

		LinkedHashMap<LoginFields, String> valuesG36 = new LinkedHashMap<LoginFields, String>();
		valuesG36.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.G36.get());
		valuesG36.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		valuesG36.put(LoginFields.STATES, getState());
		EUApp().open(valuesG36);

		CancellationActions.setAndVerifyManualCancellation(cancellationEffDateIReq1, policyNumber, PolicyActionMeta.Cancellation.INSURED_REQUEST_REASON.get());
		PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.CANCELLATION_PENDING);

		TopPanel.navigateTo(TopPanelTabs.BILLING);

		HashMap<BillColumns, String> values = new HashMap<BillingMeta.BillColumns, String>();
		values.put(BillColumns.DATE, canTransactionDateIReq1.toString(DateTime.MM_DD_YYYY));
		values.put(BillColumns.TYPE, BillingTabMeta.BillsAndStatements.CANCELLATION.get());
		// This is the amount which needs to be collected from the customer. As the policy is on Autopay on running recurring payment job this amount will be deducted from customer account
		amtReceivedOnAutopayWithdrawl = new Dollar(BillingTabHelper.getBillRow(values).getCell(BillColumns.TOTAL_DUE.get()).getValue());
		EUApp().close();
	}

	// (DD1+NOC)+C2
	// @Test(priority = 10)
	public void TC_EndorsementafterAutopay() {

		cancelDateC1 = TimePointsHelper.getCancellationDate(installmentDD1);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(cancelDateC1, null);
		LinkedHashMap<LoginFields, String> valuesG36 = new LinkedHashMap<LoginFields, String>();
		valuesG36.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		// valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.G36.get());
		valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		valuesG36.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		valuesG36.put(LoginFields.STATES, getState());
		EUApp().open(valuesG36);

		CommonPolicyActions.performEndorsement(cancelDateC1, Endorsement.ENDORSEMENT_VALUE.get(), policyNumber);
		// CH_VP_14800_01_V3
		PolicyTabHelper.verifyEndorsementIsCreated();
		EUApp().close();
	}

	// DD2
	@Test(priority = 11)
	public void TC_ValidateAutopayWithdrawl() {

		CommonUtilityFunctions.shiftTimeAndExecuteJob(dates.get(1), Jobs.recurringPaymentsJob);
		EUApp().open();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber);
		HashMap<PaymentsColumns, String> values = new HashMap<BillingMeta.PaymentsColumns, String>();
		values.put(PaymentsColumns.TRANSACTION_DATE, cancellationEffDateIReq1.toString(DateTime.MM_DD_YYYY));
		values.put(PaymentsColumns.AMOUNT, "(" + amtReceivedOnAutopayWithdrawl.toString() + ")");
		values.put(PaymentsColumns.TYPE, BillingTabMeta.TransactionType.PAYMENT.get());
		values.put(PaymentsColumns.SUBTYPE_REASON, BillingTabMeta.PaymentOtherTransactions.RECURRING_PAYMENT.get());
		// Autopay withdrawal request for the balance Earned Premium is triggered
		CustomAssert.assertTrue("CH_1", BillingTabHelper.getPaymentsRow(values).isPresent());
	}

	@Test(priority = 12)
	public void TC_PolicyStatus() {

		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.changeCancellationPendingPoliciesStatus);

		EUApp().open();
		CancellationActions.verifyAutomaticCancellation(policyNumber);
		EUApp().close();

	}

	// DD3-20
	@Test(priority = 13)
	public void TC_ReinstatementWithoutLapse_Cancellation() {

		DateTime reinsEffDate = TimePointsHelper.getBillGenerationDate(dates.get(2));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(reinsEffDate, null);

		EUApp().open();
		ReinstatementActions.reinstatePolicyWithoutLapse(reinsEffDate, cancellationEffDateIReq1, policyNumber);
		BillingSummaryPanel.lnkPolicyNumber.click();

		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);
		String fileSuffix_AHCWXX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, XPathInfo.POLICY_CANCELLATION_RESCISSION_REINSTATEMENT, true);

		// CH_VP_19361_5_V4
		try {
			DocGenHelper.verifyDocumentsGenerated("AHCWXX", policyNumber, fileSuffix_AHCWXX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AHCWXX is not generated", true);
		}

		LinkedHashMap<LoginFields, String> valuesG36 = new LinkedHashMap<LoginFields, String>();
		valuesG36.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.G36.get());
		valuesG36.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		valuesG36.put(LoginFields.STATES, getState());
		EUApp().open(valuesG36);
		CancellationActions.setAndVerifyManualCancellation(reinsEffDate, policyNumber, PolicyActionMeta.Cancellation.INSURED_REQUEST_REASON.get());
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.policyStatusUpdateJob);
		CancellationActions.verifyAutomaticCancellation(policyNumber);
	}

	// DD3
	@Test(priority = 14)
	public void TC_ReinstatementWithLapse() {

		DateTime reinsWithLapseEffDate = TimePointsHelper.getBillDueDate(dates.get(2));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(reinsWithLapseEffDate, null);

		EUApp().open();
		ReinstatementActions.reinstatePolicyWithLapse(reinsWithLapseEffDate, reinsWithLapseEffDate, false, policyNumber);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);
		String fileSuffix_AH62XX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, XPathInfo.POLICY_CANCELLATION_RESCISSION_REINSTATEMENT, true);

		// CH_VP_19353_3_V5
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AH62XX.getIDInXML(), policyNumber, fileSuffix_AH62XX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AH62XX is not generated", true);
		}
	}

	@Test(priority = 15)
	public void TC_GenerateBill4() {

		BillingAndPayments.generateBillAndVerify(dates.get(3), policyNumber);
	}

	@Test(priority = 16)
	public void TC_PayBill4() {

		BillingAndPayments.runRecurringJob(dates.get(3), policyNumber);
	}

	@Test(priority = 17)
	public void TC_GenerateBill5() {

		BillingAndPayments.generateBillAndVerify(dates.get(4), policyNumber);

	}

	@Test(priority = 18)
	public void TC_PayBill5() {

		BillingAndPayments.runRecurringJob(dates.get(4), policyNumber);
	}

	@Test(priority = 19)
	public void TC_GenerateBill6() {

		BillingAndPayments.generateBillAndVerify(dates.get(5), policyNumber);

	}

	@Test(priority = 20)
	public void TC_PayBill6() {

		BillingAndPayments.runRecurringJob(dates.get(5), policyNumber);
	}

	@Test(priority = 21)
	public void TC_GenerateBill7() {

		BillingAndPayments.generateBillAndVerify(dates.get(6), policyNumber);

	}

	@Test(priority = 22)
	public void TC_PayBill7() {

		BillingAndPayments.runRecurringJob(dates.get(6), policyNumber);
	}

	@Test(priority = 23)
	public void TC_GenerateBill8() {

		BillingAndPayments.generateBillAndVerify(dates.get(7), policyNumber);

	}

	@Test(priority = 24)
	public void TC_PayBill8() {

		BillingAndPayments.runRecurringJob(dates.get(7), policyNumber);
	}

	@Test(priority = 25)
	public void TC_GenerateBill9() {

		BillingAndPayments.generateBillAndVerify(dates.get(8), policyNumber);

	}

	@Test(priority = 26)
	public void TC_PayBill9() {

		BillingAndPayments.runRecurringJob(dates.get(8), policyNumber);
	}

	@Test(priority = 27)
	public void TC_GenerateBill10() {

		BillingAndPayments.generateBillAndVerify(dates.get(9), policyNumber);

	}

	@Test(priority = 28)
	public void TC_R_R1() {

		DateTime renewDate73 = TimePointsHelper.getRenewImageGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDate73, null);

		JobUtils.executeJob(Jobs.renewalOfferGenerationPart1);
		HttpStub.executeAllBatches();
		JobUtils.executeJob(Jobs.renewalOfferGenerationPart2);

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		// CH_VP_15949_01_V1
		PolicySummaryPanel.verifyPolicyStatus(PolicyStatus.ACTIVE);

		// CH_VP_#L
		// CH_VP_15949_04_V2
		PolicyTabHelper.verifyAutomatedRenewalGenerated(renewDate73);
		PolicySummaryPanel.openRenewals();

		// CH_VP_15975_01_V3
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.GATHERING_INFO);
		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_ENDORSEMENT.get());

		CustomAssert.assertFalse("ERROR:Endorsement Form - HS 09 04 is added to the Policy.CH_VP_15949_02_V4_01", EndorsementTabEditorPanel.isIncludedEndorsement(EndorsementForms.HS_09_04));
		EndorsementTabEditorPanel.verifyEndorsementOptional(EndorsementForms.HS_09_04);

		CustomAssert.assertFalse("ERROR:Endorsement Form - HS 09 88 is added to the Policy.CH_VP_15949_02_V4_02", EndorsementTabEditorPanel.isIncludedEndorsement(EndorsementForms.HS_09_88));
		EndorsementTabEditorPanel.verifyEndorsementOptional(EndorsementForms.HS_09_88);
		TopPanel.btnCancel.click();
	}

	@Test(priority = 29)
	public void TC_R_R2() {

		DateTime renewDate63 = TimePointsHelper.getMembershipRenewBatchOrder(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDate63, null);

		JobUtils.executeJob(Jobs.renewalOfferGenerationPart1); // membershipRenewalBatchOrderJob
		JobUtils.executeJob(Jobs.renewalOfferGenerationPart2); // membershipRenewalBatchRecieveJob

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.openRenewals();
		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());

		CustomAssert.assertEquals("CH_VP_15956_01_V5_01", ReportsTabEditorPanel.tblAAAMembershipReport.getRow(1).getCell("Status").getValue(), "Active");

		String mOrderDate = ReportsTabEditorPanel.tblAAAMembershipReport.getRow(1).getCell("Order Date").getValue();
		String membershipOrderDate = mOrderDate.replace("-", "/");
		CustomAssert.assertTrue("CH_VP_15956_01_V5_02", membershipOrderDate.equals(AutomationUtils.addDaysToCurrentDate(0)));

		TopPanel.btnCancel.click();
	}

	@Test(priority = 30)
	public void TC_PayBill10() {

		BillingAndPayments.runRecurringJob(dates.get(9), policyNumber);
	}

	@Test(priority = 31)
	public void TC_R_R4() {

		DateTime renewR4 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR4.subtractDays(-3), null);

		JobUtils.executeJob(Jobs.renewalOfferGenerationPart1); // membershipRenewalBatchOrderJob
		JobUtils.executeJob(Jobs.renewalOfferGenerationPart2); // membershipRenewalBatchRecieveJob

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.openRenewals();

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
		CustomAssert.assertEquals("CH_VP_15956_01_V6_01", ReportsTabEditorPanel.tblAAAMembershipReport.getRow(1).getCell("Status").getValue(), "Active");
		TopPanel.btnCancel.click();
	}

	@Test(priority = 32)
	public void TC_R_R5() {

		DateTime renewR5_45 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR5_45, Jobs.renewalOfferGenerationPart2);

		LinkedHashMap<LoginFields, String> valuesG36 = new LinkedHashMap<LoginFields, String>();
		valuesG36.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.G36.get());
		valuesG36.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		valuesG36.put(LoginFields.STATES, getState());
		EUApp().open(valuesG36);

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		// CH_VP_15949_05_V6
		PolicySummaryPanel.verifyRenewalLnkIsPresent();
		PolicySummaryPanel.openRenewals();

		// CH_VP_15975_02_V7
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.PREMIUM_CALCULATED);

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		CustomAssert.assertTrue("CH_VP_17726_01_V8", PremiumsAndCoveragesTabEditorPanel.getPolicyPremiumSummary().moreThan(new Dollar("$0.00")));

		String discountValue = PremiumsAndCoveragesTabEditorPanel.tblDiscounts.getRow("Discount Category", QuoteMeta.DiscountCategory.AFFINITY.get()).getCell("Discounts Applied").getValue();
		CustomAssert.assertTrue("ERROR: Membership discount is not present. CH_VP_15997_03_V9", discountValue.contains("AAA Membership"));

		premiumAtR_R5_01 = PremiumsAndCoveragesTabEditorPanel.getPolicyPremiumSummary();

		PremiumsAndCoveragesTabEditorPanel.viewRatingDetails();
		CustomAssert.assertTrue("Loyality Discount is not applied.CH_VP_15997_03_V10", !RatingDetailsDialog.discounts.getValue("Member persistancy").equals("0.0"));

		TopPanel.btnCancel.click();
	}

	@Test(priority = 33)
	public void TC_Endorsement_R_R5() {

		DateTime renewR5_45 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);

		LinkedHashMap<LoginFields, String> values = new LinkedHashMap<LoginFields, String>();
		values.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
		values.put(LoginFields.GROUPS, LoginMeta.UserGroup.E34.get());
		values.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
		values.put(LoginFields.STATES, getState());
		EUApp().open(values);

		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.setActionAndGo(PolicyAction.ENDORSEMENT);

		if(!getState().equals("CA")) {
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Date").setValue(renewR5_45.toString());
			EndorsementEditorPanel.endorsementList.getControl("Endorsement Reason").setValue(Endorsement.ENDORSEMENT_VALUE.get(), WaitMode.AJAX);
			EditorPanel.ok();

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
			PremiumsAndCoveragesTabEditorPanel.rate();

			// privilege to override premium present
			PremiumsAndCoveragesTabEditorPanel.lnkOverridePremium.click();
			PremiumsAndCoveragesTabEditorPanel.overridePremium.fillByPercentAmount(df.format(10.00));

			PremiumsAndCoveragesTabEditorPanel.overridePremium.ok();
			String overrideSuccessMsg = "Original term premium has been overridden.";
			CustomAssert.assertTrue(overrideSuccessMsg + "- msg is absent", PremiumsAndCoveragesTabEditorPanel.lblInfoMsg.getValue().contains(overrideSuccessMsg));

			premiumAtR_R5_02 = PremiumsAndCoveragesTabEditorPanel.getPolicyPremiumSummary();
			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
			BindTabEditorPanel.purchase();
			PolicyTabHelper.verifyEndorsementIsCreated();

			EUApp().close();

			premiumAtR_R5_01 = new Dollar(1186);
			premiumAtR_R5_02 = new Dollar(1270);
			LinkedHashMap<LoginFields, String> valuesG36 = new LinkedHashMap<LoginFields, String>();
			valuesG36.put(LoginFields.USER, LoginMeta.UserDetails.QA_SECURITY.getUserName());
			valuesG36.put(LoginFields.GROUPS, LoginMeta.UserGroup.G36.get());
			valuesG36.put(LoginFields.PASSWORD, LoginMeta.UserDetails.QA_SECURITY.getPassword());
			valuesG36.put(LoginFields.STATES, getState());
			EUApp().open(valuesG36);

			CustomAssert.enableSoftMode();
			SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

			PolicySummaryPanel.openRenewals();

			RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());

			premiumAtR_R5_03 = PremiumsAndCoveragesTabEditorPanel.getPolicyPremiumSummary();

			CustomAssert.assertEquals("CH_VP_16001_1_V11_01", premiumAtR_R5_01, premiumAtR_R5_03);
			CustomAssert.assertTrue("CH_VP_16001_1_V11_02", premiumAtR_R5_01 != premiumAtR_R5_03);

			TopPanel.btnCancel.click();
		}
	}

	@Test(priority = 34)
	public void TC_R_R6() {

		// TODO R-36, time point needed
		DateTime renewR6_36 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR6_36.subtractDays(1), Jobs.renewalOfferGenerationPart2);
		EUApp().open();
		CustomAssert.enableSoftMode();

		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		PolicySummaryPanel.openRenewals();

		RenewalSummaryPanel.setActionAndGo(RenewAction.DATA_GATHERING);
		RenewalSummaryPanel.btnVersionOk.click(WaitMode.AJAX);
		RenewalSummaryPanel.confirmVersionDialog.btnYes.click(WaitMode.PAGE);

		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_ENDORSEMENT.get());
		EndorsementTabEditorPanel.verifyEndorsementOptional(EndorsementForms.HS_04_59);
		EndorsementTabEditorPanel.fillEndorsementTab("HS0459_Endorsement1");

		// CH_VP_#L. At R-R6
		EndorsementTabEditorPanel.verifyEndorsementIncluded(EndorsementForms.HS_04_59);

		EndorsementTabEditorPanel.continueBtn();
		PremiumsAndCoveragesTabEditorPanel.rate();

		QuoteEditorPanel.navigateTo(QuoteHSSTabs.BIND.get());
		BindTabEditorPanel.saveAndExitRenewal();
	}

	@Test(priority = 35)
	public void TC_R_R7() {

		DateTime renewR7_35 = TimePointsHelper.getRenewOfferGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR7_35, Jobs.renewalOfferGenerationPart2);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		PolicySummaryPanel.openRenewals();

		// CH_VP_15975_03_V12
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.PROPOSED);

		// CH_VP_17410_04_V13
		String fileSuffix_HS0459 = DocGenHelper.downloadXMLToLocalFolder(policyNumber, docgen.DocGenMeta.XPathInfo.POLICY_RENEWAL, true);
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.HS0459.getIDInXML(), policyNumber, fileSuffix_HS0459, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("HS 04 59 is not generated", true);
		}

	}

	@Test(priority = 37)
	public void TC_R_R9() {

		DateTime renewR9_20 = TimePointsHelper.getBillGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR9_20, Jobs.aaaRenewalNoticeBillAsyncJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		// CH_VP_20460_02_V14_a_b_c_d
		BillingTabHelper.verifyRenewPremiumNotice(expirationDate);

		Dollar pastDue = new Dollar(BillingSummaryPanel.tblBillingGeneralInformation.getRow(1).getCell(GeneralInfoColumns.PAST_DUE.get()).getValue());
		Dollar totalDue = new Dollar(BillingSummaryPanel.tblBillingGeneralInformation.getRow(1).getCell(GeneralInfoColumns.TOTAL_DUE.get()).getValue());

		Dollar renewalPremiumAmt = new Dollar(BillingSummaryPanel.tblPaymentsAndOtherTransactions.getRow("Subtype/Reason->Renewal - Policy Renewal Proposal").getCell(PaymentsColumns.AMOUNT.get()).getValue());

		CustomAssert.assertEquals("CH_VP_20460_02_V14_e", pastDue, new Dollar(0));
		CustomAssert.assertEquals("CH_VP_20460_02_V14_f", totalDue, renewalPremiumAmt.add(BillingTabHelper.getFeeValue(TimePointsHelper.getBillGenerationDate(expirationDate))));

		// CH_VP_20460_02_V15
		BillingTabHelper.verifyFeeTransactionGenerated(renewR9_20);

		// CH_VP_20364_02_V16_01
		BillingSummaryPanel.tblBillsAndStatements.getRow(BillColumns.DATE.get() + "->" + expirationDate.toString("MM/dd/yyyy")).getCell(BillColumns.TYPE.get()).verify.value("Bill");
		CustomAssert.assertTrue("CH_VP_20364_02_V16_02", BillingSummaryPanel.tblBillsAndStatements.getRow(BillColumns.DATE.get() + "->" + expirationDate.toString("MM/dd/yyyy")).isPresent());

		CustomAssert.assertAll();

		// CH_VP_20364_01_V17
		String fileSuffix_AHR1XX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, docgen.DocGenMeta.XPathInfo.POLICY_RENEWAL, true);
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AHR1XX.getIDInXML(), policyNumber, fileSuffix_AHR1XX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AHR1XX is not generated", true);
		}

	}

	@Test(priority = 38)
	public void TC_R_R10() {

		CommonUtilityFunctions.shiftTimeAndExecuteJob(expirationDate.subtractDays(10), null);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		String minDueR_10 = BillingSummaryPanel.tblBillsAndStatements.getRow(1).getCell(BillColumns.MINIMUM_DUE.get()).getValue();
		amtToPayR_10 = new Dollar(minDueR_10).add(new Dollar(100));
		BillingSummaryPanel.btnAcceptPayment.click();
		AcceptPaymentEditorPanel.fillAcceptPaymentTab("Cash", amtToPayR_10.toString());
		EditorPanel.btnOk.click();

		DateTime dateR_R10 = expirationDate.subtractDays(10);

		HashMap<PaymentsColumns, String> transactionverificationCreditAmt = new HashMap<BillingMeta.PaymentsColumns, String>();
		transactionverificationCreditAmt.put(PaymentsColumns.TRANSACTION_DATE, dateR_R10.toString(DateTime.MM_DD_YYYY));
		transactionverificationCreditAmt.put(PaymentsColumns.TYPE, BillingTabMeta.Payment.PAYMENT.get());
		transactionverificationCreditAmt.put(PaymentsColumns.AMOUNT, "(" + amtToPayR_10 + ")");
		transactionverificationCreditAmt.put(PaymentsColumns.SUBTYPE_REASON, BillingTabMeta.Payment.MANUAL_PAYMENT.get());
		BillingTabHelper.getPaymentsRow(transactionverificationCreditAmt).verify.present();

		BillingTabHelper.verifyPolicyStatus(expirationDate, PolicyStatus.PENDING);
	}

	@Test(priority = 39)
	public void TC_R_1_Update_PolicyStatus() {

		DateTime renewDatePlus1 = TimePointsHelper.getUpdatePolicyStatusDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDatePlus1, Jobs.policyStatusUpdateJob);
		JobUtils.executeJob(Jobs.aaaDocGenBatchJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber);
		BillingSummaryPanel.showPriorTerms();

		// CH_VP_15975_06_V19
		BillingTabHelper.verifyPolicyStatus(effectiveDate, PolicyStatus.EXPIRED);
		// CH_VP_#L
		BillingTabHelper.verifyPolicyStatus(expirationDate, PolicyStatus.ACTIVE);

		Dollar prepaidAmount = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow(1).getCell(BillingMeta.AccountPoliciesColumns.PREPAID.get()).getValue().replace("(", "").replace(")", ""));
		Dollar totalDue_PolicyActive = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Active").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_DUE.get()).getValue());
		Dollar totalPaid_PolicyActive = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Active").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());
		Dollar BillableAmt_PolicyActive = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Active").getCell(BillingMeta.AccountPoliciesColumns.BILLABLE_AMOUNT.get()).getValue());

		Dollar totalDue_PolicyExpired = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Expired").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_DUE.get()).getValue());
		Dollar totalPaid_PolicyExpired = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Expired").getCell(BillingMeta.AccountPoliciesColumns.TOTAL_PAID.get()).getValue());
		Dollar BillableAmt_PolicyExpired = new Dollar(BillingSummaryPanel.tblBillingAccountPolicies.getRow("Policy Status->Policy Expired").getCell(BillingMeta.AccountPoliciesColumns.BILLABLE_AMOUNT.get()).getValue());

		CustomAssert.assertEquals("CH_VP_16276_3_V20", prepaidAmount, amtToPayR_10);

		CustomAssert.assertEquals("CH_VP_16276_3_V21_01", totalDue_PolicyActive, BillableAmt_PolicyActive.subtract(amtToPayR_10));
		CustomAssert.assertEquals("CH_VP_16276_3_V21_02", totalPaid_PolicyActive, amtToPayR_10);
		CustomAssert.assertEquals("CH_VP_16276_3_V21_03", totalDue_PolicyExpired, new Dollar(0));
		CustomAssert.assertEquals("CH_VP_16276_3_V21_04", totalPaid_PolicyExpired, BillableAmt_PolicyExpired);
		CustomAssert.assertAll();
	}
}
