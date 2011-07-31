package tux2.mcjarswitch;

import java.io.Serializable;

public class JarProperties implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -665339769190495328L;
	String md5hash = "";
	String minecraftversion = "";
	String mods = "";
	String notes = "";
	String zipfilelocation = "";
	String name = "";
	
	public JarProperties(String name, String md5hash, String location) {
		this.name = name;
		this.md5hash = md5hash;
		zipfilelocation = location;
	}
	
	public JarProperties(String name, String md5hash, String location, String mcversion) {
		this.name = name;
		this.md5hash = md5hash;
		minecraftversion = mcversion;
		zipfilelocation = location;
	}
	
	public JarProperties(String name, String md5hash, String location, String mcversion, String modlist) {
		this.name = name;
		this.md5hash = md5hash;
		minecraftversion = mcversion;
		mods = modlist;
		zipfilelocation = location;
	}
	
	public JarProperties(String name, String md5hash, String location, String mcversion, String modlist, String notes) {
		this.name = name;
		this.md5hash = md5hash;
		minecraftversion = mcversion;
		mods = modlist;
		this.notes = notes;
		zipfilelocation = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getZipLocation() {
		return zipfilelocation;
	}

	public void setZipLocation(String fileslocation) {
		this.zipfilelocation = fileslocation;
	}

	public String getMd5hash() {
		return md5hash;
	}

	public void setMd5hash(String md5hash) {
		this.md5hash = md5hash;
	}

	public String getMinecraftversion() {
		return minecraftversion;
	}

	public void setMinecraftversion(String minecraftversion) {
		this.minecraftversion = minecraftversion;
	}

	public String getMods() {
		return mods;
	}

	public void setMods(String mods) {
		this.mods = mods;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
