/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.maptofields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.maptofields.MapToFieldsMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class MapToFieldsDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = MapToFieldsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MapToFieldsMeta input;
	private boolean gotPreviousFields=false;
	private RowMetaInterface previousFields;
  
	private Label        wlMapField;
  private CCombo       wMapField;
  private FormData     fdlMapField, fdMapField;
  
  private Label        wlRemoveMapField;
  private Button       wRemoveMapField;
  private FormData     fdlRemoveMapField, fdRemoveMapField;
  
	private Label        wlKeyField;
  private TextVar      wKeyField;
  private FormData     fdlKeyField, fdKeyField;
  
  private Label        wlValueField;
  private TextVar      wValueField;
  private FormData     fdlValueField, fdValueField;
  
  public MapToFieldsDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(MapToFieldsMeta)in;
	}

	public String open()
	{
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
 		props.setLook(shell);
 		setShellImage(shell, input);
        
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		
		SelectionAdapter btnSel = new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        input.setChanged();
		    }
		};

		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.Shell.Title")); 
		
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname=new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.Stepname.Label")); 
 		props.setLook(wlStepname);
		fdlStepname=new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(middle, -margin);
		fdlStepname.top  = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
 		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname=new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top  = new FormAttachment(0, margin);
		fdStepname.right= new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
	  // Map field
    wlMapField=new Label(shell, SWT.RIGHT);
    wlMapField.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.MapField.Label")); 
    props.setLook(wlMapField);
    fdlMapField=new FormData();
    fdlMapField.left = new FormAttachment(0, 0);
    fdlMapField.right= new FormAttachment(middle, -margin);
    fdlMapField.top  = new FormAttachment(wStepname, margin);
    wlMapField.setLayoutData(fdlMapField);
    wMapField=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
    wMapField.setText("");
    props.setLook(wMapField);
    wMapField.addModifyListener(lsMod);
    fdMapField=new FormData();
    fdMapField.left = new FormAttachment(middle, 0);
    fdMapField.top  = new FormAttachment(wStepname, margin);
    fdMapField.right= new FormAttachment(100, 0);
    wMapField.setLayoutData(fdMapField);
    wMapField.addFocusListener(new FocusListener() {
      public void focusLost(org.eclipse.swt.events.FocusEvent e) {
      }
  
      public void focusGained(org.eclipse.swt.events.FocusEvent e) {
          Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
          shell.setCursor(busy);
          getFieldsInto(wMapField);
          shell.setCursor(null);
          busy.dispose();
      }
    });
    
    // Remove Map field
    wlRemoveMapField=new Label(shell, SWT.RIGHT);
    wlRemoveMapField.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.RemoveMapField.Label")); 
    props.setLook(wlRemoveMapField);
    fdlRemoveMapField=new FormData();
    fdlRemoveMapField.left = new FormAttachment(0, 0);
    fdlRemoveMapField.right= new FormAttachment(middle, -margin);
    fdlRemoveMapField.top  = new FormAttachment(wMapField, margin);
    wlRemoveMapField.setLayoutData(fdlRemoveMapField);
    wRemoveMapField=new Button(shell, SWT.CHECK);
    wRemoveMapField.setText("");
    props.setLook(wRemoveMapField);
    wRemoveMapField.addSelectionListener(btnSel);
    fdRemoveMapField=new FormData();
    fdRemoveMapField.left = new FormAttachment(middle, 0);
    fdRemoveMapField.top  = new FormAttachment(wMapField, margin);
    fdRemoveMapField.right= new FormAttachment(100, 0);
    wRemoveMapField.setLayoutData(fdRemoveMapField);
    
    
	  // Key field
    wlKeyField=new Label(shell, SWT.RIGHT);
    wlKeyField.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.field.key.Label")); 
    props.setLook(wlKeyField);
    fdlKeyField=new FormData();
    fdlKeyField.left = new FormAttachment(0, 0);
    fdlKeyField.right= new FormAttachment(middle, -margin);
    fdlKeyField.top  = new FormAttachment(wRemoveMapField, margin);
    wlKeyField.setLayoutData(fdlKeyField);
    wKeyField=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wKeyField);
    wKeyField.addModifyListener(lsMod);
    fdKeyField=new FormData();
    fdKeyField.left  = new FormAttachment(middle, 0);
    fdKeyField.top   = new FormAttachment(wRemoveMapField, margin);
    fdKeyField.right = new FormAttachment(100, 0);
    wKeyField.setLayoutData(fdKeyField);
    
    // Value field
    wlValueField=new Label(shell, SWT.RIGHT);
    wlValueField.setText(BaseMessages.getString(PKG, "MapToFieldsDialog.field.value.Label")); 
    props.setLook(wlValueField);
    fdlValueField=new FormData();
    fdlValueField.left = new FormAttachment(0, 0);
    fdlValueField.right= new FormAttachment(middle, -margin);
    fdlValueField.top  = new FormAttachment(wKeyField, margin);
    wlValueField.setLayoutData(fdlValueField);
    wValueField=new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wValueField);
    wValueField.addModifyListener(lsMod);
    fdValueField=new FormData();
    fdValueField.left  = new FormAttachment(middle, 0);
    fdValueField.top   = new FormAttachment(wKeyField, margin);
    fdValueField.right = new FormAttachment(100, 0);
    wValueField.setLayoutData(fdValueField);
    		
    
		// Some buttons
		wOK=new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel=new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		setButtonPositions(new Button[] { wOK, wCancel }, margin, wValueField);

		// Add listeners
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
		
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener    (SWT.Selection, lsOK    );
		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );


		// Set the shell size, based upon previous time...
		setSize();
		
		getData();
		input.setChanged(changed);
	
		shell.open();
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}
	
  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    if(!Const.isEmpty(input.getKeyFieldName())) {
      wKeyField.setText(input.getKeyFieldName());
    }
    if(!Const.isEmpty(input.getValueFieldName())) {
      wValueField.setText(input.getValueFieldName());
    }
    if(!Const.isEmpty(input.getMapFieldName())) {
      wMapField.setText(input.getMapFieldName());
    }
    
    wRemoveMapField.setSelection(input.isRemoveMapField());
    
    wStepname.selectAll();
    wStepname.setFocus();
  }
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(changed);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value
		input.setKeyFieldName(wKeyField.getText());
		input.setValueFieldName(wValueField.getText());
		input.setMapFieldName(wMapField.getText());
		input.setRemoveMapField(wRemoveMapField.getSelection());
		
		dispose();
	}
	
	private void getFieldsInto(CCombo fieldCombo)
  {
    try {
      if(!gotPreviousFields) {
        previousFields = transMeta.getPrevStepFields(stepname);
      }
      
      String field = fieldCombo.getText();
      
      if(previousFields != null) {
        fieldCombo.setItems(previousFields.getFieldNames());
      }
      
      if(field != null) fieldCombo.setText(field);
      gotPreviousFields=true;
      
   } catch(KettleException ke) {
     new ErrorDialog(shell, 
         BaseMessages.getString(PKG, "MapToFieldsDialog.FailedToGetFields.DialogTitle"), 
         BaseMessages.getString(PKG, "MapToFieldsDialog.FailedToGetFields.DialogMessage"), ke);
   }
  }
}
