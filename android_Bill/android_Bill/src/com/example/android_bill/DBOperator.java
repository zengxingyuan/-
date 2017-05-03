package com.example.android_bill;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class DBOperator {
	private static final String DB_NAME = "bills.db";
	private static final String TABLE_NAME = "Bill";

	private DecimalFormat df;
	private SQLiteDatabase db;

	public DBOperator(Context context) {
		this.db = context.openOrCreateDatabase(DB_NAME, 0, null);
		this.df = new DecimalFormat("#.#");
		createTable();
		// initTableForTest();
	}

	public void createTable() {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "item VARCHAR(5), " + "inorout VARCHAR(2), " + "fee DOUBLE," + "time VARCHAR(15),"
					+ "comment VARCHAR(50))");

		} catch (Exception e) {
			Log.d("Warning", e.getMessage());
		}
	}

	public boolean insert(String item, String inorout, double fee, String time, String comment) {
		try {
			ContentValues cValue = new ContentValues();

			cValue.put("item", item);
			cValue.put("inorout", inorout);
			cValue.put("fee", df.format(fee));
			cValue.put("time", time);
			cValue.put("comment", comment);

			db.insert(TABLE_NAME, null, cValue);
			return true;
		} catch (Exception e) {
			Toast.makeText(MainActivity.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public void update(int specifyid, String[] itemcontent) {
		ContentValues cValue = new ContentValues();

		cValue.put("inorout", itemcontent[0]);
		cValue.put("item", itemcontent[1]);
		cValue.put("fee", df.format(Double.parseDouble(itemcontent[2])));
		cValue.put("time", itemcontent[3]);
		if (itemcontent.length == 4)
			cValue.put("comment", "");
		else
			cValue.put("comment", itemcontent[4]);

		String whereClause = "id=?";
		String[] whereArgs = { String.valueOf(specifyid) };
		db.update(TABLE_NAME, cValue, whereClause, whereArgs);
	}

	public void delete(String[] itemcontent) {
		if (itemcontent.length == 4)
			delete(query(itemcontent[0], itemcontent[1], itemcontent[2], itemcontent[3], ""));
		else
			delete(query(itemcontent[0], itemcontent[1], itemcontent[2], itemcontent[3], itemcontent[4]));
	}

	public void delete(int specifyid) {
		String whereClause = "id=?";
		String[] whereArgs = { String.valueOf(specifyid) };
		db.delete(TABLE_NAME, whereClause, whereArgs);
	}

	public List<Bill> query() {
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		List<Bill> billList = new ArrayList<Bill>(cursor.getCount());
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				Bill bill = parseBill(cursor);
				billList.add(bill);
			}
		}

		return billList;
	}

	public int query(String inorout, String item, String fee, String time, String comment) {
		String sql = "SELECT * FROM " + TABLE_NAME + " WHERE fee=" + fee + " AND inorout=\"" + inorout + "\""
				+ " AND item=\"" + item + "\"" + " AND time=\"" + time + "\"" + " AND comment=\"" + comment + "\"";

		Cursor totalCursor = db.rawQuery(sql, null);
		totalCursor.moveToNext();
		Log.d("cursor", "" + db.rawQuery(sql, null).getCount());

		return Integer.parseInt(totalCursor.getString(totalCursor.getColumnIndex("id")));
	}

	private Bill parseBill(Cursor cursor) {
		String item = cursor.getString(1);
		String inorout = cursor.getString(2);
		float fee = cursor.getFloat(3);
		String time = cursor.getString(4);
		String comment = cursor.getString(5);

		Bill bill = new Bill(item, inorout, fee, time, comment);
		return bill;
	}

	public String getBillsTotal() {
		List<Bill> billList = query();
		double billsTotal = 0.0;
		double fee = 0.0;

		for (int i = 0; i < billList.size(); i++) {
			Bill bill = billList.get(i);
			if (bill.getInorout().contains("支出"))
				fee = -(bill.getFee());
			else
				fee = bill.getFee();

			billsTotal += fee;
		}

		return ("总额    --   " + df.format(billsTotal) + "元");
	}

	public void close() {
		db.close();
	}
}
