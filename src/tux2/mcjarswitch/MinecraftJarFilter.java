package tux2.mcjarswitch;

import java.io.File;

import javax.swing.filechooser.*;

public class MinecraftJarFilter extends FileFilter {

	@Override
	public boolean accept(File arg0) {
		if(arg0.isDirectory()) {
			return true;
		}
		String extension = "";
		
		if(arg0.getPath().lastIndexOf('.') >0) {
			extension = arg0.getPath().substring(
					arg0.getPath().lastIndexOf('.') + 1).toLowerCase();
			if(extension !="") {
				return extension.equals("jar") && arg0.isFile();
			}else {
				return false;
			}
		}
		// TODO Auto-generated method stub
		return false;
	}

	public String getDescription() {
		return "Java Files";
	}

}
