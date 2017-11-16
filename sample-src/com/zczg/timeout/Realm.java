package com.zczg.timeout;

public class Realm {
	
	private String realmId;
	private String serverIp;
	private String name;
	
	public Realm() {
		
	}
	
	public Realm(String realmId, String serverIp, String name) {
		this.realmId = realmId;
		this.serverIp = serverIp;
		this.name = name;
	}
	
	public String getRealmId() {
		return realmId;
	}
	public void setRealmId(String realmId) {
		this.realmId = realmId;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Realm [realmId = " + realmId + ", serverIp = " + serverIp + ", name = " + name + "]";
	}
}
