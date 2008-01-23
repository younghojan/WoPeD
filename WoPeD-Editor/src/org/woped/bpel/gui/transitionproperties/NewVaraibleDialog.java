/**
 * 
 */
package org.woped.bpel.gui.transitionproperties;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.woped.editor.controller.TransitionPropertyEditor;
import org.woped.translations.Messages;

/**
 * @author Frank Sch�ler, Ester Landes
 * 
 */
public class NewVaraibleDialog extends JDialog {

	static final int _OKBUTTON = 0;
	static final int _CANCELBUTTON = 1;

	private JTextField VariableName;
	private JPanel dialogButtons = null;
	private int activbutton = -1;

	private JComboBox variableTypesComboBox = null;
	
	TransitionPropertyEditor t_editor;

	/**
	 * @param arg0
	 *            TransitionPropertyEditor
	 * @param arg1
	 *            boolean
	 * @throws HeadlessException
	 */
	public NewVaraibleDialog(TransitionPropertyEditor arg0)
			throws HeadlessException {
		super(arg0, true);
		// TODO Auto-generated constructor stub
	}

	public void init() {
		this.setVisible(false);
		this.setTitle(Messages
				.getString("Transition.Properties.BPEL.NewVariable"));
		this.setSize(400, 150);
		this.setLocation(150, 150);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.weighty = 1;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 0, 0);
		this.add(new JLabel(Messages
				.getString("Transition.Properties.BPEL.NewVariable.Name")
				+ ":"), c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 0, 5);
		if (this.VariableName == null)
			this.VariableName = new JTextField("");
		this.add(this.VariableName, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 0, 0);
		this.add(new JLabel(Messages
				.getString("Transition.Properties.BPEL.NewVariable.Type")
				+ ":"), c);

		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 0, 5);
		this.add(getVariableTypesComboBox(), c);

		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(0, 5, 0, 0);
		this.add(addVariableDialogButtons(), c);

		this.setVisible(true);
	}

	public JPanel addVariableDialogButtons() {
		if (dialogButtons == null) {
			dialogButtons = new JPanel();

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.WEST;
			c.weightx = 1;
			c.weighty = 1;

			c.gridx = 0;
			c.gridy = 0;
			c.gridwidth = 1;
			c.insets = new Insets(0, 5, 0, 0);
			JButton b = new JButton(Messages
					.getString("Transition.Properties.BPEL.Buttons.OK"));
			b.addActionListener(new ButtonEvent(this,
					NewVaraibleDialog._OKBUTTON));
			dialogButtons.add(b, c);

			c.gridx = 1;
			c.gridy = 0;
			c.gridwidth = 1;
			c.insets = new Insets(0, 5, 0, 0);
			b = new JButton(Messages
					.getString("Transition.Properties.BPEL.Buttons.Cancel"));
			b.addActionListener(new ButtonEvent(this,
					NewVaraibleDialog._CANCELBUTTON));
			dialogButtons.add(b, c);
		}
		return dialogButtons;
	}

	private JComboBox getVariableTypesComboBox() {
		if (variableTypesComboBox == null) {
			variableTypesComboBox = new JComboBox();
			String[] variables = this.t_editor.getEditor().getModelProcessor()
					.getElementContainer().getTypes();
			for (int i = 0; i < variables.length; i++) {
				variableTypesComboBox.addItem(variables[i]);
			}
		}
		return variableTypesComboBox;
	}

	public String newVariable() {
		return "" + this.VariableName.getText();
	}
	
	public String getType()
	{
		return "" + this.variableTypesComboBox.getSelectedItem().toString();
	}

	public TransitionPropertyEditor getTransitionPropertyEditor() {
		return this.t_editor;
	}
	
	public void setActivButton(int Buttontype)
	{
		this.activbutton = Buttontype;
	}
	
	public int getActivButton()
	{
		return this.activbutton;
	}

	class ButtonEvent implements ActionListener {
		private NewVaraibleDialog _adaptee;
		private int _buttontype = -1;

		public ButtonEvent(NewVaraibleDialog Adaptee, int Buttontype) {
			this._adaptee = Adaptee;
			this._buttontype = Buttontype;
		}

		public void actionPerformed(ActionEvent arg0) {
			this._adaptee.setActivButton(this._buttontype);
			if (this._buttontype == NewVaraibleDialog._OKBUTTON) {
				this._adaptee.getTransitionPropertyEditor().getEditor()
						.getModelProcessor().getElementContainer().addVariable(
								VariableName.getText(),
								getVariableTypesComboBox().getSelectedItem()
										.toString());
			} 
			this._adaptee.dispose();
		}
	}
}
