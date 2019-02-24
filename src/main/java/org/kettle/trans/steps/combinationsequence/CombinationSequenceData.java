/*******************************************************************************
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

package org.kettle.trans.steps.combinationsequence;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Nicolas ADMENT
 * @since 23-09-2018
 *
 */
public class CombinationSequenceData extends BaseStepData implements StepDataInterface {

  public ValueMetaInterface[] fieldnrsMeta;
  public RowMetaInterface previousMeta;
  public RowMetaInterface outputRowMeta;

  public int[] fieldnrs;
  public Object[] previousValues;
  public int fieldnr;
  public long startAt;
  public long incrementBy;
  public long seq;
  public int nextIndexField;

  public CombinationSequenceData() {
    super();
  }

}
