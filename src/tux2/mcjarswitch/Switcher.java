package tux2.mcjarswitch;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.twmacinta.util.MD5;

public class Switcher extends JFrame implements ListSelectionListener, ActionListener {
	
	static String version = "1.0";
	static String name = "Minecraft Jar Switcher";
	File minecraftfolder = getWorkingDirectory("minecraft");
	File settingsfolder = new File(getWorkingDirectory("minecraft"), "jarswitcher");
	ConcurrentHashMap<String, JarProperties> jarversions = new ConcurrentHashMap<String, JarProperties>();
	boolean checkmd5sautomatically = false;
	boolean submitmd5sautomatically = false;
	String pathtomclauncher = "";
	
	static String[] theargs = new String[0];
	
	JTextArea jarinfo = new JTextArea("This displays the details of the jar you have selected.");
	JList jars = new JList();
	JButton add = new JButton("Add Jar");
	JButton delete = new JButton("Delete Jar");
	JButton edit = new JButton("Edit Info");
	JButton moveandclose = new JButton("Set jar and Close");
	JButton moveandopen = new JButton("Set jar and Open Minecraft!");
	
	public Switcher() {
		super(name + " version " + version);
		MD5.initNativeLibrary(true);
		System.out.println("Minecraft Path: " + minecraftfolder.getAbsolutePath());
		Container contentPane = getContentPane();
		loadJarDefs();
		loadPrefrences();
		checkJar();
		contentPane.setLayout(new BorderLayout());
		contentPane.add("North", new JLabel("Select Minecraft Jar file below:"));
		updateJarView();
		jars.addListSelectionListener(this);
		jarinfo.setLineWrap(true);
		jarinfo.setWrapStyleWord(true);
		jarinfo.setEditable(false);
		JSplitPane jarstuff = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(jars), new JScrollPane(jarinfo));
		jarstuff.setContinuousLayout(true);
		contentPane.add("Center", jarstuff);
		JPanel jarfunctions = new JPanel();
		moveandclose.addActionListener(this);
		jarfunctions.add(moveandclose);
		moveandopen.addActionListener(this);
		jarfunctions.add(moveandopen);
		add.addActionListener(this);
		jarfunctions.add(add);
		delete.addActionListener(this);
		jarfunctions.add(delete);
		edit.addActionListener(this);
		jarfunctions.add(edit);
		contentPane.add("South", jarfunctions);
	}
	
	private void checkJar() {
		File minecraftjar = new File(minecraftfolder.getAbsoluteFile() + "/bin/minecraft.jar");
		try {
			String hash = MD5.asHex(MD5.getHash(minecraftjar));
			if(!jarversions.containsKey(hash)) {
				int dowe = JOptionPane.showConfirmDialog(this, "A new version of the minecraft.jar has been detected.\nDo you want to add this to the list of jars?", "New Jar Detected!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(dowe == JOptionPane.OK_OPTION) {
					JTextField name = new JTextField();
					JTextField minecraftversion = new JTextField();
					JTextField mods = new JTextField();
					JTextField notes = new JTextField();
					final JComponent[] inputs = new JComponent[] {
					                new JLabel("Name:"),
					                name,
					                new JLabel("Minecraft Version:"),
					                minecraftversion,
					                new JLabel("Mods Installed:"),
					                mods,
					                new JLabel("Other Notes:"),
					                notes
					};
					int result = JOptionPane.showConfirmDialog(null, inputs, "Add new Minecraft.jar", JOptionPane.OK_CANCEL_OPTION);
					if(result == JOptionPane.OK_OPTION && !name.getText().equals("")) {
						String backupjarpath = settingsfolder.getAbsolutePath() + File.separator + "jars" + File.separator + hash + ".zip";
						JarProperties theproperties = new JarProperties(name.getText(), hash, 
								backupjarpath, minecraftversion.getText(), mods.getText(), notes.getText());
						
						boolean success = backupMinecraftJar(backupjarpath);
						if(success) {
							jarversions.put(hash, theproperties);
							saveJarDefs();
							JOptionPane.showMessageDialog(this, "The minecraft.jar has been backed up!", "Backup Success!", JOptionPane.INFORMATION_MESSAGE);
						}else {
							JOptionPane.showMessageDialog(this, "The minecraft.jar backup was not successful. Please run from the command line and report the error message.", "Backup Unsuccess!", JOptionPane.ERROR_MESSAGE);
						}
					}else if(result == JOptionPane.OK_OPTION) {
						JOptionPane.showMessageDialog(this, "You need to fill in at least a name! Please re-open the app to add.", "Name please!", JOptionPane.WARNING_MESSAGE);
					}
						
				}else {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private boolean backupMinecraftJar(String backupjarpath) {
		File inputFile = new File(minecraftfolder.getAbsoluteFile() + "/bin/minecraft.jar");
	    File outputFile = new File(backupjarpath);
	    outputFile.getParentFile().mkdirs();
	    boolean success = true;
		FileInputStream from = null;
	    FileOutputStream to = null;
	    try {
	      from = new FileInputStream(inputFile);
	      to = new FileOutputStream(outputFile);
	      byte[] buffer = new byte[4096];
	      int bytesRead;

	      while ((bytesRead = from.read(buffer)) != -1)
	        to.write(buffer, 0, bytesRead); // write
	    } catch (FileNotFoundException e) {
	    	success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		} finally {
	      if (from != null)
	        try {
	          from.close();
	        } catch (IOException e) {
	          ;
	        }
	      if (to != null)
	        try {
	          to.close();
	        } catch (IOException e) {
	          ;
	        }
	    }
		return success;
	}
	
	private boolean restoreMinecraftJar(String backupjarpath) {
		File outputFile = new File(minecraftfolder.getAbsoluteFile() + "/bin/minecraft.jar");
	    File inputFile = new File(backupjarpath);
	    boolean success = true;
		FileInputStream from = null;
	    FileOutputStream to = null;
	    try {
	      from = new FileInputStream(inputFile);
	      to = new FileOutputStream(outputFile);
	      byte[] buffer = new byte[4096];
	      int bytesRead;

	      while ((bytesRead = from.read(buffer)) != -1)
	        to.write(buffer, 0, bytesRead); // write
	    } catch (FileNotFoundException e) {
	    	success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		} finally {
	      if (from != null)
	        try {
	          from.close();
	        } catch (IOException e) {
	          ;
	        }
	      if (to != null)
	        try {
	          to.close();
	        } catch (IOException e) {
	          ;
	        }
	    }
		return success;
	}

	private void updateJarView() {
		if(jarversions.size() > 0) {
			JarProperties[] newjarlist = new JarProperties[jarversions.size()];
			Collection<JarProperties> thejars = jarversions.values();
			int i = 0;
			for(JarProperties thejar : thejars) {
				newjarlist[i] = thejar;
				i++;
			}
			jars.setListData(newjarlist);
		}
	}
	
	private void loadPrefrences() {
		if(!settingsfolder.exists()) {
			settingsfolder.mkdirs();
		}
		File settingsfile = new File(settingsfolder, "settings.properties");
		if(settingsfile.exists()) {
			try {
				Properties switchersettings = new Properties();
				switchersettings.load(new FileInputStream(settingsfile));
				if(switchersettings.containsKey("checkmd5s")) {
					checkmd5sautomatically = stringToBool(switchersettings.getProperty("checkmd5s", "false"));
				}else {
					/*int dowe = JOptionPane.showConfirmDialog(this, "Do you want to download new minecraft MD5 definitions automatically?", "Auto Downloads", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(dowe == JOptionPane.OK_OPTION) {
						checkmd5sautomatically = true;
					}else {
						checkmd5sautomatically = false;
					}*/
				}
				//submitmd5sautomatically = stringToBool(switchersettings.getProperty("submitmd5s", "false"));
				pathtomclauncher = switchersettings.getProperty("mcpath", "");
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}else {
			/*int dowe = JOptionPane.showConfirmDialog(this, "Do you want to download new minecraft MD5 definitions automatically?", "Auto Downloads", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(dowe == JOptionPane.OK_OPTION) {
				checkmd5sautomatically = true;
			}else {
				checkmd5sautomatically = false;
			}*/
			createPrefenceFile();
		}
	}

	private void createPrefenceFile() {
		/*int dowe = JOptionPane.showConfirmDialog(this, "Do you want to submit your new minecraft MD5 definitions automatically?", "Auto Uploads", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(dowe == JOptionPane.OK_OPTION) {
			submitmd5sautomatically = true;
		}else {
			submitmd5sautomatically = false;
		}
		if(!settingsfolder.exists()) {
			settingsfolder.mkdirs();
		}*/
		
		JOptionPane.showMessageDialog(this, "Please Select your Minecraft Launcher. If you\ndon't want to, just select \"Cancel\" on the next\npage. Without Selecting the launcher we\ncan't auto launch Minecraft for you.", "Minecraft Launcher Location", JOptionPane.INFORMATION_MESSAGE);
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new JarFilter());
		int result = chooser.showOpenDialog(this);
		File fileobj = chooser.getSelectedFile();
		if(result == JFileChooser.APPROVE_OPTION) {
			pathtomclauncher = fileobj.getAbsolutePath();
		}
		File settingsfile = new File(settingsfolder, "settings.properties");
		BufferedWriter outChannel;
		try {
			outChannel = new BufferedWriter(new FileWriter(settingsfile));
			outChannel.write("checkmd5s = " + checkmd5sautomatically + "\nsubmitmd5s = " + submitmd5sautomatically + "\nmcpath = " + escapseWindowsPath(pathtomclauncher));
			outChannel.close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
	}

	private void updateMD5s() {
		// TODO Auto-generated method stub
		
	}
	
	private String escapseWindowsPath(String path) {
		String returnstring = "";
		char[] charpath = path.toCharArray();
		for(char tchar : charpath) {
			if(tchar == '\\') {
				returnstring = returnstring + "\\\\";
			}else {
				returnstring = returnstring + tchar;
			}
		}
		return returnstring;
	}

	private void saveJarDefs() {
		if(!settingsfolder.exists()) {
			settingsfolder.mkdirs();
		}
		File settingsfile = new File(settingsfolder, "jars.db");
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(settingsfile));
			out.writeObject(jarversions);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadJarDefs() {
		if(!settingsfolder.exists()) {
			settingsfolder.mkdirs();
		}
		File settingsfile = new File(settingsfolder, "jars.db");
		if(settingsfile.exists()) {
			try {
				ObjectInputStream out = new ObjectInputStream(new FileInputStream(settingsfile));
				jarversions = (ConcurrentHashMap<String, JarProperties>) out.readObject();
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private synchronized boolean stringToBool(String thebool) {
		boolean result;
		if (thebool.trim().equalsIgnoreCase("true") || thebool.trim().equalsIgnoreCase("yes")) {
	    	result = true;
	    } else {
	    	result = false;
	    }
		return result;
	}

	public static File getWorkingDirectory(String appname) {
		String homedir = System.getProperty("user.home", ".");
		File workdir;
		switch (getPlatform().ordinal()) {
		case 0:
		case 1:
			workdir = new File(homedir, '.' + appname + '/');
			break;
		case 2:
			String applicationData = System.getenv("APPDATA");
			System.out.println("App data directory: " + applicationData);
			if (applicationData != null) {
				workdir = new File(applicationData, "." + appname + '/');
			}else {
				workdir = new File(homedir, '.' + appname + '/');
			}
			break;
		case 3:
			workdir = new File(homedir, "Library/Application Support/" + appname);
			break;
		default:
			workdir = new File(homedir, appname + '/');
		}
		if ((!workdir.exists()) && (!workdir.mkdirs())) throw new RuntimeException("The working directory could not be created: " + workdir);
		return workdir;
	}


	private static OS getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("solaris")) return OS.solaris;
		if (osName.contains("linux")) return OS.linux;
		if (osName.contains("sunos")) return OS.solaris;
		if (osName.contains("unix")) return OS.linux;
		if (osName.contains("win")) return OS.windows;
		if (osName.contains("mac")) return OS.macos;
		return OS.unknown;
	}
	
	public static void main(String[] args) {
		theargs = args;
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    System.err.println("Could not set look and feel");
		}
		final Switcher f = new Switcher();
		f.setBounds(100, 100, 600, 400);
		f.setVisible(true);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);

	}

	private static enum OS
	{
		solaris, linux, windows, macos, unknown;
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if(arg0.getSource() == jars) {
			JarProperties theproperties = (JarProperties)jars.getSelectedValue();
			if(theproperties != null) {
				jarinfo.setText("Name:\n" 
						+ theproperties.getName() + "\n" +
						"Minecraft Version:\n" +
						theproperties.getMinecraftversion() + "\n" +
						"Mods:\n" +
						theproperties.getMods() + "\n" +
						"Notes:\n" +
						theproperties.getNotes() + "\n" +
						"MD5 hash:\n" +
						theproperties.getMd5hash());
			}
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == moveandclose || e.getSource() == moveandopen) {
			JarProperties theproperties = (JarProperties)jars.getSelectedValue();
			if(theproperties != null) {
				System.out.println("We have a line!");
				boolean success = restoreMinecraftJar(theproperties.zipfilelocation);
				if(success) {
					System.out.println("Minecraft copy successful!");
					if(e.getSource() == moveandopen) {
						System.out.println("Move and open selected! Path to minecraft: " + pathtomclauncher);
						if(pathtomclauncher.toLowerCase().endsWith(".jar")) {
							SecureClassLoader sysLoader = (SecureClassLoader) ClassLoader.getSystemClassLoader();
					        ClassLoaderUtil classloader = new ClassLoaderUtil();
					        try {
								classloader.addFile(pathtomclauncher);
								System.out.println("Launching minecraft");
								net.minecraft.MinecraftLauncher.main(theargs);
								setVisible(false);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (InstantiationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IllegalAccessException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}else if(pathtomclauncher.toLowerCase().endsWith(".exe")) {
						    try
						    {
						    Runtime rt = Runtime.getRuntime();
						    System.out.println("Running exe.");
						    //Process p = rt.exec("c:\\windows\\notepad.exe");
						    Process p = rt.exec("\"" + pathtomclauncher + "\"");
						    int errcode = p.waitFor();
						    System.out.println("Minecraft Exited with error code " + errcode + ".");
						    setVisible(false);
						    System.exit(errcode);
						    //p.destroy();
						    }catch(Exception exc){
						    	exc.printStackTrace();
						    }
						}else if(pathtomclauncher.toLowerCase().endsWith(".app")) {
							SecureClassLoader sysLoader = (SecureClassLoader) ClassLoader.getSystemClassLoader();
					        ClassLoaderUtil classloader = new ClassLoaderUtil();
					        try {
								classloader.addFile(pathtomclauncher + File.separator + "Contents" + File.separator + "Resources" + File.separator + "Java" + File.separator + "MinecraftLauncher.jar");
								System.out.println("Launching minecraft");
								net.minecraft.MinecraftLauncher.main(theargs);
								setVisible(false);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ClassNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (InstantiationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IllegalAccessException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else {
						JOptionPane.showMessageDialog(this, "Minecraft.jar copied! You can now launch Minecraft.", "Launch Minecraft", JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					}
				}else {
					JOptionPane.showMessageDialog(this, "Copy failed!", "Minecraft copy failed!", JOptionPane.ERROR_MESSAGE);
				}
			}else {
				JOptionPane.showMessageDialog(this, "You need to select the version of Minecraft to launch!", "Select Minecraft Version", JOptionPane.INFORMATION_MESSAGE);
			}
		}else if(e.getSource() == add) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new MinecraftJarFilter());
			chooser.setName("Choose minecraft.jar");
			int result = chooser.showOpenDialog(this);
			File minecraftjar = chooser.getSelectedFile();
			if(result == JFileChooser.APPROVE_OPTION) {
				try {
					String hash = MD5.asHex(MD5.getHash(minecraftjar));
					if(!jarversions.containsKey(hash)) {
						JTextField name = new JTextField();
						JTextField minecraftversion = new JTextField();
						JTextField mods = new JTextField();
						JTextField notes = new JTextField();
						final JComponent[] inputs = new JComponent[] {
								new JLabel("Name:"),
								name,
								new JLabel("Minecraft Version:"),
								minecraftversion,
								new JLabel("Mods Installed:"),
								mods,
								new JLabel("Other Notes:"),
								notes
						};
						int result1 = JOptionPane.showConfirmDialog(null, inputs, "Add new Minecraft.jar", JOptionPane.OK_CANCEL_OPTION);
						if(result1 == JOptionPane.OK_OPTION && !name.getText().equals("")) {
							String backupjarpath = settingsfolder.getAbsolutePath() + File.separator + "jars" + File.separator + hash + ".zip";
							JarProperties theproperties = new JarProperties(name.getText(), hash, 
									backupjarpath, minecraftversion.getText(), mods.getText(), notes.getText());

							boolean success = backupMinecraftJar(backupjarpath, minecraftjar.getAbsolutePath());
							if(success) {
								jarversions.put(hash, theproperties);
								updateJarView();
								saveJarDefs();
								JOptionPane.showMessageDialog(this, "The minecraft.jar has been backed up!", "Backup Success!", JOptionPane.INFORMATION_MESSAGE);
							}else {
								JOptionPane.showMessageDialog(this, "The minecraft.jar backup was not successful. Please run from the command line and report the error message.", "Backup Unsuccess!", JOptionPane.ERROR_MESSAGE);
							}
						}else if(result1 == JOptionPane.OK_OPTION) {
							JOptionPane.showMessageDialog(this, "You need to fill in at least a name! Please re-add.", "Name please!", JOptionPane.WARNING_MESSAGE);
						}

					}else {
						JOptionPane.showMessageDialog(this, "This version of the minecraft.jar has already been backed up!", "Backed up already!", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, "An error occurred. Please run from the command line and submit the error report.", "Add Failed", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
			}
		}else if(e.getSource() == edit) {
			JarProperties theproperties = (JarProperties)jars.getSelectedValue();
			if(theproperties != null) {
				JTextField name = new JTextField(theproperties.getName());
				JTextField minecraftversion = new JTextField(theproperties.getMinecraftversion());
				JTextField mods = new JTextField(theproperties.getMods());
				JTextField notes = new JTextField(theproperties.getNotes());
				final JComponent[] inputs = new JComponent[] {
						new JLabel("Name:"),
						name,
						new JLabel("Minecraft Version:"),
						minecraftversion,
						new JLabel("Mods Installed:"),
						mods,
						new JLabel("Other Notes:"),
						notes
				};
				int result1 = JOptionPane.showConfirmDialog(null, inputs, "Add new Minecraft.jar", JOptionPane.OK_CANCEL_OPTION);
				if(result1 == JOptionPane.OK_OPTION && !name.getText().equals("")) {
					theproperties.setName(name.getText());
					theproperties.setMinecraftversion(minecraftversion.getText());
					theproperties.setMods(mods.getText());
					theproperties.setNotes(notes.getText());
					saveJarDefs();
					jarinfo.setText("Name:\n" 
							+ theproperties.getName() + "\n" +
							"Minecraft Version:\n" +
							theproperties.getMinecraftversion() + "\n" +
							"Mods:\n" +
							theproperties.getMods() + "\n" +
							"Notes:\n" +
							theproperties.getNotes() + "\n" +
							"MD5 hash:\n" +
							theproperties.getMd5hash());
				}else if(result1 == JOptionPane.OK_OPTION) {
					JOptionPane.showMessageDialog(this, "You need to fill in at least a name! Please re-add.", "Name please!", JOptionPane.WARNING_MESSAGE);
				}
			}else {
				JOptionPane.showMessageDialog(this, "You need to select the version of Minecraft to edit!", "Edit Minecraft Version Details", JOptionPane.INFORMATION_MESSAGE);
			}
		}else if(e.getSource() == delete) {
			JarProperties theproperties = (JarProperties)jars.getSelectedValue();
			if(theproperties != null) {
				int result1 = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this minecraft.jar?\n\"" + theproperties.getName()  + "\"\n(There is no going back!)", "Delete Minecraft.jar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(result1 == JOptionPane.OK_OPTION) {
					File delfile = new File(theproperties.zipfilelocation);
					boolean result = delfile.delete();
					if(result || !delfile.exists()) {
						jarversions.remove(theproperties.getMd5hash());
						updateJarView();
						saveJarDefs();
						JOptionPane.showMessageDialog(this, "That minecraft.jar version has been deleted successfully.", "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
					}else {
						JOptionPane.showMessageDialog(this, "Uhoh, we were unable to delete that minecraft.jar file.", "Delete Not Successful", JOptionPane.ERROR_MESSAGE);
					}
				}
			}else {
				JOptionPane.showMessageDialog(this, "You need to select the version of Minecraft to delete!", "Delete Minecraft Version", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private boolean backupMinecraftJar(String backupjarpath, String inputjar) {
		File inputFile = new File(inputjar);
	    File outputFile = new File(backupjarpath);
	    outputFile.getParentFile().mkdirs();
	    boolean success = true;
		FileInputStream from = null;
	    FileOutputStream to = null;
	    try {
	      from = new FileInputStream(inputFile);
	      to = new FileOutputStream(outputFile);
	      byte[] buffer = new byte[4096];
	      int bytesRead;
	
	      while ((bytesRead = from.read(buffer)) != -1)
	        to.write(buffer, 0, bytesRead); // write
	    } catch (FileNotFoundException e) {
	    	success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		} finally {
	      if (from != null)
	        try {
	          from.close();
	        } catch (IOException e) {
	          ;
	        }
	      if (to != null)
	        try {
	          to.close();
	        } catch (IOException e) {
	          ;
	        }
	    }
		return success;
	}
}
