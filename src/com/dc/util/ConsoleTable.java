package com.dc.util;

import java.util.ArrayList;
import java.util.List;

/**
 * ʵ�ֿ���̨������� modify by dlfh-yuc02 on 2015-12-11
 */
@SuppressWarnings("rawtypes")
public class ConsoleTable {

	private List<List> rows = new ArrayList<List>();

	//����
	private int colum;
	//�������ȵ�����
	private int[] columLen;

	private static int margin = 1;

	private boolean printHeader = false;

	public ConsoleTable(int colum, boolean printHeader) {
		this.printHeader = printHeader;
		this.colum = colum;
		this.columLen = new int[colum];
	}

	public void appendRow() {
		List row = new ArrayList(colum);
		rows.add(row);
	}

	@SuppressWarnings("unchecked")
	public ConsoleTable appendColum(Object value) {
		//�����nullҲ��Ҫռһ��λ��
		if (value == null) {
			value = "";
		}
		//ȡ�������е����һ��
		List row = rows.get(rows.size() - 1);
		row.add(value);
		int len = value.toString().getBytes().length;
		if (columLen[row.size() - 1] < len && len < 45)
			//�������С����Ҫչʾ���ַ�
			columLen[row.size() - 1] = len;
		return this;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();

		int sumlen = 0;
		for (int len : columLen) {
			sumlen += len;
		}
		if (printHeader)
			//margin * 2 * colum:ÿ�м���2�ַ�
			//colum - 1���м�������ռ��λ��
			buf.append("|").append(printChar('=', sumlen + margin * 2 * colum + (colum - 1))).append("|\n");
		else
			buf.append("|").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("|\n");
		for (int ii = 0; ii < rows.size(); ii++) {
			List row = rows.get(ii);
			for (int i = 0; i < colum; i++) {
				String o = "";
				if (i < row.size())//�����ǰ�������ֵ
					o = row.get(i).toString();
				buf.append('|').append(printChar(' ', margin)).append(o);
				buf.append(printChar(' ', columLen[i] - o.getBytes().length + margin));
			}//inner-for
			buf.append("|\n");
			if (printHeader && ii == 0)
				buf.append("|").append(printChar('=', sumlen + margin * 2 * colum + (colum - 1))).append("|\n");
			else
				buf.append("|").append(printChar('-', sumlen + margin * 2 * colum + (colum - 1))).append("|\n");
		}
		return buf.toString();
	}

	private String printChar(char c, int len) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < len; i++) {
			buf.append(c);
		}
		return buf.toString();
	}

	public static void main(String[] args) {
		ConsoleTable t = new ConsoleTable(4, true);
		t.appendRow();
		t.appendColum("���");
		t.appendColum("����");
		t.appendColum("�Ա�");
		t.appendColum("����");

		t.appendRow();
		t.appendColum("1");
		t.appendColum("��123213dadad");
		t.appendColum("��");
		t.appendColum("11");

		t.appendRow();
		t.appendColum(null);
		t.appendColum("23123ǿ3333");

		t.appendRow();
		t.appendColum("22");
		t.appendColum("23123ǿ3313666666666666666666666444444444433");

		System.out.println(t);
	}

}
