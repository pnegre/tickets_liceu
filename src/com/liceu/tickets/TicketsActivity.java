package com.liceu.tickets;

import java.util.*;
import android.util.Log;

import android.app.*;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.content.Intent;
import android.content.res.Resources;
import android.content.Context;

import android.app.AlertDialog;


public class TicketsActivity extends ListActivity
{
	static String TAG="TicketsActivity";
	
	TicketsApp tapp;
	
	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		tapp = (TicketsApp) getApplication();
		setMyListAdapter();
		
		ListView lv = getListView();
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Ticket t = (Ticket) getListView().getItemAtPosition(position);
				executeViewTicketActivity(t.id);
			}
		});
		
		
	}
	
	protected void onResume()
	{
		setMyListAdapter();
		super.onResume();
	}
	
	
	
	private void executeViewTicketActivity(int tickid)
	{
		Intent i = new Intent(this, ViewTicketActivity.class);
		i.putExtra("tickid",tickid);
		startActivity(i);
	}
	
	
	private void setMyListAdapter()
	{
		setListAdapter(new TicketAdapter(this, R.layout.ticklist_row, tapp.database.getTicketList() ));
	}

 
	// Inflate res/menu/mainmenu.xml
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	
	// Respond to user click on menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		// Handle item selection
		switch (item.getItemId()) {
		
		case R.id.preferences:
			showPreferences();
			return true;
		
		case R.id.refresh:
			new ataskRefreshTickets().execute();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showPreferences()
	{
		startActivity( new Intent(this, TicketsPreferences.class) );
	}
	
	
	// ***********************************************************************
	// Adapter class for displaying information about tickets in the main list
	private class TicketAdapter extends ArrayAdapter
	{
		private List tickets;
		
		public TicketAdapter(Context context, int resId, List tickList)
		{
			super(context,resId,tickList);
			tickets = tickList;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			if (v == null) 
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.ticklist_row, null);
			}
			
			Ticket tick = (Ticket) tickets.get(position);
			
			TextView tt = (TextView) v.findViewById(R.id.ticktext);
			TextView tu = (TextView) v.findViewById(R.id.tickuser);
			
			String ttext;
			if (tick.text.length() > 50)
				ttext = tick.text.substring(0,50);
			else
				ttext = tick.text;
			
			tt.setText(ttext);
			tu.setText(tick.user);
			
			return v;
		}
	}
	
	// ****************************************
	// Async task for refreshing tickets screen
	private class ataskRefreshTickets extends AsyncTask<Void,Void,Void> 
	{
		ProgressDialog pb;
		boolean err = false;
		
		protected void onPreExecute() 
		{
			pb = ProgressDialog.show(TicketsActivity.this, "", getString(R.string.wait), true);
		}
		
		protected Void doInBackground(Void... params) 
		{
			try 
			{
				tapp.database.refreshDataFromServer(tapp.server);
			}
			catch (ServerError e) { err = true; } 
			
			return null;
		}
		
		protected void onPostExecute(Void param) 
		{
			pb.dismiss();
			if (err) 
			{
				AlertDialog alertDialog;
				alertDialog = new AlertDialog.Builder(TicketsActivity.this).create();
				alertDialog.setTitle("Error");
				alertDialog.setMessage(getString(R.string.errserver));
				alertDialog.show();
			}
			
			setMyListAdapter();
		}
	}
	
}
