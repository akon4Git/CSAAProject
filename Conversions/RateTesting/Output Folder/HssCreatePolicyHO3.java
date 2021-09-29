package demorating.hss;

import helpers.CustomerHelper;
import helpers.RatingHelper;

import org.testng.annotations.Test;

import toolkit.ratingtest.RatingToolkitManager;
import toolkit.verification.CustomAssert;
import ui.app.metadata.PolicyMeta;
import ui.app.metadata.TabNames;
import ui.app.model.panel.edit.EditorPanel;
import ui.app.model.panel.edit.quote.BindTabEditorPanel;
import ui.app.model.panel.edit.quote.PaymentEditorPanel;
import ui.app.model.panel.edit.quote.hss.ApplicantTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.DocumentsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.EndorsementTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.GeneralTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.MortgageeTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PremiumsAndCoveragesTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ProductOfferingTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.PropertyInfoTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.ReportsTabEditorPanel;
import ui.app.model.panel.edit.quote.hss.UnderwritingApprovalTabEditorPanel;
import ui.app.model.panel.summary.PolicySummaryPanel;
import utilities.ScreenshotUtils;
import base.BaseTest;

public class HssCreatePolicyHO3 extends BaseTest {

	/**
	 * SC1 Policy creation with specific data: Reports tab - override Insurance
	 * score report Property Info tab - change PPC Product Offering tab - select
	 * bundle Check rating
	 **/

	@Test
	public void HO3_SC1() {

	EUApp().open();
		CustomerHelper.openCustomer();

		// String policyNumber = RatingHelper.createHssPolicy(getDataSet());

				RatingHelper.initiateHssQuoteCreation();
		
		GeneralTabEditorPanel.fillGeneralTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("GeneralTab");
		GeneralTabEditorPanel.next();
		
		//Save Quote
		EditorPanel.save();

		ApplicantTabEditorPanel.fillApplicantTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("ApplicantTab");
		ApplicantTabEditorPanel.continueBtn();

		PropertyInfoTabEditorPanel.fillPropertyInfoTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("PropertyInfoTab");
		PropertyInfoTabEditorPanel.continueBtn();

		ReportsTabEditorPanel.orderAllReports();	
		//Reports tab - override Insurance score report 
		ReportsTabEditorPanel.fillReportsTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("ReportsTab");
		//ReportsTabEditorPanel.continueBtn();

		/*//Property Info - change PPC values
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PROPERTY_INFO.get(), 150000);
		PropertyInfoTabEditorPanel.fillPropertyInfoPPCTab(dataSet+"_PPC");
		ScreenshotUtils.makeScreenShot("PropertyInfoPPCTab");
*/
		//Product Offering - select bundle
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_PRODUCT_OFFERING.get());
		ProductOfferingTabEditorPanel.fillProductOfferingTab("HO3_Dataset1");	
		ScreenshotUtils.makeScreenShot("ProductOfferingTab");
		ProductOfferingTabEditorPanel.continueBtn();
		
		EndorsementTabEditorPanel.fillEndorsementTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("EndorsementTab");
		EndorsementTabEditorPanel.continueBtn();
		
		EditorPanel.navigateTo(TabNames.QuoteHSSTabs.PREMIUMS_AND_COVERAGES_QUOTE.get());
		PremiumsAndCoveragesTabEditorPanel.fillPremiumAndCoveragesTab("HO3_Dataset1");
		PremiumsAndCoveragesTabEditorPanel.rate();
		ScreenshotUtils.makeScreenShot("PremiumAndCoveragesTab");
		PremiumsAndCoveragesTabEditorPanel.continueBtn();
		
		MortgageeTabEditorPanel.fillMortgageeAndInterestTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("MortgageeAndInterestTab");
		MortgageeTabEditorPanel.continueBtn();
		
		UnderwritingApprovalTabEditorPanel.fillUnderwritingApprovalTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("UnderwritingApprovalTab");
		UnderwritingApprovalTabEditorPanel.continueBtn();
		
		DocumentsTabEditorPanel.fillDocumentsTab("HO3_Dataset1");
		ScreenshotUtils.makeScreenShot("DocumentsTab");
		DocumentsTabEditorPanel.continueBtn();
		
		
	//	EditorPanel.navigateTo(TabNames.QuoteHSSTabs.BIND.get());
		BindTabEditorPanel.purchase();
		PaymentEditorPanel.fillPaymentTab();
		String policyNumber = PolicySummaryPanel.getPolicyNumber();

		PolicySummaryPanel.verifyPolicyStatus(PolicyMeta.PolicyStatus.ACTIVE);

		log.info("==================================================");
		log.info(getState() + " Home policy SC1 with " + getDataSet()
				+ " is created: " + policyNumber);
		log.info("==================================================");

		String bundle = RatingHelper.getBundleHO3Policy(getDataSet());
		log.info("Bundle is " + bundle);

		CustomAssert.assertTrue(RatingToolkitManager.validatePremiums("Q"
				+ policyNumber, "AAA_HO_SS", getState(), "HO3", bundle));

	}

}
