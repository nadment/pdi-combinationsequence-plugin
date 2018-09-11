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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private Text wResult;
  
  private TextVar wStart;

  private TextVar wIncrement;

  private TableView wFields;

  private Button wModeReset;

  private Button wModeIncrement;

  private Map<String, Integer> inputFields;

  private ColumnInfo[] columnInfos;

  public static final String STRING_CHANGE_SEQUENCE_WARNING_PARAMETER = "ChangeSequenceSortWarning";

  public CombinationSequenceDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);

    setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Shell.Title"));

    inputFields = new HashMap<String, Integer>();
  }

  @Override
  public Point getMinimumSize() {
    return new Point(500, 500);
  }

  protected void setComboBoxes() {
    // Something was changed in the row.
    //
    final Map<String, Integer> fields = new HashMap<String, Integer>();

    // Add the currentMeta fields...
    fields.putAll(inputFields);

    Set<String> keySet = fields.keySet();
    List<String> entries = new ArrayList<String>(keySet);

    String[] fieldNames = entries.toArray(new String[entries.size()]);

    Const.sortStrings(fieldNames);
    columnInfos[0].setComboValues(fieldNames);
  }

  protected void onGetFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        TableItemInsertListener insertListener = new TableItemInsertListener() {
          public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v) {
            tableItem.setText(2, BaseMessages.getString(PKG, "System.Combo.Yes"));
            return true;
          }
        };
        BaseStepDialog.getFieldsFromPrevious(r, wFields, 1, new int[] { 1 }, new int[] {}, -1, -1, insertListener);
      }
    } catch (KettleException ke) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"),
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), ke);
    }

  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  protected void loadMeta(final CombinationSequenceMeta meta) {

    if ( meta.getMode()==CombinationSequenceMode.RESET )
      wModeReset.setSelection(true);
    else 
      wModeIncrement.setSelection(true);
    
    wStart.setText(Const.NVL(meta.getStart(), "1"));
    wIncrement.setText(Const.NVL(meta.getIncrement(), "1"));
    wResult.setText(Const.NVL(meta.getResultFieldName(), "result"));

    Table table = wFields.table;
    if (meta.getFieldName().length > 0) {
      table.removeAll();
    }
    for (int i = 0; i < meta.getFieldName().length; i++) {
      TableItem ti = new TableItem(table, SWT.NONE);
      ti.setText(0, "" + (i + 1));
      ti.setText(1, meta.getFieldName()[i]);
    }

    wFields.setRowNums();
    wFields.optWidth(true);

    wStepname.selectAll();
    wStepname.setFocus();
  }

  @Override
  protected void saveMeta(final CombinationSequenceMeta meta) {
    stepname = wStepname.getText(); // return value

    
    
    meta.setMode( wModeReset.getSelection() ? CombinationSequenceMode.RESET: CombinationSequenceMode.INCREMENT );
    meta.setStart(wStart.getText());
    meta.setIncrement(wIncrement.getText());
    meta.setResultFieldName(wResult.getText());

    int nrfields = wFields.nrNonEmpty();
    meta.allocate(nrfields);
    for (int i = 0; i < nrfields; i++) {
      TableItem ti = wFields.getNonEmpty(i);
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
    group.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Label"));
    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
    rowLayout.marginWidth = 10;
    rowLayout.marginHeight = 10;    
    group.setLayout(rowLayout);
    group.setLayoutData(new FormDataBuilder().top().fullWidth().result());
    props.setLook(group);

    wModeReset = new Button(group, SWT.RADIO);
    wModeReset.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Reset.Label"));
    wModeReset.addSelectionListener(lsDef);
    props.setLook(wModeReset);

    wModeIncrement = new Button(group, SWT.RADIO);
    wModeIncrement.setText(BaseMessages.getString(PKG,"CombinationSequenceDialog.Mode.Increment.Label"));
    wModeIncrement.addSelectionListener(lsDef);
    props.setLook(wModeIncrement);

    // Result line...
    Label wlResult = new Label(parent, SWT.NONE);
    wlResult.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Result.Label"));
    wlResult.setLayoutData(new FormDataBuilder().top(group, Const.MARGIN*2).fullWidth().result());
    props.setLook(wlResult);

    wResult = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wResult.setLayoutData(new FormDataBuilder().top(wlResult, Const.MARGIN).fullWidth().result()); 
    wResult.addModifyListener(lsMod);
    props.setLook(wResult);

    // Start
    Label wlStart = new Label(parent, SWT.LEFT);
    wlStart.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Start.Label"));
    wlStart.setLayoutData(new FormDataBuilder().top(wResult, Const.MARGIN*2).fullWidth().result());
    props.setLook(wlStart);
    
    wStart = new TextVar(transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wStart.setLayoutData(new FormDataBuilder().top(wlStart, Const.MARGIN).fullWidth().result());
    wStart.addModifyListener(lsMod);
    props.setLook(wStart);
    
    // Increment
    Label wlIncrement = new Label(parent, SWT.LEFT);
    wlIncrement.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Increment.Label"));
    wlIncrement.setLayoutData(new FormDataBuilder().top(wStart, Const.MARGIN*2).fullWidth().result());   
    props.setLook(wlIncrement);
        
    wIncrement = new TextVar(transMeta, parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wIncrement.setLayoutData(new FormDataBuilder().top(wlIncrement, Const.MARGIN).fullWidth().result());
    wIncrement.addModifyListener(lsMod);
    props.setLook(wIncrement);
    
    // Table with fields
    Label wlFields = new Label(parent, SWT.LEFT);
    wlFields.setText(BaseMessages.getString(PKG, "CombinationSequenceDialog.Fields.Label"));
    wlFields.setLayoutData(new FormDataBuilder().top(wIncrement, Const.MARGIN*2).fullWidth().result());
    
    props.setLook(wlFields);
    
    // Button Get fields
    wGet = new Button(parent, SWT.PUSH);
    wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));
    wGet.setLayoutData(new FormDataBuilder().top(wlFields, Const.MARGIN).right().result());
    wGet.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event e) {
        onGetFields();
      }
    });

    final int FieldsCols = 1;
    final int FieldsRows = this.getStepMeta().getFieldName().length;

    columnInfos = new ColumnInfo[FieldsCols];
    columnInfos[0] = new ColumnInfo(BaseMessages.getString(PKG, "CombinationSequenceDialog.Fieldname.Column"),
        ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
    wFields = new TableView(transMeta, parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, columnInfos, FieldsRows,
        lsMod, props);

    this.wFields.setLayoutData(
        new FormDataBuilder().left().right(wGet, -Const.MARGIN).top(wlFields, Const.MARGIN).bottom().result());


    this.wFields.getTable().addListener(SWT.Resize, new ColumnsResizer(4, 95));
    //
    // Search the fields in the background
    //

    final Runnable runnable = new Runnable() {
      public void run() {
        StepMeta stepMeta = transMeta.findStep(stepname);
        if (stepMeta != null) {
          try {
            RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
            if (row != null) {
              // Remember these fields...
              for (int i = 0; i < row.size(); i++) {
                inputFields.put(row.getValueMeta(i).getName(), new Integer(i));
              }

              setComboBoxes();
            }

            // Dislay in red missing field names
            Display.getDefault().asyncExec(new Runnable() {
              public void run() {
                if (!wFields.isDisposed()) {
                  for (int i = 0; i < wFields.table.getItemCount(); i++) {
                    TableItem it = wFields.table.getItem(i);
                    if (!Utils.isEmpty(it.getText(1))) {
                      if (!inputFields.containsKey(it.getText(1))) {
                        it.setBackground(GUIResource.getInstance().getColorRed());
                      }
                    }
                  }
                }
              }
            });

          } catch (KettleException e) {
            logError(
                BaseMessages.getString(PKG, "CombinationSequenceDialog.ErrorGettingPreviousFields", e.getMessage()));
          }
        }
      }
    };
    new Thread(runnable).start();

    return parent;

  }
}
