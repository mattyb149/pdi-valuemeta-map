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

package org.pentaho.di.trans.steps.maptofields;

import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaMap;
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
public class MapToFields extends BaseStep implements StepInterface
{
  protected static Class<?> PKG = MapToFieldsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	protected MapToFieldsMeta meta;
	protected MapToFieldsData data;
	
	protected int mapFieldIndex;
	protected int keyFieldIndex;
	protected int valueFieldIndex;
	protected RowMetaInterface outputRowMeta;
	protected ValueMetaInterface mapValueMeta;
	protected Object[] outputRowData;
  
	
	public MapToFields(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
	  meta = (MapToFieldsMeta)smi;
	  data = (MapToFieldsData)sdi;
	  
		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
		  setOutputDone();
			return false;
		}
		
		if(first) {
		  RowMetaInterface inputRowMeta = getInputRowMeta();
		  outputRowMeta = inputRowMeta.clone();
		  meta.getFields(outputRowMeta, getStepname(), null, null, this, repository, metaStore);
		  
		  // Get Map field index
		  mapFieldIndex = inputRowMeta.indexOfValue(meta.getMapFieldName());
		  mapValueMeta = inputRowMeta.getValueMeta(mapFieldIndex);
		  if(!ValueMetaMap.class.isAssignableFrom(mapValueMeta.getClass())) {
		    logError(BaseMessages.getString(PKG,"MapToFieldsMeta.Exception.MapFieldNotAMap"));
		    setErrors(1L);
		    setOutputDone();
		    return false;
		  }
		  
		  // Get key field index
      keyFieldIndex = outputRowMeta.indexOfValue(meta.getKeyFieldName());
      if(keyFieldIndex < 0) {
        logError(BaseMessages.getString(PKG,"MapToFieldsMeta.Exception.KeyFieldNameNotFound"));
        setErrors(1L);
        setOutputDone();
        return false;
      }

      // Get value field index
      valueFieldIndex = outputRowMeta.indexOfValue(meta.getValueFieldName());
      if(valueFieldIndex < 0) {
        logError(BaseMessages.getString(PKG,"MapToFieldsMeta.Exception.ValueFieldNameNotFound"));
        setErrors(1L);
        setOutputDone();
        return false;
      }
	    
      first = false;
		}
		
		data.outputRowMeta = outputRowMeta.clone();
		
		Map<Object,Object> map = ((ValueMetaMap)mapValueMeta).getMap(r[mapFieldIndex]);
		Object[] outputRowData = RowDataUtil.removeItem(r, mapFieldIndex);
    if(map != null) {
      for(Map.Entry<Object, Object> entry : map.entrySet()) {
        Object[] newData = new Object[] {entry.getKey(), entry.getValue()};
        outputRowData = RowDataUtil.addRowData(outputRowData,r.length-1, newData);
        putRow(data.outputRowMeta, outputRowData);  // copy row to possible alternate rowset(s).
      }
    }
		
    if (checkFeedback(getLinesRead())) {
    	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "MapToFields.Log.LineNumber")+getLinesRead()); 
    }
			
		return true;
	}
}