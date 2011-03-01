 /*
 * Copyright (C) 2010 Google Code.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.resting.transform.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;



import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.resting.component.Alias;
import com.google.resting.component.impl.ServiceResponse;
import com.google.resting.component.impl.json.JSONAlias;
import com.google.resting.json.JSONArray;
import com.google.resting.json.JSONException;
import com.google.resting.json.JSONObject;
import com.google.resting.transform.Transformer;
/**
 * Base transformer for transforming JSON response. 
 * 
 * @author sujata.de
 * @since resting 0.1
 *
 * @param <T> Target type
 */
public class JSONTransformer<T> implements Transformer<T, ServiceResponse> {

	@Override
	public T createEntity(String source, Class<T> targetType) {
		Gson gson=new Gson();
		T dest=null;
		try {
			dest = gson.fromJson(source,targetType );
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		}
		return dest;
	}//createEntity
	
	public List<T> getEntityList(ServiceResponse serviceResponse, Class<T> targetType, Alias alias){
		List<T> dests=null;
		String singleAlias=null;
		if (alias instanceof JSONAlias)
			//Eliminating the possibility of ClassCastException by checking the type beforehand
			singleAlias=((JSONAlias)alias).getSingleAlias();
		else
			return null;
		JSONObject responseObject=null;
		try {
			responseObject=new JSONObject(serviceResponse.getResponseString());
			
			if(responseObject.has(singleAlias)){
				
				Object aliasedObject=responseObject.get(singleAlias);
				
				//If the entity is JSONArray
				if (aliasedObject instanceof JSONArray){
					JSONArray responseArray=responseObject.getJSONArray(singleAlias);
					int arrayLength=responseArray.length();
					dests=new ArrayList<T>(arrayLength);
					for(int i=0;i<arrayLength;i++){
						JSONObject jsonObject=responseArray.getJSONObject(i);
						dests.add(createEntity(jsonObject.toString(), targetType));
					}	
					return dests;
				}
				else {
					dests=new ArrayList<T>(1);
					dests.add(createEntity(((JSONObject)aliasedObject).toString(),targetType));
					return dests;
				}
			}
			else 
				return null;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return dests;
	}//getEntityList

	@SuppressWarnings("unchecked")
	public Map<String, List> getEntityLists(ServiceResponse serviceResponse, JSONAlias alias) {
		Map<String, List> destMap = new HashMap<String, List>();
		List dests = null;
		JSONObject responseObject = null;
		Set<Entry<String, Class>> aliasSet = alias.getAliasTypeMap().entrySet();
		try {
			responseObject = new JSONObject(serviceResponse.getResponseString());
			for (Map.Entry<String, Class> entry : aliasSet) {
				String singleAlias = entry.getKey();
				Class targetType = entry.getValue();
				if (responseObject.has(singleAlias)) {

					Object aliasedObject = responseObject.get(singleAlias);

					if (aliasedObject instanceof JSONArray) {
						JSONArray responseArray = responseObject
								.getJSONArray(singleAlias);
						int arrayLength = responseArray.length();
						dests = new ArrayList<T>(arrayLength);
						for (int i = 0; i < arrayLength; i++) {
							JSONObject jsonObject = responseArray
									.getJSONObject(i);
							dests.add(createEntity(jsonObject.toString(), targetType));
						}
						destMap.put(singleAlias, dests);
					} else {
						dests = new ArrayList(1);
						dests.add(createEntity(((JSONObject) aliasedObject).toString(), targetType));
						destMap.put(singleAlias, dests);
					}// if
				}// if

			}// for
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return destMap;
	}// getEntityLists
	

}//JSONTransformer