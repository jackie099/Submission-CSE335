/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.msu.lizheng9;

import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


@ActionID(
        category = "File",
        id = "com.msu.lizheng9.PackListener"
)
@ActionRegistration(
        iconBase = "com/msu/lizheng9/16x16.png",
        displayName = "#CTL_PackListener"
)
@ActionReference(path = "Toolbars/File", position = 0)
@Messages("CTL_PackListener=Pack")
public final class PackListener implements ActionListener {
    private String getProjectDirectory() {
        try {
            Lookup lookup = Utilities.actionsGlobalContext(); 
            Project project = lookup.lookup(Project.class);
            if (project == null){
                System.out.println("hi");
                TopComponent activeTC = TopComponent.getRegistry().getActivated();
                DataObject dataLookup = activeTC.getLookup().lookup(DataObject.class);
                File f = FileUtil.toFile(dataLookup.getPrimaryFile());
                if(f.isFile()){
                    f = f.getParentFile();
                }


                while(true){
                    File chk = new File(f,"nbproject");
                    if(chk.exists()){
                        if(chk.isDirectory()){
                            break;
                        }
                    }
                    f = f.getParentFile();
                }
                return f.getPath();
            }

            FileObject projectDir = project.getProjectDirectory();
            return projectDir.getPath();

            
        } catch (Exception e) {
              JFrame frame = new JFrame("Window");
              JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", 0);
              return null;
        }
    }
    public static void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
              .filter(path -> !Files.isDirectory(path))
              .forEach(path -> {
                  ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                  try {
                      zs.putNextEntry(zipEntry);
                      Files.copy(path, zs);
                      zs.closeEntry();
                } catch (IOException e) {
                    System.err.println(e);
                }
              });
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        JFrame frame = new JFrame("Window");
        //JOptionPane.showMessageDialog(frame, getProjectDirectory(), "Path", 0);
        try{
            final JFileChooser fc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("zip files", "*.zip"); 
            fc.addChoosableFileFilter(filter);
            fc.setFileFilter(filter);
            int returnVal = fc.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION){
                File f = fc.getSelectedFile();
                String path = f.getPath();
                if (!path .endsWith(".zip")){
                    path += ".zip";
                }
                File chk = new File(path);
                if (chk.exists()){
                    if(chk.isFile()){
                        if(chk.delete() == false){
                            JOptionPane.showMessageDialog(frame, "File in use, please close the file and try again.", "Error", 0);
                            return;
                        }
                    }
                }
                pack(getProjectDirectory(), path);
                JOptionPane.showMessageDialog(frame, "Zip saved as "+path, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(IOException x){
            JOptionPane.showMessageDialog(frame, x.getMessage(), "Error", 0);
        }
        
        // TODO implement action body
    }
}

