/**
 * '###############################################################################
'# DESCRIPTION       :	CP100
						Create a policy
						1. Log-in with privilege "Cancel backdated policy 30 to 60 days" 
						2. Select ‘Cancellation’   from the "Move To" drop down and click on "Go".
						3. Enter the  required information on the cancel screen:
						a. Cancel Date = Backdated upto 60 days (eg . DDX-20).
						b. Cancellation Reason = Insured request
						c. Supporting Data =  "Text remarks"
						Click on "ok"
 # Scenario Path     :
'# AUTHOR            :Anusha
'# CREATION DATE     :07/29/2015
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
import helpers.CahHelper;
import helpers.CustomerHelper;
import helpers.FileHelper;
import helpers.HssHelper;
import helpers.PolicyTabHelper;
import helpers.TimePointsHelper;
import http.HttpStub;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.testng.annotations.Test;
import docgen.DocGenHelper;
import docgen.DocGenMeta.XPathInfo;
import regression.UtilityClasses.BillingAndPayments;
import regression.UtilityClasses.CancellationActions;
import regression.UtilityClasses.CommonPolicyActions;
import regression.UtilityClasses.CommonUtilityFunctions;
import regression.UtilityClasses.ReinstatementActions;
import regression.commonMetadata.BillingTabMeta;
import regression.commonMetadata.EndorsementTabMeta.EndorsementForm;
import regression.commonMetadata.PolicyActionMeta;
import regression.commonMetadata.PremiumTabMeta;
import regression.commonMetadata.ReportsTabMeta;
import regression.commonMetadata.PolicyActionMeta.Endorsement;
import toolkit.selenium.controls.WaitMode;
import toolkit.utils.datetime.DateTime;
import toolkit.verification.CustomAssert;
import ui.admin.metadata.Jobs;
import ui.app.metadata.DocumentsMeta.OnDemandDocuments;
import ui.app.metadata.PolicyMeta.PolicyStatus;
import ui.app.metadata.PolicyMeta.RenewAction;
import ui.app.metadata.PolicyMeta.RenewalStatus;
import ui.app.metadata.BillingMeta;
import ui.app.metadata.PolicyMeta;
import ui.app.metadata.QuoteMeta;
import ui.app.metadata.TabNames;
import ui.app.metadata.BillingMeta.BillColumns;
import ui.app.metadata.BillingMeta.PaymentPlan;
import ui.app.metadata.BillingMeta.PaymentsColumns;
import ui.app.metadata.PolicyMeta.PolicyAction;
import ui.app.metadata.QuoteMeta.EndorsementForms;
import ui.app.metadata.SearchMeta.SearchBy;
import ui.app.metadata.SearchMeta.SearchFor;
import ui.app.metadata.TabNames.TopPanelTabs;
import ui.app.model.panel.SearchPanel;
import ui.app.model.panel.TopPanel;
import ui.app.model.panel.edit.EditorPanel;
import ui.app.model.panel.edit.ErrorPanel;
import ui.app.model.panel.edit.billing.AcceptPaymentEditorPanel;
import ui.app.model.panel.edit.billing.AddPaymentMethodEditorPanel;
import ui.app.model.panel.edit.dialogs.RatingDetailsDialog;
import ui.app.model.panel.edit.policy.CancellationEditorPanel;
import ui.app.model.panel.edit.policy.EndorsementEditorPanel;
import ui.app.model.panel.edit.quote.BindTabEditorPanel;
import ui.app.model.panel.edit.quote.PaymentEditorPanel;
import ui.app.model.panel.edit.quote.QuotesPanel;
import ui.app.model.panel.edit.quote.cah.CahPremiumsAndCoveragesTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ApplicantTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.DocumentsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.EndorsementTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.GeneralTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PremiumsAndCoveragesTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PropertyInfoTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ReportsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.UnderwritingApprovalTabEditorPanel;
import ui.app.model.panel.summary.BillingSummaryPanel;
import ui.app.model.panel.summary.EndorsementSummaryPanel;
import ui.app.model.panel.summary.PolicySummaryPanel;
import ui.app.model.panel.summary.RenewalSummaryPanel;
import ui.meta.attributes.cah.CahPremiumMetaData;
import ui.meta.attributes.hss.HssPremiumMetaData;
import ui.meta.attributes.hss.HssPremiumMetaData.HssPremium;
import utilities.Dollar;
import utilities.JobUtils;
import utilities.time.TimeSetterUtil;
import base.BaseTest;

public class CP100_1 extends BaseTest {

	public String policyNumber = "";
	public ArrayList<DateTime> dates = FileHelper.getInstallmentsDueDates(PaymentPlan.MONTHLY_STANDARD);
	public DateTime installmentDD1 = dates.get(0), installmentDD2 = dates.get(1);
	public DateTime cancelNoticeDate, cancellationEffDateIReq1, cancelDateC1, expirationDate;
	public Dollar amtReceivedOnAutopayWithdrawl;
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
			// BindTabEditorPanel.overrideAllRules("Term", "Other");
			BindTabEditorPanel.referForApproval();
			BindTabEditorPanel.purchase();
			BindTabEditorPanel.overridePolicy();
			BindTabEditorPanel.purchase();
		}
		//PaymentEditorPanel.fillPaymentTabWithAutopayEFT();
		
		PaymentEditorPanel.btnAddPaymentMethod.click();
		AddPaymentMethodEditorPanel.addEFTPaymentMethod();
		PaymentEditorPanel.fillAutopaySection();
		PaymentEditorPanel.cbAutopaySelection.setValue("index=1");
		if(PaymentEditorPanel.rgSignatureOnFileIndicator.isPresent()) PaymentEditorPanel.rgSignatureOnFileIndicator.setValue(PremiumTabMeta.Payment.YES.get());
		//Dollar actMinReqDownp = new Dollar(PaymentEditorPanel.tbMinRequiredDownPayment.getValue());
		Dollar minDwnpayment = new Dollar(2);
		PaymentEditorPanel.setChangeMinDownPaymentCB(true);

		PaymentEditorPanel.tbMinRequiredDownPayment.setValue(minDwnpayment.toPlaingString(), WaitMode.AJAX);
		PaymentEditorPanel.fillPaymentTab();
		
		policyNumber = PolicySummaryPanel.getPolicyNumber();
		PolicySummaryPanel.verifyPolicyStatus(PolicyMeta.PolicyStatus.ACTIVE);

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
		EUApp().open();
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

		EUApp().open();
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
				//PremiumsAndCoveragesTabEditorPanel.selectCoverageLimit(dt);
				//PremiumsAndCoveragesTabEditorPanel.tblCoverages.getRow("Description", "Coverage E - Personal Liability Each Occurrence").getCell("Limits ($)").setValue(value);
				PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage E - Personal Liability Each Occurrence").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$2,000,000");
				PremiumsAndCoveragesTabEditorPanel.getCoverageRow("Coverage F - Medical Payments to Others").getCell("Limits ($)").controls.comboBoxes.getFirst().setValueContains("$2,000,000");
				PremiumsAndCoveragesTabEditorPanel.tblCoverages.getRow("Description", "Coverage F - Medical Payments to Others").getCell("Limits ($)").getValue().equals("$1,000");
				PremiumsAndCoveragesTabEditorPanel.tblCoverages.getRow("Description", "Deductible").getCell("Limits ($)").setValue("$1,500", WaitMode.AJAX);
				
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
		EUApp().open();
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
	@Test(priority = 10)
	public void TC_EndorsementafterAutopay() {

		cancelDateC1 = TimePointsHelper.getCancellationDate(installmentDD1);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(cancelDateC1, null);
		EUApp().open();

		CommonPolicyActions.performEndorsement(cancelDateC1, Endorsement.ENDORSEMENT_VALUE.get(), policyNumber);

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

		// CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.policyStatusUpdateJob);
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

		CancellationActions.setAndVerifyManualCancellation(reinsEffDate, policyNumber, PolicyActionMeta.Cancellation.INSURED_REQUEST_REASON.get());
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.policyStatusUpdateJob);
		CancellationActions.verifyAutomaticCancellation(policyNumber);
	}

	// DD3-20
	@Test(priority = 14)
	public void TC_ReinstatementWithLapse() {

		DateTime reinsWithLapseEffDate = TimePointsHelper.getBillDueDate(dates.get(2));
		CommonUtilityFunctions.shiftTimeAndExecuteJob(reinsWithLapseEffDate, null);

		EUApp().open();
		ReinstatementActions.reinstatePolicyWithLapse(reinsWithLapseEffDate, reinsWithLapseEffDate, false, policyNumber);
		// TODO check on Reinstatement fee
		CommonUtilityFunctions.shiftTimeAndExecuteJob(null, Jobs.aaaDocGenBatchJob);
		String fileSuffix_AH62XX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, XPathInfo.POLICY_CANCELLATION_RESCISSION_REINSTATEMENT, true);

		// CH_VP_19353_3_V5
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AH62XX.getIDInXML(), policyNumber, fileSuffix_AH62XX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AH62XX is not generated", true);
		}
	}

	@Test(priority = 8)
	public void TC_GenerateBill4() {

		BillingAndPayments.generateBillAndVerify(dates.get(3), policyNumber);

	}

	@Test(priority = 9)
	public void TC_PayBill4() {

		BillingAndPayments.runRecurringJob(dates.get(3), policyNumber);
	}

	@Test(priority = 10)
	public void TC_GenerateBill5() {

		BillingAndPayments.generateBillAndVerify(dates.get(4), policyNumber);

	}

	@Test(priority = 11)
	public void TC_PayBill5() {

		BillingAndPayments.runRecurringJob(dates.get(4), policyNumber);
	}

	@Test(priority = 12)
	public void TC_GenerateBill6() {

		BillingAndPayments.generateBillAndVerify(dates.get(5), policyNumber);

	}

	@Test(priority = 13)
	public void TC_PayBill6() {

		BillingAndPayments.runRecurringJob(dates.get(5), policyNumber);
	}

	@Test(priority = 14)
	public void TC_GenerateBill7() {

		BillingAndPayments.generateBillAndVerify(dates.get(6), policyNumber);

	}

	@Test(priority = 15)
	public void TC_PayBill7() {

		BillingAndPayments.runRecurringJob(dates.get(6), policyNumber);
	}

	@Test(priority = 16)
	public void TC_GenerateBill8() {

		BillingAndPayments.generateBillAndVerify(dates.get(7), policyNumber);

	}

	@Test(priority = 17)
	public void TC_PayBill8() {

		BillingAndPayments.runRecurringJob(dates.get(7), policyNumber);
	}

	@Test(priority = 18)
	public void TC_GenerateBill9() {

		BillingAndPayments.generateBillAndVerify(dates.get(8), policyNumber);

	}

	@Test(priority = 19)
	public void TC_PayBill9() {

		BillingAndPayments.runRecurringJob(dates.get(8), policyNumber);
	}

	@Test(priority = 20)
	public void TC_GenerateBill10() {

		BillingAndPayments.generateBillAndVerify(dates.get(9), policyNumber);

	}

	// @Test(priority = 21)
	public void TC_R1() {

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

	@Test(priority = 22)
	public void TC_R2() {

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber);
		expirationDate = PolicyTabHelper.getExpirationDate();

		DateTime renewDate63 = TimePointsHelper.getMembershipRenewBatchOrder(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewDate63, null);

		HttpStub.executeAllBatches();

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.openRenewals();

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
		// CH_VP_15956_01(V5)
		ReportsTabEditorPanel.tblAAAMembershipReport.getRow(1).getCell(ReportsTabMeta.PPC.PPC_REPORT.get()).controls.links.get(ReportsTabMeta.PPC.VIEW_REPORT.get(), WaitMode.PAGE).verify.present();

		TopPanel.btnCancel.click();
	}

	@Test(priority = 23)
	public void TC_PayBill10() {

		BillingAndPayments.runRecurringJob(dates.get(9), policyNumber);

	}

	@Test(priority = 24)
	public void TC_R_R4() {

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber);
		expirationDate = PolicyTabHelper.getExpirationDate();

		DateTime renewR4 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR4.addBusinessDays(-3), null);

		HttpStub.executeAllBatches();

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		PolicySummaryPanel.openRenewals();

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.REPORTS.get());
		// CH_VP_15956_01(V5)
		ReportsTabEditorPanel.tblAAAMembershipReport.getRow(1).getCell(ReportsTabMeta.PPC.PPC_REPORT.get()).controls.links.get(ReportsTabMeta.PPC.VIEW_REPORT.get(), WaitMode.PAGE).verify.present();

		TopPanel.btnCancel.click();
	}

	@Test(priority = 25)
	public void TC_R_R5() {

		/*
		 * EUApp().open(); CustomAssert.enableSoftMode(); SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber); expirationDate = PolicyTabHelper.getExpirationDate();
		 */
		DateTime renewR5_45 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR5_45, Jobs.renewalOfferGenerationPart2);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		// CH_VP_15949_05_V6
		PolicySummaryPanel.verifyRenewalLnkIsPresent();
		PolicySummaryPanel.openRenewals();

		// CH_VP_15975_02_V7
		RenewalSummaryPanel.verifyRenewalStatus(RenewalStatus.PREMIUM_CALCULATED);

		RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		CustomAssert.assertTrue(PremiumsAndCoveragesTabEditorPanel.getPolicyPremiumSummary().moreThan(new Dollar("$0.00")));

		String discountValue = PremiumsAndCoveragesTabEditorPanel.tblDiscounts.getRow("Discount Category", QuoteMeta.DiscountCategory.AFFINITY.get()).getCell("Discounts Applied").getValue();
		CustomAssert.assertTrue("ERROR: Membership discount is not present", discountValue.contains("AAA Membership"));

		PremiumsAndCoveragesTabEditorPanel.viewRatingDetails();
		CustomAssert.assertTrue("Loyality Discount is not applied", !RatingDetailsDialog.discounts.getValue("Member persistancy").equals("0.0"));

		TopPanel.btnCancel.click();
	}

	@Test(priority = 26)
	public void TC_Endorsement_R_R5() {

		DateTime renewR5_45 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		EUApp().open();
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

			Dollar overriddenValue = new Dollar(PremiumsAndCoveragesTabEditorPanel.overridePremium.tbFlatAmount.getValue());
			Dollar originalPremium = new Dollar(PremiumsAndCoveragesTabEditorPanel.overridePremium.getOriginalTermPremium());
			String finalPremium = overriddenValue.add(originalPremium).toString();
			String pcAfterOverridden = PremiumsAndCoveragesTabEditorPanel.overridePremium.tbPercentage.getValue();

			PremiumsAndCoveragesTabEditorPanel.overridePremium.ok();
			String overrideSuccessMsg = "Original term premium has been overridden.";
			CustomAssert.assertTrue(overrideSuccessMsg + "- msg is absent", PremiumsAndCoveragesTabEditorPanel.lblInfoMsg.getValue().contains(overrideSuccessMsg));

			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
			BindTabEditorPanel.purchase();
			PolicyTabHelper.verifyEndorsementIsCreated();

			// TODO- need a login step #l, V11
			PolicySummaryPanel.openRenewals();

			RenewalSummaryPanel.setActionAndGo(RenewAction.INQUIRY);
			EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());

			PremiumsAndCoveragesTabEditorPanel.lnkOverridePremium.click();

			Dollar overriddenValue2 = new Dollar(PremiumsAndCoveragesTabEditorPanel.overridePremium.tbFlatAmount.getValue());
			Dollar originalPremium2 = new Dollar(PremiumsAndCoveragesTabEditorPanel.overridePremium.getOriginalTermPremium());
			String finalPremium2 = overriddenValue.add(originalPremium).toString();
			String pcAfterOverridden2 = PremiumsAndCoveragesTabEditorPanel.overridePremium.tbPercentage.getValue();

			TopPanel.btnCancel.click();
		}
	}

	@Test(priority = 27)
	public void TC_R_R6() {

		// R-36, time point needed
		DateTime renewR6_36 = TimePointsHelper.getRenewPreviewGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR6_36.subtractDays(1), Jobs.renewalOfferGenerationPart2);
		EUApp().open();
		CustomAssert.enableSoftMode();

		SearchPanel.search(SearchFor.POLICY, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		PolicySummaryPanel.openRenewals();

		RenewalSummaryPanel.setActionAndGo(RenewAction.DATA_GATHERING);
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_ENDORSEMENT.get());

		EndorsementTabEditorPanel.verifyEndorsementOptional(EndorsementForms.HS_04_59);

		EndorsementTabEditorPanel.addEndorsementForm(EndorsementForms.HS_04_59);
		// TODO- refer F-H-Endorsement-CL-051

		// CH_VP_#L. At R-R6
		EndorsementTabEditorPanel.verifyEndorsementIncluded(EndorsementForms.HS_04_59);
	}

	@Test(priority = 28)
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

	@Test(priority = 29)
	public void TC_R_R9() {

		DateTime renewR9_20 = TimePointsHelper.getBillGenerationDate(expirationDate);
		CommonUtilityFunctions.shiftTimeAndExecuteJob(renewR9_20, Jobs.aaaRenewalNoticeBillAsyncJob);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());
		// TODO V14-16

		// CH_VP_20364_01_V17
		String fileSuffix_AHR1XX = DocGenHelper.downloadXMLToLocalFolder(policyNumber, docgen.DocGenMeta.XPathInfo.POLICY_MISCELLANEOUS, true);
		try {
			DocGenHelper.verifyDocumentsGenerated(OnDemandDocuments.AHR1XX.getIDInXML(), policyNumber, fileSuffix_AHR1XX, true);
		} catch (Exception e) {
			CustomAssert.assertFalse("AHR1XX is not generated", true);
		}

	}

	@Test(priority = 30)
	public void TC_R_R10() {

		CommonUtilityFunctions.shiftTimeAndExecuteJob(expirationDate.subtractDays(10), null);

		EUApp().open();
		CustomAssert.enableSoftMode();
		SearchPanel.search(SearchFor.BILLING, SearchBy.POLICY_QUOTE, policyNumber, "Status", PolicyStatus.ACTIVE.get());

		String minDue = BillingSummaryPanel.tblBillsAndStatements.getRow(1).getCell(BillColumns.MINIMUM_DUE.get()).getValue();
		Dollar amtToPay = new Dollar(minDue).add(new Dollar(100));
		BillingSummaryPanel.btnAcceptPayment.click();
		AcceptPaymentEditorPanel.fillAcceptPaymentTab("Cash", amtToPay.toString());
		EditorPanel.btnOk.click();

		// TODO V18
	}

}
