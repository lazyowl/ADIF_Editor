package main;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;

import net.coderazzi.filters.gui.*;
import net.coderazzi.filters.gui.TableFilterHeader.*;

import java.awt.Color;
import java.awt.event.*;
//跨平台起见，swing丑就丑吧TAT!!

public class GUIFrame {

	private JFrame frame;
	
	private JMenuBar menuBar;
	
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem importMenuItem;	
	private JMenuItem exportMenuItem;
	private JMenuItem exitMenuItem;
	
	private JMenu editMenu;
	private JMenuItem addColumnMenuItem;
	private JMenuItem removeColumnMenuItem;
	private JMenuItem hideColumnMenuItem;
	private JMenuItem showAllHiddenColumnMenuItem;
	private JMenuItem searchMenuItem;
	private JMenuItem filterMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	private JFileChooser jFileChooser;
	
	private GUITable table = null;
	
	private TableFilterHeader filterHeader = null;
	
	public GUIFrame()
	{
		//创建初始界面
		frame = new JFrame();
		frame.setSize(800,600);
		frame.setTitle("ADIF Editor v1.0 by lazyowl");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		table = new GUITable(new MyTableModel());	
		frame.add(new JScrollPane(table));
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		newMenuItem = new JMenuItem("New");
		importMenuItem = new JMenuItem("Import & Merge");
		exportMenuItem = new JMenuItem("Export");
		exitMenuItem = new JMenuItem("Exit");
		
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		newMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.setModel(new MyTableModel());
			}
		});		
		importMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				importFile();
			}
		});
		exportMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				boolean ok = exportFile();
				if (ok) JOptionPane.showMessageDialog(frame, "导出成功");
				else JOptionPane.showMessageDialog(frame, "没有找到相应的值", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});		
		exitMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}
		});
		
		editMenu = new JMenu("Edit");
		addColumnMenuItem = new JMenuItem("Add column");
		removeColumnMenuItem = new JMenuItem("Remove column");
		hideColumnMenuItem = new JMenuItem("Hide column");
		hideColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(frame, "请输入要隐藏的列号(列号从1开始)");
				try
				{
					int col = Integer.parseInt(target)-1;
					table.hideColumn(table.displayedColumnNumber(col));
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(frame, "请输入合法的整数值", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		showAllHiddenColumnMenuItem = new JMenuItem("Show all hidden column");
		showAllHiddenColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.showAllHiddenColumn(frame.getWidth());
			}
		});
		
		searchMenuItem = new JMenuItem("Search");
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		searchMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(frame, "请输入要搜索的值", "搜索", JOptionPane.PLAIN_MESSAGE);
				if (target!=null)
				{
					boolean ok = table.search(target);
					if (!ok) JOptionPane.showMessageDialog(frame, "没有找到相应的值");					
				}
			}
		});
		filterMenuItem = new JMenuItem("Show / Hide filter");
		filterMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				if (filterHeader!=null && filterHeader.isVisible())
				{
					filterHeader.setVisible(false);
				}
				else
				{
					filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
					filterHeader.setPosition(Position.TOP);
					filterHeader.setBackground(Color.darkGray);
					filterHeader.setForeground(Color.white);
					filterHeader.setVisible(true);
				}
			}
		});
		
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				JOptionPane.showMessageDialog(frame, 
						"ADIF Editor v1.0 is a simple editor build for ADIF format data.\n"
						+ "If you have any questions, please contact fenixl@163.com\n"
						+ "                                                              Author: Lazyowl", 
						"About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(newMenuItem);
		fileMenu.add(importMenuItem);
		fileMenu.add(exportMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		editMenu.add(addColumnMenuItem);
		editMenu.add(removeColumnMenuItem);
		editMenu.add(hideColumnMenuItem);
		editMenu.add(showAllHiddenColumnMenuItem);
		editMenu.addSeparator();
		editMenu.add(searchMenuItem);
		editMenu.add(filterMenuItem);		
		
		helpMenu.add(aboutMenuItem);
		
		//Do something for preview		
		frame.setVisible(true);
	}	
	
	private void importFile()
	{
		//open file的菜单项
		jFileChooser = new JFileChooser(new File("."));
		jFileChooser.removeChoosableFileFilter(jFileChooser.getFileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			FileAnalyzer fa = new FileAnalyzer();
			Records r = fa.analyze(jFileChooser.getSelectedFile());

			//设置好table
			table.importData(r);
			
			//刷新屏幕
			frame.revalidate();
			table.requestFocusInWindow(); //有了focus搜索才能显示
		}
	}
	
	private boolean exportFile()
	{
		jFileChooser = new JFileChooser(new File("."));
		jFileChooser.removeChoosableFileFilter(jFileChooser.getFileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
		{			
			Records r = table.exportData();
			FileExporter fe = new FileExporter();
			try
			{
				fe.export(r, jFileChooser.getSelectedFile());
			}
			catch (Exception e)
			{
				return false;
			}
		}
		return true;
	}
	
	private class ADIF3FileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 3.0.4 file(*.adi;*.adx)";
		}
	}
	
	private class ADIF2FileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 2.2.7 file(*.adi)";
		}
	}
	
	private class XlsxFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".xlsx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any Excel 2007(or later) file(*.xlsx)";
		}
	}
	
	/*
	private class AllFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx") || f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any legal file(*.adi;*.adx;*.xls;*.xlsx)";
		}
	}
	*/
}
