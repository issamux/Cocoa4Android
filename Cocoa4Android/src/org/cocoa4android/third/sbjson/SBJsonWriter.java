package org.cocoa4android.third.sbjson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cocoa4android.ns.NSObject;
import org.cocoa4android.ns.NSString;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class SBJsonWriter extends NSObject {
	public NSString stringWithObject(NSObject value){
		String contentString ="";
		JSONStringer js = new JSONStringer();
		serialize(js, value);
		contentString = js.toString();
		return new NSString(contentString);
	}
	/**
	 * ���л�ΪJSON
	 * @param js json����
	 * @param o	�������л��Ķ���
	 */
	private void serialize(JSONStringer js, Object o) {
		if (isNull(o)) {
			try {
				js.value(null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}

		Class<?> clazz = o.getClass();
		if (isObject(clazz)) { // ����
			serializeObject(js, o);
		} else if (isArray(clazz)) { // ����
			serializeArray(js, o);
		} else if (isCollection(clazz)) { // ����
			Collection<?> collection = (Collection<?>) o;
			serializeCollect(js, collection);
		}else if (isMap(clazz)) { // ����
			HashMap<?,?> collection = (HashMap<?,?>) o;
			serializeMap(js, collection);
		} else { // ����ֵ
			try {
				js.value(o);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * ���л����� 
	 * @param js	json����
	 * @param array	����
	 */
	private void serializeArray(JSONStringer js, Object array) {
		try {
			js.array();
			for (int i = 0; i < Array.getLength(array); ++i) {
				Object o = Array.get(array, i);
				serialize(js, o);
			}
			js.endArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * ���л�����
	 * @param js	json����
	 * @param collection	����
	 */
	private void serializeCollect(JSONStringer js, Collection<?> collection) {
		try {
			js.array();
			for (Object o : collection) {
				serialize(js, o);
			}
			js.endArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���л�Map
	 * @param js	json����
	 * @param map	map����
	 */
	private void serializeMap(JSONStringer js, Map<?,?> map) {
		try {
			js.object();
			@SuppressWarnings("unchecked")
			Map<String, Object> valueMap = (Map<String, Object>) map;
			Iterator<Map.Entry<String, Object>> it = valueMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>)it.next();
				js.key(entry.getKey());
				serialize(js,entry.getValue());
			}
			js.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���л�����
	 * @param js	json����
	 * @param obj	�����л�����
	 */
	private void serializeObject(JSONStringer js, Object obj) {
		try {
			js.object();
			Class<? extends Object> objClazz = obj.getClass();
			Method[] methods = objClazz.getDeclaredMethods();   
	        Field[] fields = objClazz.getDeclaredFields();     
	        for (Field field : fields) {   
	            try {   
	                String fieldType = field.getType().getSimpleName();   
	                String fieldGetName = parseMethodName(field.getName(),"get");   
	                if (!haveMethod(methods, fieldGetName)) {   
	                    continue;   
	                }   
	                Method fieldGetMet = objClazz.getMethod(fieldGetName, new Class[] {});   
	                Object fieldVal = fieldGetMet.invoke(obj, new Object[] {});   
	                String result = null;   
	                if ("Date".equals(fieldType)) {   
	                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);   
	                    result = sdf.format((Date)fieldVal);  

	                } else {   
	                    if (null != fieldVal) {   
	                        result = String.valueOf(fieldVal);   
	                    }   
	                }   
	                js.key(field.getName());
					serialize(js, result);  
	            } catch (Exception e) {   
	                continue;   
	            }   
	        }  
			js.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * �ж��Ƿ����ĳ���Ե� get����
	 * @param methods	���÷���������
	 * @param fieldMethod	��������
	 * @return true����false
	 */
	public boolean haveMethod(Method[] methods, String fieldMethod) {
		for (Method met : methods) {
			if (fieldMethod.equals(met.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ƴ��ĳ���Ե� get����set����
	 * @param fieldName	�ֶ�����
	 * @param methodType	��������
	 * @return ��������
	 */
	public String parseMethodName(String fieldName,String methodType) {
		if (null == fieldName || "".equals(fieldName)) {
			return null;
		}
		return methodType + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
	}
	/**
	 * �ж϶����Ƿ�Ϊ��
	 * @param obj	ʵ��
	 * @return
	 */
	private boolean isNull(Object obj) {
		if (obj instanceof JSONObject) {
			return JSONObject.NULL.equals(obj);
		}
		return obj == null;
	}

	/**
	 * �ж��Ƿ���ֵ���� 
	 * @param clazz	
	 * @return
	 */
	private boolean isSingle(Class<?> clazz) {
		return isBoolean(clazz) || isNumber(clazz) || isString(clazz);
	}

	/**
	 * �Ƿ񲼶�ֵ
	 * @param clazz	
	 * @return
	 */
	public boolean isBoolean(Class<?> clazz) {
		return (clazz != null)
				&& ((Boolean.TYPE.isAssignableFrom(clazz)) || (Boolean.class
						.isAssignableFrom(clazz)));
	}

	/**
	 * �Ƿ���ֵ 
	 * @param clazz	
	 * @return
	 */
	public boolean isNumber(Class<?> clazz) {
		return (clazz != null)
				&& ((Byte.TYPE.isAssignableFrom(clazz)) || (Short.TYPE.isAssignableFrom(clazz))
						|| (Integer.TYPE.isAssignableFrom(clazz))
						|| (Long.TYPE.isAssignableFrom(clazz))
						|| (Float.TYPE.isAssignableFrom(clazz))
						|| (Double.TYPE.isAssignableFrom(clazz)) || (Number.class
						.isAssignableFrom(clazz)));
	}

	/**
	 * �ж��Ƿ����ַ��� 
	 * @param clazz	
	 * @return
	 */
	public boolean isString(Class<?> clazz) {
		return (clazz != null)
				&& ((String.class.isAssignableFrom(clazz))
						|| (Character.TYPE.isAssignableFrom(clazz)) || (Character.class
						.isAssignableFrom(clazz)));
	}

	/**
	 * �ж��Ƿ��Ƕ���
	 * @param clazz	
	 * @return
	 */
	private boolean isObject(Class<?> clazz) {
		return clazz != null && !isSingle(clazz) && !isArray(clazz) && !isCollection(clazz) && !isMap(clazz);
	}

	/**
	 * �ж��Ƿ������� 
	 * @param clazz
	 * @return
	 */
	public boolean isArray(Class<?> clazz) {
		return clazz != null && clazz.isArray();
	}

	/**
	 * �ж��Ƿ��Ǽ���
	 * @param clazz
	 * @return
	 */
	public boolean isCollection(Class<?> clazz) {
		return clazz != null && Collection.class.isAssignableFrom(clazz);
	}
		
	/**
	 * �ж��Ƿ���Map
	 * @param clazz
	 * @return
	 */
	public boolean isMap(Class<?> clazz) {
		return clazz != null && Map.class.isAssignableFrom(clazz);
	}
	
	/**
	 * �ж��Ƿ����б� 
	 * @param clazz
	 * @return
	 */
	public boolean isList(Class<?> clazz) {
		return clazz != null && List.class.isAssignableFrom(clazz);
	}
}