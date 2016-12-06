package com.digirati.pygar.mapping;

import java.util.List;
import java.util.Map;

public class BodyTargetFieldData {
    
    private Map<String, List<String>> fieldData;
    private Map<String, List<String>> targetFieldData;
    
    
    public Map<String, List<String>> getFieldData() {
	return fieldData;
    }
    public void setFieldData(Map<String, List<String>> fieldData) {
	this.fieldData = fieldData;
    }
    public Map<String, List<String>> getTargetFieldData() {
	return targetFieldData;
    }
    public void setTargetFieldData(Map<String, List<String>> targetFieldData) {
	this.targetFieldData = targetFieldData;
    }

}
