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

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaMap;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;



/**
 * The Fields To Map step will take key/value pairs from the input rows and join them into a single map field
 *
 */
@Step(
    id = "FieldsToMap", 
    image = "fields-to-map.png",
    name = "Fields To Map", 
    description = "Combines key/value field pairs into a single Map", 
    categoryDescription = "Transform")
public class FieldsToMapMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = FieldsToMapMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private String keyFieldName;
	private String valueFieldName;
	private String mapFieldName;

	public FieldsToMapMeta() {
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone() {
	  FieldsToMapMeta retval = (FieldsToMapMeta) super.clone();
    retval.setKeyFieldName(this.keyFieldName);
    retval.setValueFieldName(this.valueFieldName);
    retval.setMapFieldName(this.mapFieldName);
		return retval;
	}
	
	private void readData(Node stepnode) throws KettleXMLException {
	  try{
      this.keyFieldName = XMLHandler.getTagValue(stepnode, "keyfield");
      this.valueFieldName = XMLHandler.getTagValue(stepnode, "valuefield");
      this.mapFieldName = XMLHandler.getTagValue(stepnode, "mapfield");
    }
    catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "FieldsToMapMeta.Exception.UnableToReadStepInfo"), e);
    }
	}

	public void setDefault() {
    this.keyFieldName = null;
    this.valueFieldName = null;
    this.mapFieldName = null;
  }

	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
	  try {
	    this.keyFieldName = rep.getStepAttributeString(id_step, "keyfield");
	    this.valueFieldName = rep.getStepAttributeString(id_step, "valuefield");
	    this.mapFieldName = rep.getStepAttributeString(id_step, "mapfield");   
      
    }
	  catch (Exception e) {
	    throw new KettleException(BaseMessages.getString(PKG, "FieldsToMapMeta.Exception.UnexpectedErrorReadingStepInfo"), e);
	  }
	}
	
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) 
		throws KettleException {
	  try{
      rep.saveStepAttribute(id_transformation, id_step, "keyfield", this.keyFieldName);
      rep.saveStepAttribute(id_transformation, id_step, "valuefield", this.valueFieldName);
      rep.saveStepAttribute(id_transformation, id_step, "mapfield", this.mapFieldName);
    }
    catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "FieldsToMapMeta.Exception.UnexpectedErrorSavingStepInfo"), e); 
    }
	}
	
	public void getFields(RowMetaInterface inputRowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {
	  if (!Const.isEmpty(this.mapFieldName)) {
	    String mapField = (space == null) ? this.mapFieldName : space.environmentSubstitute(this.mapFieldName);
	    
	    // Get class of key field
	    ValueMetaInterface keyMeta = inputRowMeta.searchValueMeta(this.keyFieldName);
	    ValueMetaInterface valueMeta = inputRowMeta.searchValueMeta(this.valueFieldName);
	    
      ValueMetaInterface v = new ValueMetaMap(mapField, keyMeta, valueMeta);
      v.setOrigin(origin);
      inputRowMeta.addValueMeta(v);
    }
	  else {
	    throw new KettleStepException(BaseMessages.getString(PKG,"FieldsToMapMeta.Exception.MapFieldNameNotFound"));
	  }
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "FieldsToMapMeta.CheckResult.NotReceivingFields"), stepMeta); 
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FieldsToMapMeta.CheckResult.StepRecevingData",prev.size()+""), stepMeta);  
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "FieldsToMapMeta.CheckResult.StepRecevingData2"), stepMeta); 
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "FieldsToMapMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta); 
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans) {
		return new FieldsToMap(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData() {
		return new FieldsToMapData();
	}

  public String getKeyFieldName() {
    return keyFieldName;
  }

  public void setKeyFieldName(String keyFieldName) {
    this.keyFieldName = keyFieldName;
  }

  public String getValueFieldName() {
    return valueFieldName;
  }

  public void setValueFieldName(String valueFieldName) {
    this.valueFieldName = valueFieldName;
  }

  public String getMapFieldName() {
    return mapFieldName;
  }

  public void setMapFieldName(String mapFieldName) {
    this.mapFieldName = mapFieldName;
  }

  @Override
  public String getXML() throws KettleException {
    StringBuffer retval = new StringBuffer();
    retval.append("    " + XMLHandler.addTagValue("keyfield", this.keyFieldName));
    retval.append("    " + XMLHandler.addTagValue("valuefield", this.valueFieldName));
    retval.append("    " + XMLHandler.addTagValue("mapfield", this.mapFieldName));
    return retval.toString();
  }

}
