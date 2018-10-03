/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.steps.combinationsequence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.combinationsequence.CombinationSequenceMeta;
import org.pentaho.di.trans.steps.combinationsequence.CombinationSequenceMode;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ColumnsResizer;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.dialog.AbstractStepDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

public class CombinationSequenceDialog extends AbstractStepDialog<CombinationSequenceMeta> {
  private static Class<?> PKG = CombinationSequenceMeta.class; // for i18n purposes, needed by Translator2!!

  private Text txtResult;
  
  private TextVar txtStart;

  private TextVar txtIncrement;

  private TableView tblFields;

  private Button btnModeReset;

  private Button btnModeIncrement;


  public static final String STRING_CHANGE_SEQUENCE_WARNING_PARAMETER = "ChangeSequenceSortWarning";

  public CombinationSequenceDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);

    setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Shell.Title"));  //$NON-NLS-1$

   
  }

  @Override
  public Point getMinimumSize() {
    return new Point(500, 500);
  }


  protected void onGetFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        TableItemInsertListener insertListener = new TableItemInsertListener() {
          public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
            tableItem.setText(2, BaseMessages.getString(PKG, "System.Combo.Yes"));  //$NON-NLS-1$
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious(r, tblFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
      }
    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"),  //$NON-NLS-1$
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);  //$NON-NLS-1$
    }

  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  protected void loadMeta(final CombinationSequenceMeta meta) {

    if ( meta.getMode()==CombinationSequenceMode.RESET )
      btnModeReset.setSelection(true);
    else 
      btnModeIncrement.setSelection(true);
    
    txtStart.setText(Const.NVL(meta.getStart(), "1"));
    txtIncrement.setText(Const.NVL(meta.getIncrement(), "1"));
    txtResult.setText(Const.NVL(meta.getResultFieldName(), "result"));

    Table table = tblFields.table;
    if (meta.getFieldName().length > 0) {
      table.removeAll();
    }
    for (int i = 0; i < meta.getFieldName().length; i++) {
      TableItem ti = new TableItem(table, SWT.NONE);
      ti.setText(0, "" + (i + 1));
      ti.setText(1, meta.getFieldName()[i]);
    }

    tblFields.setRowNums();
    tblFields.optWidth(true);

    wStepname.selectAll();
    wStepname.setFocus();
  }

  @Override
  protected void saveMeta(final CombinationSequenceMeta meta) {
    stepname = wStepname.getText(); // return value

    
    
    meta.setMode( btnModeReset.getSelection() ? CombinationSequenceMode.RESET: CombinationSequenceMode.INCREMENT );
    meta.setStart(txtStart.getText());
    meta.setIncrement(txtIncrement.getText());
    meta.setResultFieldName(txtResult.getText());

    int nrfields = tblFields.nrNonEmpty();
    meta.allocate(nrfields);
    for (int i = 0; i < nrfields; i++) {
      TableItem ti = tblFields.getNonEmpty(i);
      //CHECKSTYLE:Indentation:OFF
      meta.getFieldName()[i] = ti.getText(1);
    }

    if ("Y".equalsIgnoreCase(props.getCustomParameter(STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, "Y"))) {
      MessageDialogWithToggle md = new MessageDialogWithToggle(shell,
          BaseMessages.getString(PKG, "CombinationSequenceDialog.InputNeedSort.DialogTitle"), null,
          BaseMessages.getString(PKG, "CombinationSequenceDialog.InputNeedSort.DialogMessage", Const.CR) + Const.CR,
          MessageDialog.WARNING,
          new String[] { BaseMessages.getString(PKG, "CombinationSequenceDialog.InputNeedSort.Option1") }, 0,
          BaseMessages.getString(PKG, "CombinationSequenceDialog.InputNeedSort.Option2"),
          "N".equalsIgnoreCase(props.getCustomParameter(STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, "Y")));
      MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
      md.open();
      props.setCustomParameter(STRING_CHANGE_SEQUENCE_WARNING_PARAMETER, md.getToggleState() ? "N" : "Y");
      props.saveProps();

    }
  }

  @Override
  protected Control createDialogArea(final Composite parent) {

    // Mode
    Group group = new Group(parent, SWT.SHADOW_IN);
    group.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Label"));  //$NON-NLS-1$
    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
    rowLayout.marginWidth = 10;
    rowLayout.marginHeight = 10;    
    group.setLayout(rowLayout);
    group.setLayoutData(new FormDataBuilder().top().fullWidth().result());
    props.setLook(group);

    btnModeReset = new Button(group, SWT.RADIO);
    btnModeReset.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Reset.Label"));  //$NON-NLS-1$
    btnModeReset.addSelectionListener(lsDef);
    props.setLook(btnModeReset);

    btnModeIncrement = new Button(group, SWT.RADIO);
    btnModeIncrement.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Increment.Label"));  //$NON-NLS-1$
    btnModeIncrement.addSelectionListener(lsDef);
    props.setLook(btnModeIncrement);

    // Result line...
    Label lblResult = new Label(parent, SWT.NONE);
    lblResult.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Result.Label"));  //$NON-NLS-1$
    lblResult.setLayoutData(new FormDataBuilder().top(group, Const.MARGIN*2).fullWidth().result());
    props.setLook(lblResult);

    txtResult = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    txtResult.setLayoutData(new FormDataBuilder().top(lblResult, Const.MARGIN).fullWidth().result()); 
    txtResult.addModifyListener(lsMod);
    props.setLook(txtResult);

    // Start
    Label lblStart = new Label(parent, SWT.LEFT);
    lblStart.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Start.Label"));  //$NON-NLS-1$
    lblStart.setLayoutData(new FormDataBuilder().top(txtResult, Const.MARGIN*2).fullWidth().result());
    props.setLook(lblStart);
    
    txtStart = new TextVar(transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    txtStart.setLayoutData(new FormDataBuilder().top(lblStart, Const.MARGIN).fullWidth().result());
    txtStart.addModifyListener(lsMod);
    props.setLook(txtStart);
    
    // Increment
    Label lblIncrement = new Label(parent, SWT.LEFT);
    lblIncrement.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Increment.Label"));  //$NON-NLS-1$
    lblIncrement.setLayoutData(new FormDataBuilder().top(txtStart, Const.MARGIN*2).fullWidth().result());   
    props.setLook(lblIncrement);
        
    txtIncrement = new TextVar(transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    txtIncrement.setLayoutData(new FormDataBuilder().top(lblIncrement, Const.MARGIN).fullWidth().result());
    txtIncrement.addModifyListener(lsMod);
    props.setLook(txtIncrement);
    
    // Table with fields
    Label lblFields = new Label(parent, SWT.LEFT);
    lblFields.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Fields.Label"));  //$NON-NLS-1$
    lblFields.setLayoutData(new FormDataBuilder().top(txtIncrement, Const.MARGIN*2).fullWidth().result());
    
    props.setLook(lblFields);
    
    // Button Get fields
    wGet = new Button(parent, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));  //$NON-NLS-1$
    wGet.setLayoutData(new FormDataBuilder().top(lblFields, Const.MARGIN).right().result());
    wGet.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onGetFields();
      }
    });

    final int FieldsCols = 1;
    final int FieldsRows = this.getStepMeta().getFieldName().length;

    ColumnInfo[] columns = new ColumnInfo[FieldsCols];
    columns[0] = new ColumnInfo(BaseMessages.getString(PKG, "CombinationSequenceDialog.Fieldname.Column"),  //$NON-NLS-1$
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
    tblFields = new TableView(transMeta, parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, columns, FieldsRows,
        lsMod, props);

    this.tblFields.setLayoutData(
        new FormDataBuilder().left().right(wGet, -Const.MARGIN).top(lblFields, Const.MARGIN).bottom().result());


    this.tblFields.getTable().addListener(SWT.Resize, new ColumnsResizer(4, 95));
    
    //
    // Search the inputs fields in the background
    //
    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep(stepname);
        if (stepMeta != null) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
            final List<String> inputFields = new ArrayList<>();
			
			
			if (row != null) {

				
				for (ValueMetaInterface vm : row.getValueMetaList()) {
					inputFields.add(vm.getName());
				}					
				
				// Sort by name
				String[] fieldNames = Const.sortStrings(inputFields.toArray(new String[0]));
				columns[0].setComboValues(fieldNames);
            }

            // Dislay in red missing field names
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                if (!tblFields.isDisposed()) {
                  for (int i = 0; i < tblFields.table.getItemCount(); i++) {
                    TableItem item = tblFields.table.getItem(i);
                    if (!Utils.isEmpty(item.getText(1))) {
                      if (!inputFields.contains(item.getText(1))) {
                        item.setBackground(GUIResource.getInstance().getColorRed());
                      }
                    }
                  }
                }
              }
            });

          } catch (KettleException e) {
            logError(
                BaseMessages.getString(PKG, "CombinationSequenceDialog.ErrorGettingPreviousFields", e.getMessage()));  //$NON-NLS-1$
          }
        }
      }
    };
    new Thread(runnable).start();

    return parent;

  }
}
