package cz.muni.fi.japanesedictionary.main;

import java.util.ArrayList;
import java.util.List;

public class Translation {
	private List<String> jap_keb;
	private List<String> jap_reb;
	private List<List<String>> english;
	private List<List<String>> french;
	private List<List<String>> dutch;
	private List<List<String>> german;
	
	@Override
	public String toString() {
		return "Translation [jap_keb=" + jap_keb + ", jap_reb=" + jap_reb
				+ ", english=" + english + ", french=" + french + ", dutch="
				+ dutch + ", german=" + german + "]";
	}

	public Translation(){
		jap_keb = new ArrayList<String>();
		jap_reb = new ArrayList<String>();
		english = new ArrayList<List<String>>();
		french = new ArrayList<List<String>>();
		dutch = new ArrayList<List<String>>();
		german = new ArrayList<List<String>>();
	}
	
	public void addJapKeb(String keb){
		jap_keb.add(keb);
	}
	
	public void addJapReb(String reb){
		jap_reb.add(reb);
	}
	
	public void addEnglishSense(List<String> sense){
		english.add(sense);
	}
	public void addFrenchSense(List<String> sense){
		french.add(sense);
	}
	public void addDutchSense(List<String> sense){
		dutch.add(sense);
	}
	public void addGermanSense(List<String> sense){
		german.add(sense);
	}
	
	public List<String> getJapaneseKeb(){
		return jap_keb.isEmpty()?null:jap_keb;
	}
	
	public List<String> getJapaneseReb(){
		return jap_reb.isEmpty()?null:jap_reb;
	}
	
	public List<List<String>> getEnglishSense(){
		return english.isEmpty()?null:english;
	}
	
	public List<List<String>> getFrenchSense(){
		return french.isEmpty()?null:french;
	}
	
	public List<List<String>> getDutchSense(){
		return dutch.isEmpty()?null:dutch;
	}
	
	public List<List<String>> getGermanSense(){
		return german.isEmpty()?null:german;
	}
	
	public String getEnglishFirstSense(){
		if(english.isEmpty()){
			return null;
		}
		List<String> sense= english.get(0);
		String ret="";
		for(String trans: sense){
			if(ret.length()>0){
				ret.concat(", ");
			}
			ret.concat(trans);
		}
		return ret;
	}
}
