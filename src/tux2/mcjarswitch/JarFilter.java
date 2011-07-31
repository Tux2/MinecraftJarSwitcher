package tux2.mcjarswitch;

import java.io.File;

import javax.swing.filechooser.*;

public class JarFilter extends FileFilter {

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
				return (extension.equals("jar") || extension.equals("exe") || extension.equals("app")) && arg0.isFile();
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
