package org.pentaho.di.core.row.value;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

@ValueMetaPlugin( id = "627", name = "Map", description = "A collection of key/value pairs" )
public class ValueMetaMap extends ValueMetaBase implements Cloneable {

  ValueMetaInterface keyMeta;
  ValueMetaInterface valueMeta;

  public static final int TYPE_MAP = 627; // Value is "MAP" on a phone keypad

  public ValueMetaMap() {
    this( null );
  }

  public ValueMetaMap( String name ) {
    super( name, TYPE_MAP );
    try {
      keyMeta = ValueMetaFactory.createValueMeta( ValueMetaString.TYPE_STRING );
      valueMeta = ValueMetaFactory.createValueMeta( ValueMetaString.TYPE_STRING );
    } catch ( KettlePluginException kpe ) {
      // do nothing -- TODO log?
    }
  }

  public ValueMetaMap( String name, ValueMetaInterface keyMeta, ValueMetaInterface valueMeta ) {
    this( name );
    this.keyMeta = keyMeta;
    this.valueMeta = valueMeta;
  }

  @Override
  public ValueMetaMap clone() {
    ValueMetaMap mapMeta = (ValueMetaMap) super.clone();
    mapMeta.keyMeta = ( keyMeta == null ) ? null : keyMeta.clone();
    mapMeta.valueMeta = ( valueMeta == null ) ? null : valueMeta.clone();
    mapMeta.compareStorageAndActualFormat();

    return mapMeta;

  }

  @Override
  public Object cloneValueData( Object object ) throws KettleValueException {
    Map<Object, Object> map = getMap( object );
    if ( map == null )
      return null;

    try {
      Map<Object, Object> mapClone = new ConcurrentHashMap<Object, Object>( map.size() );
      mapClone.putAll( map );
      return mapClone;

    } catch ( Exception e ) {
      throw new KettleValueException( "Unable to clone Map", e );
    }
  }

  @Override
  public String getString( Object object ) throws KettleValueException {
    String mapString = getMap( object ).toString();
    return mapString.substring( 1, mapString.length() - 1 );
  }

  @Override
  public Double getNumber( Object object ) throws KettleValueException {
    throw new KettleValueException( toString() + " : can't be converted to a number" );
  }

  @Override
  public Long getInteger( Object object ) throws KettleValueException {
    throw new KettleValueException( toString() + " : can't be converted to an integer" );
  }

  @Override
  public BigDecimal getBigNumber( Object object ) throws KettleValueException {
    throw new KettleValueException( toString() + " : can't be converted to a big number" );
  }

  @Override
  public Boolean getBoolean( Object object ) throws KettleValueException {
    throw new KettleValueException( toString() + " : can't be converted to a boolean" );
  }

  @Override
  public Date getDate( Object object ) throws KettleValueException {
    throw new KettleValueException( toString() + " : can't be converted to a date" );
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
  public Object convertData( ValueMetaInterface meta2, Object data2 ) throws KettleValueException {
    try {
      switch ( meta2.getType() ) {
        case TYPE_STRING:
          return convertStringToMap( meta2.getString( data2 ) );
        case TYPE_MAP:
          return data2;
        default:
          throw new KettleValueException( meta2.toStringMeta() + " : can't be converted to a Map" );
      }
    } catch ( KettlePluginException kpe ) {
      throw new KettleValueException( kpe );
    }
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getMap( object );
  }

  @SuppressWarnings( "unchecked" )
  public Map<Object, Object> getMap( Object object ) throws KettleValueException {
    try {
      if ( object == null || object instanceof Map ) // NULL
      {
        return (Map<Object, Object>) object;
      }
      switch ( type ) {
        case TYPE_NUMBER:
          throw new KettleValueException( toString() + " : I don't know how to convert a number to a map." );
        case TYPE_STRING:
          switch ( storageType ) {
            case STORAGE_TYPE_NORMAL:
              return convertStringToMap( (String) object );
            case STORAGE_TYPE_BINARY_STRING:
              return convertStringToMap( (String) convertBinaryStringToNativeType( (byte[]) object ) );
            case STORAGE_TYPE_INDEXED:
              return convertStringToMap( (String) index[( (Integer) object ).intValue()] );
            default:
              throw new KettleValueException( toString() + " : Unknown storage type " + storageType + " specified." );
          }
        case TYPE_DATE:
          throw new KettleValueException( toString() + " : I don't know how to convert a date to a map." );
        case TYPE_INTEGER:
          throw new KettleValueException( toString() + " : I don't know how to convert an integer to a map." );
        case TYPE_BIGNUMBER:
          throw new KettleValueException( toString() + " : I don't know how to convert a big number to a map." );
        case TYPE_BOOLEAN:
          throw new KettleValueException( toString() + " : I don't know how to convert a boolean to a map." );
        case TYPE_BINARY:
          throw new KettleValueException( toString() + " : I don't know how to convert binary values to numbers." );
        case TYPE_SERIALIZABLE:
          throw new KettleValueException( toString() + " : I don't know how to convert serializable values to numbers." );
        default:
          throw new KettleValueException( toString() + " : Unknown type " + type + " specified." );
      }
    } catch ( Exception e ) {
      throw new KettleValueException( "Unexpected conversion error while converting value [" + toString()
          + "] to a Map", e );
    }
  }

  protected Map<Object, Object> convertStringToMap( String object ) throws KettlePluginException {
    if ( object == null ) {
      return null;
    }
    Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();

    String keyValuePairs = object.trim();
    if ( keyValuePairs.startsWith( "{" ) && keyValuePairs.endsWith( "}" ) ) {
      keyValuePairs = keyValuePairs.substring( 1, keyValuePairs.length() - 1 );
    }

    String[] kvPairList = Const.splitString( keyValuePairs, ",", "\"" );
    for ( String kvPairString : kvPairList ) {
      String[] kvPair = Const.splitString( kvPairString, "=", "\"" );
      if ( kvPair != null && kvPair.length > 0 ) {
        String value = "";
        if ( kvPair.length > 1 ) {
          value = kvPair[1].trim();
        }
        map.put( kvPair[0].trim(), value );
      }
    }

    // Key/value types assumed to be Strings (see above logic)
    this.setKeyMeta( ValueMetaFactory.createValueMeta( ValueMetaString.TYPE_STRING ) );
    this.setValueMeta( ValueMetaFactory.createValueMeta( ValueMetaString.TYPE_STRING ) );

    return map;
  }

  public ValueMetaInterface getKeyMeta() {
    return keyMeta;
  }

  public void setKeyMeta( ValueMetaInterface keyMeta ) {
    this.keyMeta = keyMeta;
  }

  public ValueMetaInterface getValueMeta() {
    return valueMeta;
  }

  public void setValueMeta( ValueMetaInterface valueMeta ) {
    this.valueMeta = valueMeta;
  }

  @Override
  public Object readData( DataInputStream inputStream ) throws KettleFileException, KettleEOFException,
    SocketTimeoutException {
    try {
      // Is the value NULL?
      if ( inputStream.readBoolean() ) {
        return null; // done
      }

      switch ( storageType ) {
        case STORAGE_TYPE_NORMAL:
          try {
            // Handle Content -- only when not NULL
            int numEntries = inputStream.readInt();
            int keyType = inputStream.readInt();
            int valueType = inputStream.readInt();
            ValueMetaInterface inputKeyMeta = ValueMetaFactory.createValueMeta( keyType );
            ValueMetaInterface inputValueMeta = ValueMetaFactory.createValueMeta( valueType );
            Map<Object, Object> map = new ConcurrentHashMap<Object, Object>( numEntries );
            for ( int i = 0; i < numEntries; i++ ) {
              Object key = inputKeyMeta.readData( inputStream );
              Object value = inputValueMeta.readData( inputStream );
              map.put( key, value );
            }

            return map;
          } catch ( KettlePluginException kpe ) {
            throw new KettleEOFException( kpe );
          }

        case STORAGE_TYPE_BINARY_STRING:
          return readBinaryString( inputStream );

        case STORAGE_TYPE_INDEXED:
          return readSmallInteger( inputStream ); // just an index: 4-bytes should be enough.

        default:
          throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
      }
    } catch ( EOFException e ) {
      throw new KettleEOFException( e );
    } catch ( SocketTimeoutException e ) {
      throw e;
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to read value map data from input stream", e );
    }
  }

  @Override
  public void writeData( DataOutputStream outputStream, Object object ) throws KettleFileException {
    try {
      // Is the value NULL?
      outputStream.writeBoolean( object == null );

      if ( object != null ) {
        switch ( storageType ) {
          case STORAGE_TYPE_NORMAL:
            // Handle Content -- only when not NULL
            @SuppressWarnings( "unchecked" )
            Map<Object, Object> map = (Map<Object, Object>) object;
            // Write number of elements
            outputStream.writeInt( map.size() );
            // Write key,value types
            outputStream.writeInt( keyMeta.getType() );
            outputStream.writeInt( valueMeta.getType() );

            for ( @SuppressWarnings( "rawtypes" )
            Map.Entry entry : map.entrySet() ) {
              keyMeta.writeData( outputStream, entry.getKey() );
              valueMeta.writeData( outputStream, entry.getValue() );
            }
            break;

          case STORAGE_TYPE_BINARY_STRING:
            // Handle binary string content -- only when not NULL
            // In this case, we opt not to convert anything at all for speed.
            // That way, we can save on CPU power.
            // Since the streams can be compressed, volume shouldn't be an issue
            // at all.
            //
            writeBinaryString( outputStream, (byte[]) object );
            break;

          case STORAGE_TYPE_INDEXED:
            writeInteger( outputStream, (Integer) object ); // just an index
            break;

          default:
            throw new KettleFileException( toString() + " : Unknown storage type " + getStorageType() );
        }
      }
    } catch ( ClassCastException e ) {
      throw new RuntimeException( toString() + " : There was a data type error: the data type of "
          + object.getClass().getName() + " object [" + object + "] does not correspond to value meta ["
          + toStringMeta() + "]" );
    } catch ( IOException e ) {
      throw new KettleFileException( toString() + " : Unable to write value map data to output stream", e );
    }
  }

}
