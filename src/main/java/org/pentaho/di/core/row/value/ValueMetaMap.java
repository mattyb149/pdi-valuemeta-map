package org.pentaho.di.core.row.value;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

@ValueMetaPlugin(id="627", name="Map", description="A collection of key/value pairs")
public class ValueMetaMap extends ValueMetaBase {
  
  public static final int TYPE_MAP = 627;  // Value is "MAP" on a phone keypad
  
  public ValueMetaMap() {
    this(null);
  }
  
  public ValueMetaMap(String name) {
    super(name, TYPE_MAP);
  }
    
  
  @Override
  public String getString(Object object) throws KettleValueException {
    return getMap(object).toString();
  }

  @Override
  public Double getNumber(Object object) throws KettleValueException {
    throw new KettleValueException(toString()+" : can't be converted to a number");
  }

  @Override
  public Long getInteger(Object object) throws KettleValueException {
    throw new KettleValueException(toString()+" : can't be converted to an integer");
  }

  @Override
  public BigDecimal getBigNumber(Object object) throws KettleValueException {
    throw new KettleValueException(toString()+" : can't be converted to a big number");
  }

  @Override
  public Boolean getBoolean(Object object) throws KettleValueException {
    throw new KettleValueException(toString()+" : can't be converted to a boolean");
  }

  @Override
  public Date getDate(Object object) throws KettleValueException {
    throw new KettleValueException(toString()+" : can't be converted to a date");
  }

  /**
   * Convert the specified data to the data type specified in this object.
   * 
   * @param meta2
   *          the metadata of the object to be converted
   * @param data2
   *          the data of the object to be converted
   * @return the object in the data type of this value metadata object
   * @throws KettleValueException
   *           in case there is a data conversion error
   */
  @Override
  public Object convertData(ValueMetaInterface meta2, Object data2) throws KettleValueException {
    switch(meta2.getType()) {
    case TYPE_STRING: return convertStringToMap(meta2.getString(data2)); 
    case TYPE_MAP: return data2;
    default: 
      throw new KettleValueException(meta2.toStringMeta()+" : can't be converted to a Map");
    }
  }
  
  @Override
  public Object getNativeDataType(Object object) throws KettleValueException {
    return getMap(object);
  }
  
  @SuppressWarnings("unchecked")
  public Map<Object,Object> getMap(Object object) throws KettleValueException {
    try {
      if (object == null || object instanceof Map ) // NULL
      {
        return (Map<Object, Object>) object;
      }
      switch (type) {
      case TYPE_NUMBER:
        throw new KettleValueException(toString() + " : I don't know how to convert a number to a map.");
      case TYPE_STRING:
        switch (storageType) {
        case STORAGE_TYPE_NORMAL:
          return convertStringToMap((String) object);
        case STORAGE_TYPE_BINARY_STRING:
          return convertStringToMap((String) convertBinaryStringToNativeType((byte[]) object));
        case STORAGE_TYPE_INDEXED:
          return convertStringToMap((String) index[((Integer) object).intValue()]);
        default:
          throw new KettleValueException(toString() + " : Unknown storage type " + storageType + " specified.");
        }
      case TYPE_DATE:
        throw new KettleValueException(toString() + " : I don't know how to convert a date to a map.");
      case TYPE_INTEGER:
        throw new KettleValueException(toString() + " : I don't know how to convert an integer to a map.");
      case TYPE_BIGNUMBER:
        throw new KettleValueException(toString() + " : I don't know how to convert a big number to a map.");
      case TYPE_BOOLEAN:
        throw new KettleValueException(toString() + " : I don't know how to convert a boolean to a map.");
      case TYPE_BINARY:
        throw new KettleValueException(toString() + " : I don't know how to convert binary values to numbers.");
      case TYPE_SERIALIZABLE:
        throw new KettleValueException(toString() + " : I don't know how to convert serializable values to numbers.");
      default:
        throw new KettleValueException(toString() + " : Unknown type " + type + " specified.");
      }
    } catch (Exception e) {
      throw new KettleValueException("Unexpected conversion error while converting value [" + toString()
          + "] to a Map", e);
    }
  }

  protected Map<Object, Object> convertStringToMap(String object) {
    if(object == null) {
      return null;
    }
    Map<Object,Object> map = new HashMap<Object,Object>();
    
    String keyValuePairs = object.trim();
    if(keyValuePairs.startsWith("{") && keyValuePairs.endsWith("}")) {
      keyValuePairs = keyValuePairs.substring(1,keyValuePairs.length()-1);
    }
    
    String[] kvPairList = Const.splitString(object, ",","\"");
    for(String kvPairString : kvPairList) {
      String[] kvPair = Const.splitString(kvPairString, "=","\"");
      map.put(kvPair[0].trim(), kvPair[1].trim());
    }
    
    return map;
  }

}
