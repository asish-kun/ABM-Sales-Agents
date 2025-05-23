package view;

/*
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;*/

/**
 * Class to allocate a bean for loading database with the stream of leads
 * 
 * @author mchica
 * @date 2024/05/21
 * @place Granada
 */

public class LeadData {		

	private String LeadID;
	private String Amount;
	private String BusinessModel;
	private String WeekCreated;
	private String WeekClosed;
	private String WeekLost;
	private String WeekWon;
	private String WeekUnlikely0;
	private String WeekPipeline20;
	private String WeekBestCase40;
	private String WeekCommit80;
	private String CertaintyForConv;
	private String ConvertedLead;

	//Asish: NEW FIELDS FOR NEURAL NETWORK INPUT FEATURES

	private String weeksElapsed;          // weeks_elapsed_since_created
	private String weeksDiffPrior;        // weeks_diff_prior
	private String realLeadSum;           // real_lead_sum
	private String processWeek;           // process_week
	private String mktgGen;               // mktg_gen (marketing-generated flag)
	private String sector;                // sector (raw string)
	private String businessModelEncoded;  // businessmodel_lead (encoded as float)

	// Getters and setters for new fields
	public String getWeeksElapsed() {
		return weeksElapsed;
	}

	public void setWeeksElapsed(String weeksElapsed) {
		this.weeksElapsed = weeksElapsed;
	}

	public String getWeeksDiffPrior() {
		return weeksDiffPrior;
	}

	public void setWeeksDiffPrior(String weeksDiffPrior) {
		this.weeksDiffPrior = weeksDiffPrior;
	}

	public String getRealLeadSum() {
		return realLeadSum;
	}

	public void setRealLeadSum(String realLeadSum) {
		this.realLeadSum = realLeadSum;
	}

	public String getProcessWeek() {
		return processWeek;
	}

	public void setProcessWeek(String processWeek) {
		this.processWeek = processWeek;
	}

	public String getMktgGen() {
		return mktgGen;
	}

	public void setMktgGen(String mktgGen) {
		this.mktgGen = mktgGen;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getBusinessModelEncoded() {
		return businessModelEncoded;
	}

	public void setBusinessModelEncoded(String businessModelEncoded) {
		this.businessModelEncoded = businessModelEncoded;
	}

	// getters and setters of the attributes
	public String getLeadID() {
		return LeadID;
	}

	public void setLeadID(String leadID) {
		LeadID = leadID;
	}

	public String getAmount() {
		return Amount;
	}

	public void setAmount(String amount) {
		Amount = amount;
	}

	public String getBusinessModel() {
		return BusinessModel;
	}

	public void setBusinessModel(String businessModel) {
		BusinessModel = businessModel;
	}

	public String getWeekCreated() {
		return WeekCreated;
	}

	public void setWeekCreated(String weekCreated) {
		WeekCreated = weekCreated;
	}

	public String getWeekClosed() {
		return WeekClosed;
	}

	public void setWeekClosed(String weekClosed) {
		WeekClosed = weekClosed;
	}

	public String getWeekLost() {
		return WeekLost;
	}

	public void setWeekLost(String weekLost) {
		WeekLost = weekLost;
	}

	public String getWeekWon() {
		return WeekWon;
	}

	public void setWeekWon(String weekWon) {
		WeekWon = weekWon;
	}

	public String getWeekUnlikely0() {
		return WeekUnlikely0;
	}

	public void setWeekUnlikely0(String weekUnlikely0) {
		WeekUnlikely0 = weekUnlikely0;
	}

	public String getWeekPipeline20() {
		return WeekPipeline20;
	}

	public void setWeekPipeline20(String weekPipeline20) {
		WeekPipeline20 = weekPipeline20;
	}

	public String getWeekBestCase40() {
		return WeekBestCase40;
	}

	public void setWeekBestCase40(String weekBestCase40) {
		WeekBestCase40 = weekBestCase40;
	}

	public String getWeekCommit80() {
		return WeekCommit80;
	}

	public void setWeekCommit80(String weekCommit80) {
		WeekCommit80 = weekCommit80;
	}

	public String getCertaintyForConv() {
		return CertaintyForConv;
	}

	public void setCertaintyForConv(String _certaintyForConv) {
		CertaintyForConv = _certaintyForConv;
	}	
	
	public String getConvertedLead() {
		return ConvertedLead;
	}

	public void setConvertedLead (String _convertedLead) {
		ConvertedLead = _convertedLead;
	}

	/*public void setECByDistance (String distance, float _EC) {
		EC.put(distance, _EC);
	}	

	public float getECByDistance (String distance) {
		return EC.get(distance);
	}	

	public void setECAllDistances (FastList <String>distances, FloatArrayList _EC) {
		
		for (int i = 0; i < distances.size(); i++) {
			this.EC.put(distances.get(i), Float.valueOf(_EC.get(i)));
		}
		
	}*/	
	
	@Override
	public String toString() {
		return "{" + LeadID + "::" + Amount + "::" + BusinessModel +
				", weeksElapsed='" + weeksElapsed + '\'' +
				", weeksDiffPrior='" + weeksDiffPrior + '\'' +
				", realLeadSum='" + realLeadSum + '\'' +
				", processWeek='" + processWeek + '\'' +
				", mktgGen='" + mktgGen + '\'' +
				", sector='" + sector + '\'' +
				", businessModelEncoded='" + businessModelEncoded + '\'' +
				"::" + WeekCreated + "::" + WeekClosed  + "::" + WeekLost
				+ "::" + WeekWon + "::" + WeekUnlikely0 + "::" + WeekPipeline20 + WeekBestCase40 + "::" + WeekCommit80 + "::" + CertaintyForConv + "::" + ConvertedLead +"}";
	}
		
}
