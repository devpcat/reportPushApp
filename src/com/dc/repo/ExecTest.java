package com.dc.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;

public class ExecTest {

	public static void main(String[] args) {
		PropertyConfigurator.configure("./config" + "/log4j.properties");
		ExecTest eTest = new ExecTest();
		eTest.compositeFill();
	}

	public void compositeFill() {
		// ģ��ע�� ��{} ����ʾ��Ҫ�õı��� �����������"{","}" �����ַ� ��"\{","\}"����
		// {} ������ͨ���� {.} ������list�ı��� {ǰ׺.} ǰ׺�������ֲ�ͬ��list
		String templateFileName = "C:\\Users\\dlfh-yuc02\\Desktop\\aaaa.xlsx";
		String fileName = "C:\\Users\\dlfh-yuc02\\Desktop\\bbbb.xlsx";
		ExcelWriter excelWriter = EasyExcel.write(fileName).withTemplate(templateFileName).build();
		WriteSheet writeSheet;// = EasyExcel.writerSheet().build();
		writeSheet=EasyExcel.writerSheet(1).build();
		FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();

		// ����ж��list ģ���ϱ�����{ǰ׺.} �����ǰ׺���� data1��Ȼ����list������ FillWrapper����

		excelWriter.fill(new FillWrapper("data1", data()), fillConfig,writeSheet);
		// excelWriter.fill(new FillWrapper("data1", data()), writeSheet);
		excelWriter.fill(new FillWrapper("data2", data()), fillConfig,writeSheet);
		// excelWriter.fill(new FillWrapper("data2", data()), writeSheet);
//		  excelWriter.fill(new FillWrapper("data3", data()), writeSheet);
//		  excelWriter.fill(new FillWrapper("data3", data()), writeSheet);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("date", "2019��10��9��13:28:28");
		excelWriter.fill(map, writeSheet);

		// �����ǹر���
		excelWriter.finish();
	}

	private List<Map> data() {
		List<Map> list = new ArrayList<Map>();
		for (int i = 0; i < 4; i++) {
//            FillData fillData = new FillData();
//            list.add(fillData);
//            fillData.setName("����");
//            fillData.setNumber(5.2);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", "zhangsan");
			map.put("number", 5.2);
			list.add(map);
		}
		return list;
	}

}
