package com.m_mb.mm.ws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.kobjects.base64.Base64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class WS {
	
	//region Helpers...
	
	private static void SetBytes(Object object, Object value, Field field) {
		try {
			SoapPrimitive spValue = (SoapPrimitive)value;
			byte[] rValue = Base64.decode(spValue.toString());
			field.set(object, rValue);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void SetInt(Object object, Object value, Field field) {
		try {
			SoapPrimitive spValue = (SoapPrimitive)value;
			Integer intValue = Integer.parseInt(spValue.toString());
			field.set(object, intValue);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static void SetString(Object object, Object value, Field field) {
		try {
			SoapPrimitive spValue = (SoapPrimitive)value;
			field.set(object, spValue.toString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	private static List<Field> GetDMFieldsOnly(Field[] sFields) {
		List<Field> list = new ArrayList<Field>();
		for (Field field : sFields) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			boolean isDataParameter = false;
			for (Annotation a : annotations) {
				if (a instanceof DataMember)
					isDataParameter = true;
			}
			if (isDataParameter)
				list.add(field);
		}
		return list; 
	}
	
	private static <T> void SetFields(Field field, SoapObject result, T nObj) {
		String fieldName = field.getName();
		if (result.hasProperty(fieldName)) {
			Object value = result.getProperty(fieldName);
			Class<?> fieldType = field.getType();
			if (fieldType == String.class) {
				SetString(nObj, value, field);	
			} else if (fieldType == int.class) {
				SetInt(nObj,  value, field);
			} else if (fieldType == byte[].class) {
				SetBytes(nObj, value, field);
			}
		}
	}
	
	private static boolean IsSimpleType(Field field) {
		Class<?> type = field.getType();
		if (type == int.class) {
			return true;
		} else if (type == String.class) {
			return true;
		} else if (type == boolean.class) {
			return true;
		} else if (type == Enum.class) {
			return true;
		} else if (type == float.class) {
			return true;
		} else if (type == Integer.class) {
			return true;
		}
		return false;
	}
	
	private static <T> void CreateObjects(Class<T> type, Vector<SoapObject> collection, List<Field> objFields, List<T> tList) throws Exception {
		for (SoapObject result : collection) {
			T nObj = type.newInstance();
			SetObject(type, nObj, result, objFields);
			tList.add(nObj);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> void SetObject(Class<T> type, T nObj, SoapObject result, List<Field> objFields) throws Exception {
		for (Field field : objFields) {
			if (IsSimpleType(field)) {
				SetFields(field, result, nObj);
			} else {
				Class<T> newType = (Class<T>)field.getType();
				if (result.hasProperty(newType.getSimpleName())) {
					T newObject = (T) newType.newInstance();
					SoapObject newResult = (SoapObject)result.getProperty(newType.getSimpleName());
					List<Field> test = GetDMFieldsOnly(newType.getFields());
					SetObject(newType, newObject, newResult, test);
					field.set(nObj, newObject);	
				}
				
			}
		}
	}
	
	//endregion
	
	//region Public methods...
	
	public static <T> SoapObject CreateSoapObject(String ns, T data) throws Exception {
		SoapObject item = new SoapObject(ns, data.getClass().getSimpleName());
		List<Field> fields = GetDMFieldsOnly(data.getClass().getFields());
		Collections.reverse(fields);
		for (Field field : fields) {
			item.addProperty(field.getName(), field.get(data));
		}
		return item;
	}
	
	public static <T> SoapObject CreateSoapObject(String ns, T data, boolean createNested) throws Exception {
		SoapObject item = new SoapObject(ns, data.getClass().getSimpleName());
		List<Field> fields = GetDMFieldsOnly(data.getClass().getFields());
		Collections.reverse(fields);
		for (Field field : fields) {
			if (IsSimpleType(field)) {
				CreateSoapObject(ns, field.get(data), createNested);
			} else {
				item.addProperty(field.getName(), field.get(data));	
			}
		}
		return item;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> T ReadSimple(Class<T> type, SoapSerializationEnvelope envelope) throws Exception {
		Object obj = envelope.getResponse();
		if (obj == null) 
			throw new IllegalArgumentException();
		SoapPrimitive result = (SoapPrimitive)obj;
		return (T)result.getValue();
	}
	
	public static <T> T Read(Class<T> type, SoapSerializationEnvelope envelope) throws Exception {
		Object obj = envelope.getResponse();
		List<Field> objFields = GetDMFieldsOnly(type.getFields());
		SoapObject result = (SoapObject)obj;
		T nObj = type.newInstance();
		for (Field field : objFields) {
			SetFields(field, result, nObj);
		}
		return nObj;
	}
	
	@SuppressWarnings({ "unchecked" })
	public static <T> List<T> ReadCollection(Class<T> type, SoapSerializationEnvelope envelope) throws Exception {
		Object obj = envelope.getResponse();
		List<T> tList = new ArrayList<T>();
		List<Field> objFields = GetDMFieldsOnly(type.getFields());
		if (obj.getClass() == Vector.class) {
			Vector<SoapObject> collection = (Vector<SoapObject>)obj;
			CreateObjects(type, collection, objFields, tList);
		} else {
			SoapObject result = (SoapObject)obj;
			T nObj = type.newInstance();
			for (Field field : objFields) {
				SetFields(field, result, nObj);
			}
			tList.add(nObj);
		}
		return tList;
	}
	
	//endregion
}