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

package org.pentaho.di.trans.steps.fieldstomap;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * The Fields To Map step will take key/value pairs from the input rows and join them into a single map field
 *
 */
public class FieldsToMap extends BaseStep implements StepInterface
{
	private static Class<?> PKG = FieldsToMapMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private FieldsToMapMeta meta;
	private FieldsToMapData data;
	
	private Map<Object,Object> map;
	
	public FieldsToMap(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	@Override
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    if(super.init(smi, sdi)) {
      map = new HashMap<Object,Object>();
      return true;
    }
    else return false;
  }
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
	  meta = (FieldsToMapMeta)smi;
	  data = (FieldsToMapData)sdi;
	  
		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
		  // Output map
		  data.outputRowMeta = new RowMeta();
		  RowMetaInterface inputRowClone = getInputRowMeta().clone();
		  // Get output field (map)
	    meta.getFields(inputRowClone, getStepname(), null, null, this, repository, metaStore);
	    
	    // Create a new output row and add the map to it
	    Object[] outputRowData = new Object[] {map};
	    
	    // The Map's valuemeta has been added to inputRowClone at the last index. Add it to the output rowMeta
  	  data.outputRowMeta.addValueMeta(inputRowClone.getValueMeta(getInputRowMeta().size()));
  	  
  	  putRow(data.outputRowMeta, outputRowData);  // copy row to possible alternate rowset(s).
		  
	    setOutputDone();
			return false;
		}
		
		// Add key/value pair to map
		int keyIndex = getInputRowMeta().indexOfValue(meta.getKeyFieldName());
    if(keyIndex < 0) {
      logError(BaseMessages.getString(PKG,"FieldsToMap.Error.NotFound.KeyField"));
		  setErrors(1L);
		  setOutputDone();
      return false;
		}
    int valueIndex = getInputRowMeta().indexOfValue(meta.getValueFieldName());
    if(valueIndex < 0) {
      logError(BaseMessages.getString(PKG,"FieldsToMap.Error.NotFound.ValueField"));
      setErrors(1L);
      setOutputDone();
      return false;
    }
    Object key = r[keyIndex];
    Object value = r[valueIndex];
		map.put(key, value);
		
    if (checkFeedback(getLinesRead())) {
    	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "FieldsToMap.Log.LineNumber")+getLinesRead()); 
    }
			
		return true;
	}
}