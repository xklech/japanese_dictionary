package cz.muni.fi.japanesedictionary.database;

import java.util.List;

public class Translation {
	private List<String> japanese;
	private List<String> english;
	private List<String> french;
	private List<String> dutch;
	private List<String> german;
	public List<String> getJapanese() {
		return japanese;
	}
	public void setJapanese(String japanese) {
		this.japanese.add(japanese);
	}
	public List<String> getEnglish() {
		return english;
	}
	public void setEnglish(String english) {
		this.english.add(english);
	}
	public List<String> getFrench() {
		return french;
	}
	public void setFrench(String french) {
		this.french.add(french);
	}
	public List<String> getDutch() {
		return dutch;
	}
	public void setDutch(String dutch) {
		this.dutch.add(dutch);
	}
	public List<String> getGerman() {
		return german;
	}
	public void setGerman(String german) {
		this.german.add(german);
	}
	
	
	
	
}
