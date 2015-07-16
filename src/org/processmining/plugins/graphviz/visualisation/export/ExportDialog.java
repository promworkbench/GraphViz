package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExportDialog extends JFileChooser {
	private static final long serialVersionUID = -6928894765212379860L;
	private static final Preferences preferences = Preferences.userRoot().node("org.processmining.graphviz");

	public ExportDialog(NavigableSVGPanel parent) {
		super(preferences.get("lastUsedFolder", new File(".").getAbsolutePath()));
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new ExporterPDF());
		addChoosableFileFilter(new ExporterPNG());

		try {
			int returnVal = showSaveDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Exporter fileFilter = (Exporter) getFileFilter();
				File file = fileFilter.addExtension(getSelectedFile());

				fileFilter.export(parent.getImage(), file);

				//save the path for later use
				preferences.put("lastUsedFolder", file.getParent());
			}
		} catch (Exception e) {
			JOptionPane.showConfirmDialog(this, e.getMessage(), "Error while saving image", JOptionPane.OK_OPTION);
		}
	}

	@Override
	public void approveSelection() {
		Exporter fileFilter = (Exporter) getFileFilter();
		File f = fileFilter.addExtension(getSelectedFile());
		if (f.exists() && getDialogType() == SAVE_DIALOG) {
			int result = JOptionPane.showConfirmDialog(this, "The file already exists, do you want to overwrite it?",
					"Existing file", JOptionPane.YES_NO_OPTION);
			switch (result) {
				case JOptionPane.YES_OPTION :
					super.approveSelection();
					return;
				default :
					return;
			}
		}
		super.approveSelection();
	}

}
