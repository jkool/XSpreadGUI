package xspread.impl;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TermLists {

	public final static Map<String, String> defaults = new HashMap<String, String>();
	public final static BiMap<String, String> fieldLookup = HashBiMap.create();

	public TermLists() {
		fieldLookup.put("speciesList", "species");
		fieldLookup.put("presenceFiles", "presence files");
		fieldLookup.put("ageFiles", "age files");
		fieldLookup.put("habitatFiles", "habitat files");
		fieldLookup.put("referenceFiles", "reference files");
		fieldLookup.put("managementFiles", "management files");
		fieldLookup.put("distances", "dispersal distances");
		fieldLookup.put("rates", "dispersal rates");
		fieldLookup.put("directions", "direction kernels");
		fieldLookup.put("age_stage", "age at stage information");
		fieldLookup.put("p_detection", "probability of detection at stage");
		fieldLookup.put("groundControlCost", "ground control cost");
		fieldLookup.put("groundControlLabour", "ground control labour");
		fieldLookup.put("waitTime", "wait time before detection");
	}
	
	public String in2out(String term){
		return fieldLookup.get(term);
	}
	
	public String out2in(String term){
		return fieldLookup.inverse().get(term);
	}

}
